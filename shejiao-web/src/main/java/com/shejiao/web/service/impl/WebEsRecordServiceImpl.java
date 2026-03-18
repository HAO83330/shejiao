package com.shejiao.web.service.impl;

import cn.hutool.core.util.RandomUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.shejiao.common.constant.NoteConstant;
import com.shejiao.web.domain.dto.EsRecordDTO;
import com.shejiao.web.domain.vo.RecordSearchVO;
import com.shejiao.web.service.IWebEsRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ES
 *
 * @Author shejiao
 */
@Service
@Slf4j
public class WebEsRecordServiceImpl implements IWebEsRecordService {

    @Autowired
    ElasticsearchClient elasticsearchClient;


    /**
     * 获取搜索记录
     */
    @Override
    public List<RecordSearchVO> getRecordByKeyWord(EsRecordDTO esRecordDTO) {
        String keyword = esRecordDTO.getKeyword();
        String uid = esRecordDTO.getUid();

        List<RecordSearchVO> records = new ArrayList<>();
        try {
            // 检查索引是否存在
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                // 如果索引不存在，返回热门搜索
                return getHotRecord();
            }
            
            // 1. 获取用户的历史搜索记录
            if (StringUtils.isNotBlank(uid)) {
                SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.RECORD_INDEX);
                builder.query(q -> q.bool(b -> {
                    b.must(m -> m.term(t -> t.field("uid").value(uid)));
                    if (StringUtils.isNotBlank(keyword)) {
                        b.must(m -> m.match(f -> f.field("content").query(keyword)));
                    }
                    return b;
                }));
                builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
                builder.highlight(h -> h.fields("content", m -> m).preTags("<font color='#409eff'>").postTags("</font>"));
                builder.size(5); // 限制用户历史记录数量

                SearchRequest searchRequest = builder.build();
                SearchResponse<RecordSearchVO> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVO.class);

                List<Hit<RecordSearchVO>> hits = searchResponse.hits().hits();
                for (Hit<RecordSearchVO> hit : hits) {
                    RecordSearchVO recordSearchVo = hit.source();
                    records.add(recordSearchVo);
                }
            }

            // 2. 获取相关的热门搜索记录
            if (StringUtils.isNotBlank(keyword)) {
                List<RecordSearchVO> hotRecords = getHotRecord();
                List<RecordSearchVO> relatedHotSearches = new ArrayList<>();
                
                for (RecordSearchVO hotRecord : hotRecords) {
                    if (hotRecord.getContent().contains(keyword)) {
                        // 避免重复
                        boolean isDuplicate = records.stream()
                                .anyMatch(r -> r.getContent().equals(hotRecord.getContent()));
                        if (!isDuplicate) {
                            relatedHotSearches.add(hotRecord);
                        }
                    }
                }
                
                // 添加相关热门搜索，限制总数不超过10个
                int remainingSlots = 10 - records.size();
                if (remainingSlots > 0) {
                    records.addAll(relatedHotSearches.stream().limit(remainingSlots).collect(Collectors.toList()));
                }
            } else if (records.isEmpty()) {
                // 如果没有关键词且用户没有历史记录，返回空列表
                // 新用户的搜索历史应该是空的
                return records;
            }

            return records;
        } catch (Exception e) {
            log.error("Search failed, returning hot records", e);
            // 出错时返回热门搜索
            return getHotRecord();
        }
    }

    /**
     * 热门搜索
     */
    @Override
    public List<RecordSearchVO> getHotRecord() {
        List<RecordSearchVO> records = new ArrayList<>();
        try {
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                return records;
            }
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.RECORD_INDEX);
            builder.sort(o -> o.field(f -> f.field("searchCount").order(SortOrder.Desc)));
            builder.size(100);
            SearchRequest searchRequest = builder.build();
            SearchResponse<RecordSearchVO> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVO.class);
            List<Hit<RecordSearchVO>> hits = searchResponse.hits().hits();
            
            Map<String, RecordSearchVO> uniqueRecords = new HashMap<>();
            for (Hit<RecordSearchVO> hit : hits) {
                RecordSearchVO recordSearchVo = hit.source();
                String content = recordSearchVo.getContent();
                
                if (!uniqueRecords.containsKey(content)) {
                    uniqueRecords.put(content, recordSearchVo);
                } else {
                    RecordSearchVO existing = uniqueRecords.get(content);
                    if (recordSearchVo.getSearchCount() > existing.getSearchCount()) {
                        uniqueRecords.put(content, recordSearchVo);
                    }
                }
            }
            
            records = uniqueRecords.values().stream()
                    .sorted((a, b) -> Long.compare(b.getSearchCount(), a.getSearchCount()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            return records;
        } catch (Exception e) {
            log.error("Get hot records failed, returning empty list", e);
        }
        return records;
    }

    /**
     * 增加搜索记录
     */
    @Override
    public void addRecord(EsRecordDTO esRecordDTO) {
        String keyword = esRecordDTO.getKeyword();
        String uid = esRecordDTO.getUid();
        try {
            String trimmedKeyword = keyword.trim();
            
            if (StringUtils.isBlank(trimmedKeyword)) {
                log.warn("搜索关键词为空，跳过记录");
                return;
            }
            
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e.index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                elasticsearchClient.indices().create(c -> c.index(NoteConstant.RECORD_INDEX));
            }
            
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.RECORD_INDEX);
            
            if (StringUtils.isNotBlank(trimmedKeyword)) {
                builder.query(q -> q.bool(b -> {
                    b.must(m -> m.term(t -> t.field("content.keyword").value(trimmedKeyword)));
                    if (StringUtils.isNotBlank(uid)) {
                        b.must(m -> m.term(t -> t.field("uid").value(uid)));
                    }
                    return b;
                }));
            }
            builder.size(100);
            SearchRequest searchRequest = builder.build();
            SearchResponse<RecordSearchVO> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVO.class);
            List<Hit<RecordSearchVO>> hits = searchResponse.hits().hits();

            if (hits.isEmpty()) {
                RecordSearchVO recordSearchVo = new RecordSearchVO();
                recordSearchVo.setContent(trimmedKeyword);
                recordSearchVo.setSearchCount(1L);
                recordSearchVo.setUid(uid);
                recordSearchVo.setTime(System.currentTimeMillis());
                String id = RandomUtil.randomString(12);
                elasticsearchClient.create(c -> c.index(NoteConstant.RECORD_INDEX).id(id).document(recordSearchVo));
                log.info("新增搜索记录: {}, uid: {}", trimmedKeyword, uid);
            } else {
                long totalCount = 0L;
                List<String> idsToDelete = new ArrayList<>();
                
                for (int i = 0; i < hits.size(); i++) {
                    Hit<RecordSearchVO> hit = hits.get(i);
                    RecordSearchVO recordSearchVo = hit.source();
                    totalCount += recordSearchVo.getSearchCount();
                    
                    if (i < hits.size() - 1) {
                        idsToDelete.add(hit.id());
                    }
                }
                
                totalCount += 1;
                
                Hit<RecordSearchVO> lastHit = hits.get(hits.size() - 1);
                RecordSearchVO lastRecord = lastHit.source();
                lastRecord.setSearchCount(totalCount);
                lastRecord.setUid(uid);
                lastRecord.setTime(System.currentTimeMillis());
                
                elasticsearchClient.update(u -> u.index(NoteConstant.RECORD_INDEX).id(lastHit.id()).doc(lastRecord), RecordSearchVO.class);
                log.info("更新搜索记录: {}, 总搜索次数: {}, uid: {}", trimmedKeyword, totalCount, uid);
                
                for (String id : idsToDelete) {
                    try {
                        elasticsearchClient.delete(d -> d.index(NoteConstant.RECORD_INDEX).id(id));
                        log.info("删除重复记录ID: {}", id);
                    } catch (Exception e) {
                        log.error("删除重复记录失败: {}", id, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("添加搜索记录失败", e);
        }
    }

    /**
     * 删除搜索记录
     */
    @Override
    public void clearRecordByUser(EsRecordDTO esRecordDTO) {
        String keyword = esRecordDTO.getKeyword();
        String uid = esRecordDTO.getUid();

        try {
            // 检查索引是否存在
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                log.warn("Index does not exist. No records to clear.");
                return;
            }

            // 构建删除请求
            DeleteByQueryRequest.Builder deleteRequestBuilder = new DeleteByQueryRequest.Builder()
                    .index(NoteConstant.RECORD_INDEX)
                    .query(q -> q.bool(b -> {
                        b.must(m -> m.term(t -> t.field("uid").value(uid)));
                        if (StringUtils.isNotBlank(keyword)) {
                            b.must(m -> m.term(t -> t.field("content.keyword").value(keyword.trim())));
                        }
                        return b;
                    }));

            // 执行删除操作
            DeleteByQueryResponse deleteResponse = elasticsearchClient.deleteByQuery(deleteRequestBuilder.build());

            log.info("Deleted {} records for uid: {}", deleteResponse.deleted(), uid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 清空搜索记录
     */
    @Override
    public void clearAllRecord() {
        try {
            // 检查索引是否存在
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (exists.value()) {
                // 删除整个索引
                elasticsearchClient.indices().delete(d -> d.index(NoteConstant.RECORD_INDEX));
                // 重新创建索引
                elasticsearchClient.indices().create(c -> c.index(NoteConstant.RECORD_INDEX));
                log.info("All search records have been cleared.");
            } else {
                log.warn("Index does not exist. No records to clear.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取个性化推荐搜索词（猜你想搜）
     * 基于用户历史搜索、浏览记录和热搜
     */
    @Override
    public List<RecordSearchVO> getGuessYouWant(String uid) {
        List<RecordSearchVO> recommendations = new ArrayList<>();
        
        try {
            // 检查索引是否存在
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                return getHotRecord();
            }
            
            // 如果是新用户（没有 uid 或 uid 为空），返回热门标签 + 随机推荐
            if (StringUtils.isBlank(uid)) {
                log.info("新用户或无用户信息，返回热门标签推荐");
                return getDefaultRecommendations();
            }
            
            // 1. 获取用户的历史搜索记录
            List<RecordSearchVO> userHistory = getUserHistory(uid);
            
            // 如果用户没有搜索记录，返回默认推荐
            if (userHistory.isEmpty()) {
                log.info("用户无搜索记录，返回默认推荐");
                return getDefaultRecommendations();
            }
            
            // 2. 基于历史搜索生成推荐
            // 2.1 从历史搜索中提取关键词
            Set<String> historyKeywords = new HashSet<>();
            for (RecordSearchVO record : userHistory) {
                historyKeywords.add(record.getContent());
            }
            
            // 2.2 根据历史搜索推荐相关词（搜索包含相同关键词的热门搜索）
            List<RecordSearchVO> relatedHotSearches = getRelatedHotSearches(historyKeywords, uid);
            recommendations.addAll(relatedHotSearches);
            
            // 2.3 如果推荐数量不足，补充热门搜索
            if (recommendations.size() < 8) {
                List<RecordSearchVO> hotRecords = getHotRecord();
                for (RecordSearchVO hotRecord : hotRecords) {
                    if (recommendations.size() >= 8) {
                        break;
                    }
                    // 避免重复
                    boolean isDuplicate = recommendations.stream()
                            .anyMatch(r -> r.getContent().equals(hotRecord.getContent()));
                    if (!isDuplicate) {
                        recommendations.add(hotRecord);
                    }
                }
            }
            
            // 3. 随机打乱推荐结果，增加多样性
            Collections.shuffle(recommendations);
            
            // 4. 返回前 8 个推荐
            return recommendations.stream().limit(8).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取个性化推荐失败，返回热门搜索", e);
        }
        
        // 如果出错，返回热门搜索作为降级
        return getHotRecord().stream().limit(8).collect(Collectors.toList());
    }
    
    /**
     * 获取用户的历史搜索记录
     */
    private List<RecordSearchVO> getUserHistory(String uid) {
        List<RecordSearchVO> records = new ArrayList<>();
        try {
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index(NoteConstant.RECORD_INDEX)
                    .query(q -> q.term(t -> t.field("uid").value(uid)))
                    .sort(o -> o.field(f -> f.field("searchCount").order(SortOrder.Desc)))
                    .size(20);
            
            SearchRequest searchRequest = builder.build();
            SearchResponse<RecordSearchVO> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVO.class);
            
            for (Hit<RecordSearchVO> hit : searchResponse.hits().hits()) {
                records.add(hit.source());
            }
        } catch (Exception e) {
            log.error("获取用户历史搜索失败", e);
        }
        return records;
    }
    
    /**
     * 获取与历史搜索相关的热门搜索
     */
    private List<RecordSearchVO> getRelatedHotSearches(Set<String> keywords, String excludeUid) {
        List<RecordSearchVO> relatedSearches = new ArrayList<>();
        try {
            // 获取热门搜索
            List<RecordSearchVO> hotRecords = getHotRecord();
            
            // 筛选与历史搜索相关的热门词
            for (RecordSearchVO hotRecord : hotRecords) {
                String content = hotRecord.getContent();
                // 检查热门词是否包含历史搜索词，或者历史搜索词是否包含热门词
                for (String keyword : keywords) {
                    if (content.contains(keyword) || keyword.contains(content)) {
                        // 排除用户已经搜索过的词
                        if (!keywords.contains(content)) {
                            relatedSearches.add(hotRecord);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取相关热门搜索失败", e);
        }
        return relatedSearches;
    }
    
    /**
     * 获取默认推荐（用于新用户）
     * 结合热门标签和随机推荐
     */
    private List<RecordSearchVO> getDefaultRecommendations() {
        List<RecordSearchVO> recommendations = new ArrayList<>();
        try {
            // 1. 获取热门搜索
            List<RecordSearchVO> hotRecords = getHotRecord();
            recommendations.addAll(hotRecords);
            
            // 2. 如果热门不足，补充一些常用标签
            if (recommendations.size() < 8) {
                String[] defaultTags = {"壁纸", "风景", "情侣", "头像", "动漫", "动物", "美食", "旅行"};
                Random random = new Random();
                
                for (String tag : defaultTags) {
                    if (recommendations.size() >= 8) {
                        break;
                    }
                    // 避免重复
                    boolean isDuplicate = recommendations.stream()
                            .anyMatch(r -> r.getContent().equals(tag));
                    if (!isDuplicate) {
                        RecordSearchVO vo = new RecordSearchVO();
                        vo.setContent(tag);
                        vo.setSearchCount((long) (random.nextInt(100) + 1));
                        recommendations.add(vo);
                    }
                }
            }
            
            // 3. 随机打乱
            Collections.shuffle(recommendations);
            
        } catch (Exception e) {
            log.error("获取默认推荐失败", e);
        }
        return recommendations.stream().limit(8).collect(Collectors.toList());
    }
}
