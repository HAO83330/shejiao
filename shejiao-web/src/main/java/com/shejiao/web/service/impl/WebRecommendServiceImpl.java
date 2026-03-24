package com.shejiao.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shejiao.common.constant.NoteConstant;
import com.shejiao.common.utils.DozerUtil;
import com.shejiao.web.domain.entity.*;
import com.shejiao.web.domain.vo.NoteSearchVO;
import com.shejiao.web.mapper.*;
import com.shejiao.web.service.IWebRecommendService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 * 实现协同过滤推荐算法
 *
 * @Author shejiao
 */
@Service
@Slf4j
public class WebRecommendServiceImpl implements IWebRecommendService {

    @Autowired
    private WebUserMapper userMapper;
    
    @Autowired
    private WebNoteMapper noteMapper;
    
    @Autowired
    private WebLikeOrCollectMapper likeOrCollectMapper;
    
    @Autowired
    private WebCommentMapper commentMapper;
    
    @Autowired
    private SysVisitMapper visitMapper;
    
    @Autowired
    private WebNavbarMapper navbarMapper;
    
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    @Autowired
    private WebRecommendConfigMapper recommendConfigMapper;
    
    @Autowired
    private WebFollowMapper followMapper;

    /**
     * 获取用户交互数据
     * 包括点赞、收藏、评论、浏览、搜索等
     * 改进版：整合多种行为，设置不同权重，引入时间衰减
     *
     * @return 用户-物品交互矩阵
     */
    private Map<Long, Map<Long, Double>> getUserItemMatrix() {
        Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
        
        long currentTime = System.currentTimeMillis();
        
        WebRecommendConfig config = getConfig();
        
        // 1. 处理点赞和收藏数据
        List<WebLikeOrCollect> likeOrCollects = likeOrCollectMapper.selectList(null);
        for (WebLikeOrCollect record : likeOrCollects) {
            try {
                Long userId = Long.parseLong(record.getUid());
                Long noteId = Long.parseLong(record.getLikeOrCollectionId());
                
                // 只处理笔记相关的点赞和收藏（type=1为点赞图片，type=3为收藏图片）
                if (record.getType() == 1 || record.getType() == 3) {
                    double baseWeight = record.getType() == 3 ? config.getCollectWeight() : config.getLikeWeight();
                    double timeDecay = calculateTimeDecay(record.getTimestamp(), currentTime);
                    double weight = baseWeight * timeDecay;
                    
                    userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>());
                    userItemMatrix.get(userId).merge(noteId, weight, Double::sum);
                }
            } catch (NumberFormatException e) {
                log.warn("解析点赞收藏数据失败: {}", record);
            }
        }
        
        // 2. 处理评论数据
        List<WebComment> comments = commentMapper.selectList(null);
        for (WebComment comment : comments) {
            try {
                Long userId = Long.parseLong(comment.getUid());
                Long noteId = Long.parseLong(comment.getNid());
                
                double baseWeight = config.getCommentWeight();
                double timeDecay = calculateTimeDecay(comment.getCreateTime().getTime(), currentTime);
                double weight = baseWeight * timeDecay;
                
                userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>());
                userItemMatrix.get(userId).merge(noteId, weight, Double::sum);
            } catch (NumberFormatException e) {
                log.warn("解析评论数据失败: {}", comment);
            }
        }
        
        // 3. 处理浏览数据
        QueryWrapper<WebVisit> visitQuery = new QueryWrapper<>();
        visitQuery.eq("behavior", "点击了文章");
        List<WebVisit> visits = visitMapper.selectList(visitQuery);
        for (WebVisit visit : visits) {
            try {
                if (visit.getUserUid() != null && visit.getModuleUid() != null) {
                    Long userId = Long.parseLong(visit.getUserUid());
                    Long noteId = Long.parseLong(visit.getModuleUid());
                    
                    double baseWeight = config.getVisitWeight();
                    double timeDecay = calculateTimeDecay(visit.getCreateTime().getTime(), currentTime);
                    double weight = baseWeight * timeDecay;
                    
                    userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>());
                    userItemMatrix.get(userId).merge(noteId, weight, Double::sum);
                }
            } catch (NumberFormatException e) {
                log.warn("解析浏览数据失败: {}", visit);
            }
        }
        
        // 4. 处理搜索数据
        QueryWrapper<WebVisit> searchQuery = new QueryWrapper<>();
        searchQuery.eq("behavior", "进行了搜索");
        List<WebVisit> searches = visitMapper.selectList(searchQuery);
        for (WebVisit search : searches) {
            try {
                if (search.getUserUid() != null && search.getOtherData() != null) {
                    Long userId = Long.parseLong(search.getUserUid());
                    String searchKeyword = search.getOtherData();
                    
                    // 根据搜索关键词查找相关笔记
                    List<Long> relatedNoteIds = searchNotesByKeyword(searchKeyword, 5);
                    
                    // 为搜索到的笔记添加交互权重
                    double baseWeight = config.getSearchWeight();
                    double timeDecay = calculateTimeDecay(search.getCreateTime().getTime(), currentTime);
                    double weight = baseWeight * timeDecay;
                    
                    userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>());
                    for (Long noteId : relatedNoteIds) {
                        userItemMatrix.get(userId).merge(noteId, weight, Double::sum);
                    }
                }
            } catch (Exception e) {
                log.warn("解析搜索数据失败: {}", search, e);
            }
        }
        
        // 5. 处理关注关系数据
        List<WebFollow> follows = followMapper.selectList(null);
        for (WebFollow follow : follows) {
            try {
                Long userId = Long.parseLong(follow.getUid());
                Long followUserId = Long.parseLong(follow.getFid());
                
                // 获取关注用户发布的笔记
                QueryWrapper<WebNote> noteQuery = new QueryWrapper<>();
                noteQuery.eq("uid", followUserId);
                noteQuery.eq("audit_status", 1); // 只处理审核通过的笔记
                List<WebNote> notes = noteMapper.selectList(noteQuery);
                
                double baseWeight = config.getFollowWeight();
                double timeDecay = calculateTimeDecay(follow.getCreateTime().getTime(), currentTime);
                double weight = baseWeight * timeDecay;
                
                for (WebNote note : notes) {
                    Long noteId = Long.parseLong(note.getId());
                    userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>());
                    userItemMatrix.get(userId).merge(noteId, weight, Double::sum);
                }
            } catch (NumberFormatException e) {
                log.warn("解析关注关系数据失败: {}", follow);
            }
        }
        
        log.info("构建用户-物品交互矩阵完成，用户数: {}", userItemMatrix.size());
        return userItemMatrix;
    }
    
    /**
     * 获取推荐算法配置
     * @return 推荐算法配置
     */
    private WebRecommendConfig getConfig() {
        List<WebRecommendConfig> configs = recommendConfigMapper.selectList(null);
        if (configs != null && !configs.isEmpty()) {
            return configs.get(0);
        }
        return WebRecommendConfig.getDefaultConfig();
    }

    /**
     * 计算时间衰减因子
     * 使用指数衰减：weight * exp(-lambda * days)
     * lambda为衰减系数，days为天数差
     *
     * @param timestamp 行为发生时间戳
     * @param currentTime 当前时间戳
     * @return 时间衰减因子（0-1之间）
     */
    private double calculateTimeDecay(long timestamp, long currentTime) {
        long daysDiff = (currentTime - timestamp) / (1000 * 60 * 60 * 24);
        double lambda = getConfig().getLambda(); // 从配置中获取衰减系数
        return Math.exp(-lambda * daysDiff);
    }

    /**
     * 对相同分数的笔记进行随机排序
     * 增加推荐结果的多样性
     *
     * @param entries 推荐条目列表
     */
    private void randomizeEqualScores(List<Map.Entry<Long, Double>> entries) {
        if (entries.size() <= 1) {
            return;
        }
        
        int i = 0;
        while (i < entries.size()) {
            int j = i + 1;
            double currentScore = entries.get(i).getValue();
            
            // 找到所有相同分数的条目
            while (j < entries.size() && Math.abs(entries.get(j).getValue() - currentScore) < 0.0001) {
                j++;
            }
            
            // 如果有多个相同分数的条目，则随机打乱它们的顺序
            if (j - i > 1) {
                List<Map.Entry<Long, Double>> subList = entries.subList(i, j);
                Collections.shuffle(subList);
            }
            
            i = j;
        }
    }

    /**
     * 根据搜索关键词查找相关笔记
     *
     * @param keyword 搜索关键词
     * @param limit 限制返回数量
     * @return 笔记ID列表
     */
    @SneakyThrows
    private List<Long> searchNotesByKeyword(String keyword, int limit) {
        List<Long> noteIds = new ArrayList<>();
        
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .query(q -> q.multiMatch(m -> m
                            .fields("title", "content", "tags", "categoryName")
                            .query(keyword)))
                    .size(limit));
            
            SearchResponse<NoteSearchVO> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVO.class);
            
            noteIds = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(note -> Long.parseLong(note.getId()))
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("根据关键词搜索笔记失败: {}", keyword, e);
            // Elasticsearch连接失败时返回空列表，避免整个推荐过程失败
            return noteIds;
        }
        
        return noteIds;
    }

    /**
     * 计算用户之间的相似度
     * 改进版：使用调整后的余弦相似度（考虑用户平均评分）
     *
     * @param userItemMatrix 用户-物品交互矩阵
     * @return 用户相似度矩阵
     */
    private Map<Long, Map<Long, Double>> calculateUserSimilarity(Map<Long, Map<Long, Double>> userItemMatrix) {
        Map<Long, Map<Long, Double>> userSimilarityMatrix = new HashMap<>();
        
        // 计算每个用户的平均评分
        Map<Long, Double> userAverageRatings = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userItemMatrix.entrySet()) {
            Long userId = entry.getKey();
            Map<Long, Double> ratings = entry.getValue();
            double sum = ratings.values().stream().mapToDouble(Double::doubleValue).sum();
            double avg = sum / ratings.size();
            userAverageRatings.put(userId, avg);
        }
        
        // 获取所有用户ID
        List<Long> userIds = new ArrayList<>(userItemMatrix.keySet());
        
        // 计算每对用户之间的相似度
        for (int i = 0; i < userIds.size(); i++) {
            Long userId1 = userIds.get(i);
            Map<Long, Double> itemRatings1 = userItemMatrix.get(userId1);
            double avgRating1 = userAverageRatings.get(userId1);
            
            userSimilarityMatrix.putIfAbsent(userId1, new HashMap<>());
            
            for (int j = i + 1; j < userIds.size(); j++) {
                Long userId2 = userIds.get(j);
                Map<Long, Double> itemRatings2 = userItemMatrix.get(userId2);
                double avgRating2 = userAverageRatings.get(userId2);
                
                // 找出共同交互的物品
                Set<Long> commonItems = new HashSet<>(itemRatings1.keySet());
                commonItems.retainAll(itemRatings2.keySet());
                
                // 如果共同物品少于1个，相似度设为0（避免偶然性）
                if (commonItems.size() < 1) {
                    userSimilarityMatrix.get(userId1).put(userId2, 0.0);
                    userSimilarityMatrix.computeIfAbsent(userId2, k -> new HashMap<>()).put(userId1, 0.0);
                    continue;
                }
                
                // 使用调整后的余弦相似度（皮尔逊相关系数）
                double numerator = 0.0;
                double denominator1 = 0.0;
                double denominator2 = 0.0;
                
                for (Long itemId : commonItems) {
                    double rating1 = itemRatings1.get(itemId);
                    double rating2 = itemRatings2.get(itemId);
                    
                    numerator += (rating1 - avgRating1) * (rating2 - avgRating2);
                    denominator1 += Math.pow(rating1 - avgRating1, 2);
                    denominator2 += Math.pow(rating2 - avgRating2, 2);
                }
                
                double denominator = Math.sqrt(denominator1) * Math.sqrt(denominator2);
                double similarity = denominator == 0 ? 0.0 : numerator / denominator;
                
                // 存储相似度
                userSimilarityMatrix.get(userId1).put(userId2, similarity);
                userSimilarityMatrix.computeIfAbsent(userId2, k -> new HashMap<>()).put(userId1, similarity);
            }
        }
        
        log.info("计算用户相似度矩阵完成");
        return userSimilarityMatrix;
    }

    /**
     * 计算物品之间的相似度
     * 改进版：使用调整后的余弦相似度（考虑物品平均评分）
     *
     * @param userItemMatrix 用户-物品交互矩阵
     * @return 物品相似度矩阵
     */
    private Map<Long, Map<Long, Double>> calculateItemSimilarity(Map<Long, Map<Long, Double>> userItemMatrix) {
        Map<Long, Map<Long, Double>> itemUserMatrix = new HashMap<>();
        
        for (Map.Entry<Long, Map<Long, Double>> userEntry : userItemMatrix.entrySet()) {
            Long userId = userEntry.getKey();
            Map<Long, Double> itemRatings = userEntry.getValue();
            
            for (Map.Entry<Long, Double> itemEntry : itemRatings.entrySet()) {
                Long itemId = itemEntry.getKey();
                Double rating = itemEntry.getValue();
                
                itemUserMatrix.computeIfAbsent(itemId, k -> new HashMap<>());
                itemUserMatrix.get(itemId).put(userId, rating);
            }
        }
        
        // 计算每个物品的平均评分
        Map<Long, Double> itemAverageRatings = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : itemUserMatrix.entrySet()) {
            Long itemId = entry.getKey();
            Map<Long, Double> ratings = entry.getValue();
            double sum = ratings.values().stream().mapToDouble(Double::doubleValue).sum();
            double avg = sum / ratings.size();
            itemAverageRatings.put(itemId, avg);
        }
        
        // 计算物品相似度矩阵
        Map<Long, Map<Long, Double>> itemSimilarityMatrix = new HashMap<>();
        
        // 获取所有物品ID
        List<Long> itemIds = new ArrayList<>(itemUserMatrix.keySet());
        
        // 计算每对物品之间的相似度
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId1 = itemIds.get(i);
            Map<Long, Double> userRatings1 = itemUserMatrix.get(itemId1);
            double avgRating1 = itemAverageRatings.get(itemId1);
            
            itemSimilarityMatrix.putIfAbsent(itemId1, new HashMap<>());
            
            for (int j = i + 1; j < itemIds.size(); j++) {
                Long itemId2 = itemIds.get(j);
                Map<Long, Double> userRatings2 = itemUserMatrix.get(itemId2);
                double avgRating2 = itemAverageRatings.get(itemId2);
                
                // 找出共同交互的用户
                Set<Long> commonUsers = new HashSet<>(userRatings1.keySet());
                commonUsers.retainAll(userRatings2.keySet());
                
                // 如果共同用户少于1个，相似度设为0（避免偶然性）
                if (commonUsers.size() < 1) {
                    itemSimilarityMatrix.get(itemId1).put(itemId2, 0.0);
                    itemSimilarityMatrix.computeIfAbsent(itemId2, k -> new HashMap<>()).put(itemId1, 0.0);
                    continue;
                }
                
                // 使用调整后的余弦相似度
                double numerator = 0.0;
                double denominator1 = 0.0;
                double denominator2 = 0.0;
                
                for (Long userId : commonUsers) {
                    double rating1 = userRatings1.get(userId);
                    double rating2 = userRatings2.get(userId);
                    
                    numerator += (rating1 - avgRating1) * (rating2 - avgRating2);
                    denominator1 += Math.pow(rating1 - avgRating1, 2);
                    denominator2 += Math.pow(rating2 - avgRating2, 2);
                }
                
                double denominator = Math.sqrt(denominator1) * Math.sqrt(denominator2);
                double similarity = denominator == 0 ? 0.0 : numerator / denominator;
                
                // 存储相似度
                itemSimilarityMatrix.get(itemId1).put(itemId2, similarity);
                itemSimilarityMatrix.computeIfAbsent(itemId2, k -> new HashMap<>()).put(itemId1, similarity);
            }
        }
        
        log.info("计算物品相似度矩阵完成");
        return itemSimilarityMatrix;
    }

    @Override
    public List<WebUser> getRecommendUser(Long userId) {
        // TODO: 实现用户推荐算法
        // 目前返回随机用户列表
        return getRandomUsers();
    }

    @Override
    public List<NoteSearchVO> getRecommendNote(Long userId) {
        // 添加随机种子，确保每次推荐结果不同
        long randomSeed = System.currentTimeMillis();
        Random random = new Random(randomSeed);
        
        // 默认使用混合协同过滤算法，返回更多结果以支持分页
        List<NoteSearchVO> recommendations = hybridCF(userId, 10, 15, 100);
        
        // 记录推荐行为
        recordRecommendBehavior(userId, recommendations, "hybrid_cf");
        
        // 对推荐结果进行随机打乱，增加多样性
        Collections.shuffle(recommendations, random);
        
        log.info("获取推荐笔记完成，用户ID: {}, 推荐笔记数: {}, 随机种子: {}", userId, recommendations.size(), randomSeed);
        return recommendations;
    }

    @Override
    public List<NoteSearchVO> getRecommendNoteByPage(Long userId, long currentPage, long pageSize) {
        // 添加随机种子，确保每次推荐结果不同
        long randomSeed = System.currentTimeMillis();
        Random random = new Random(randomSeed);
        
        // 计算偏移量
        int offset = (int) ((currentPage - 1) * pageSize);
        
        // 使用混合协同过滤算法，根据页码获取不同范围的推荐结果
        // 每次请求获取更多数据，确保有足够的推荐内容
        int requestSize = (int) pageSize * 2; // 请求2倍的数据量
        List<NoteSearchVO> recommendations = hybridCF(userId, 10, 15, requestSize);
        
        // 记录推荐行为
        recordRecommendBehavior(userId, recommendations, "hybrid_cf");
        
        // 对推荐结果进行随机打乱，增加多样性
        Collections.shuffle(recommendations, random);
        
        // 计算分页范围
        int startIndex = offset % recommendations.size();
        int endIndex = Math.min(startIndex + (int) pageSize, recommendations.size());
        
        // 如果startIndex超出范围，从头开始
        if (startIndex >= recommendations.size()) {
            startIndex = 0;
            endIndex = Math.min((int) pageSize, recommendations.size());
        }
        
        // 获取分页结果
        List<NoteSearchVO> pageResult = new ArrayList<>();
        if (startIndex < endIndex) {
            pageResult = recommendations.subList(startIndex, endIndex);
        }
        
        // 如果结果不足，补充热门推荐
        if (pageResult.size() < pageSize) {
            int needMore = (int) pageSize - pageResult.size();
            Set<Long> existingIds = pageResult.stream()
                    .map(note -> Long.parseLong(note.getId()))
                    .collect(Collectors.toSet());
            
            List<NoteSearchVO> hotNotes = getHotNotes(needMore * 2, existingIds);
            pageResult.addAll(hotNotes.stream().limit(needMore).collect(Collectors.toList()));
        }
        
        log.info("分页获取推荐笔记完成，用户ID: {}, 当前页: {}, 每页大小: {}, 返回数量: {}, 随机种子: {}", 
                userId, currentPage, pageSize, pageResult.size(), randomSeed);
        return pageResult;
    }

    @Override
    public List<NoteSearchVO> userBasedCF(Long userId, int k, int n) {
        log.info("开始基于用户的协同过滤推荐，用户ID: {}, 相似用户数: {}, 推荐数: {}", userId, k, n);
        
        // 1. 构建用户-物品交互矩阵
        Map<Long, Map<Long, Double>> userItemMatrix = getUserItemMatrix();
        
        // 2. 计算用户相似度矩阵
        Map<Long, Map<Long, Double>> userSimilarityMatrix = calculateUserSimilarity(userItemMatrix);
        
        // 3. 获取目标用户的相似用户
        Map<Long, Double> similarUsers = userSimilarityMatrix.getOrDefault(userId, new HashMap<>());
        
        // 4. 按相似度排序，取前k个相似用户（只取相似度大于0的用户）
        List<Map.Entry<Long, Double>> sortedSimilarUsers = similarUsers.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(k)
                .collect(Collectors.toList());
        
        if (sortedSimilarUsers.isEmpty()) {
            log.warn("未找到相似用户，返回随机推荐");
            List<NoteSearchVO> randomNotes = getRandomNotes(n);
            // 记录推荐行为
            recordRecommendBehavior(userId, randomNotes, "user_cf");
            log.info("基于用户的协同过滤推荐完成，推荐笔记数: {}", randomNotes.size());
            return randomNotes;
        }
        
        // 5. 计算目标用户的平均评分
        Map<Long, Double> targetUserItems = userItemMatrix.getOrDefault(userId, new HashMap<>());
        double targetUserAvgRating = targetUserItems.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // 6. 获取相似用户交互过的物品，排除目标用户已交互的物品
        Set<Long> targetUserItemIds = targetUserItems.keySet();
        Map<Long, Double> recommendedItems = new HashMap<>();
        
        for (Map.Entry<Long, Double> similarUserEntry : sortedSimilarUsers) {
            Long similarUserId = similarUserEntry.getKey();
            Double similarity = similarUserEntry.getValue();
            
            Map<Long, Double> similarUserItems = userItemMatrix.getOrDefault(similarUserId, new HashMap<>());
            
            // 计算相似用户的平均评分
            double similarUserAvgRating = similarUserItems.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            
            for (Map.Entry<Long, Double> itemEntry : similarUserItems.entrySet()) {
                Long itemId = itemEntry.getKey();
                Double rating = itemEntry.getValue();
                
                // 如果目标用户已经交互过该物品，则跳过
                if (targetUserItemIds.contains(itemId)) {
                    continue;
                }
                
                // 使用改进的预测公式：预测评分 = 用户平均评分 + 相似度 * (相似用户评分 - 相似用户平均评分)
                double predictedRating = targetUserAvgRating + similarity * (rating - similarUserAvgRating);
                
                // 累加推荐分数（考虑相似度权重）
                double score = similarity * predictedRating;
                recommendedItems.put(itemId, recommendedItems.getOrDefault(itemId, 0.0) + score);
            }
        }
        
        // 如果没有推荐物品，返回随机推荐
        if (recommendedItems.isEmpty()) {
            log.warn("未找到推荐物品，返回随机推荐");
            List<NoteSearchVO> randomNotes = getRandomNotes(n);
            // 记录推荐行为
            recordRecommendBehavior(userId, randomNotes, "user_cf");
            log.info("基于用户的协同过滤推荐完成，推荐笔记数: {}", randomNotes.size());
            return randomNotes;
        }
        
        // 7. 按推荐分数排序，取前n个物品
        List<Map.Entry<Long, Double>> sortedEntries = recommendedItems.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        // 对相同分数的笔记进行随机排序，增加推荐多样性
        randomizeEqualScores(sortedEntries);
        
        List<Long> recommendedNoteIds = sortedEntries.stream()
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 8. 获取推荐笔记的详细信息
        List<NoteSearchVO> result = getNoteSearchVOs(recommendedNoteIds);
        
        // 如果获取笔记失败，返回随机推荐
        if (result.isEmpty()) {
            log.warn("获取推荐笔记失败，返回随机推荐");
            List<NoteSearchVO> randomNotes = getRandomNotes(n);
            // 记录推荐行为
            recordRecommendBehavior(userId, randomNotes, "user_cf");
            log.info("基于用户的协同过滤推荐完成，推荐笔记数: {}", randomNotes.size());
            return randomNotes;
        }
        
        // 记录推荐行为
        recordRecommendBehavior(userId, result, "user_cf");
        
        log.info("基于用户的协同过滤推荐完成，推荐笔记数: {}", result.size());
        return result;
    }

    @Override
    public List<NoteSearchVO> itemBasedCF(Long userId, int k, int n) {
        log.info("开始基于物品的协同过滤推荐，用户ID: {}, 相似物品数: {}, 推荐数: {}", userId, k, n);
        
        // 1. 构建用户-物品交互矩阵
        Map<Long, Map<Long, Double>> userItemMatrix = getUserItemMatrix();
        
        // 2. 获取目标用户已交互的物品
        Map<Long, Double> targetUserItems = userItemMatrix.getOrDefault(userId, new HashMap<>());
        
        // 如果用户没有交互过任何物品，返回随机推荐
        if (targetUserItems.isEmpty()) {
            log.warn("用户无交互记录，返回随机推荐");
            return getRandomNotes(n);
        }
        
        // 3. 计算物品相似度矩阵
        Map<Long, Map<Long, Double>> itemSimilarityMatrix = calculateItemSimilarity(userItemMatrix);
        
        // 4. 计算目标用户的平均评分
        double targetUserAvgRating = targetUserItems.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // 5. 计算每个物品的推荐分数
        Map<Long, Double> recommendedItems = new HashMap<>();
        
        for (Map.Entry<Long, Double> targetItemEntry : targetUserItems.entrySet()) {
            Long targetItemId = targetItemEntry.getKey();
            Double targetRating = targetItemEntry.getValue();
            
            // 获取与目标物品相似的物品
            Map<Long, Double> similarItems = itemSimilarityMatrix.getOrDefault(targetItemId, new HashMap<>());
            
            // 按相似度排序，取前k个相似物品（只取相似度大于0的物品）
            List<Map.Entry<Long, Double>> sortedSimilarItems = similarItems.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(k)
                    .collect(Collectors.toList());
            
            // 计算推荐分数
            for (Map.Entry<Long, Double> similarItemEntry : sortedSimilarItems) {
                Long similarItemId = similarItemEntry.getKey();
                Double similarity = similarItemEntry.getValue();
                
                // 如果目标用户已经交互过该物品，则跳过
                if (targetUserItems.containsKey(similarItemId)) {
                    continue;
                }
                
                // 使用改进的预测公式：预测评分 = 用户平均评分 + 相似度 * (目标物品评分 - 目标物品平均评分)
                // 这里简化处理，使用相似度加权
                double predictedRating = targetRating;
                double score = similarity * predictedRating;
                recommendedItems.put(similarItemId, recommendedItems.getOrDefault(similarItemId, 0.0) + score);
            }
        }
        
        // 6. 按推荐分数排序，取前n个物品
        List<Map.Entry<Long, Double>> sortedEntries = recommendedItems.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        // 对相同分数的笔记进行随机排序，增加推荐多样性
        randomizeEqualScores(sortedEntries);
        
        List<Long> recommendedNoteIds = sortedEntries.stream()
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 7. 获取推荐笔记的详细信息
        List<NoteSearchVO> result = getNoteSearchVOs(recommendedNoteIds);
        
        // 记录推荐行为
        recordRecommendBehavior(userId, result, "item_cf");
        
        log.info("基于物品的协同过滤推荐完成，推荐笔记数: {}", result.size());
        return result;
    }

    @Override
    public List<NoteSearchVO> hybridCF(Long userId, int k1, int k2, int n) {
        log.info("开始混合协同过滤推荐，用户ID: {}, 用户CF参数: {}, 物品CF参数: {}, 推荐数: {}", userId, k1, k2, n);
        
        // 1. 使用用户协同过滤获取推荐
        List<NoteSearchVO> userBasedRecommendations = userBasedCF(userId, k1, n * 2);
        
        // 2. 使用物品协同过滤获取推荐
        List<NoteSearchVO> itemBasedRecommendations = itemBasedCF(userId, k2, n * 2);
        
        // 3. 合并推荐结果，计算综合得分
        Map<Long, HybridScore> mergedRecommendations = new HashMap<>();
        Set<Long> recommendedNoteIds = new HashSet<>();
        
        WebRecommendConfig config = getConfig();
        // 为不同算法的推荐结果设置不同权重
        double userBasedWeight = config.getUserCfWeight();
        double itemBasedWeight = config.getItemCfWeight();
        
        // 处理用户协同过滤的推荐结果
        int userRank = 0;
        for (NoteSearchVO note : userBasedRecommendations) {
            Long noteId = Long.parseLong(note.getId());
            double score = (userBasedRecommendations.size() - userRank) * userBasedWeight;
            mergedRecommendations.compute(noteId, (k, v) -> {
                if (v == null) {
                    return new HybridScore(note, score);
                } else {
                    v.totalScore += score;
                    v.count++;
                    return v;
                }
            });
            recommendedNoteIds.add(noteId);
            userRank++;
        }
        
        // 处理物品协同过滤的推荐结果
        int itemRank = 0;
        for (NoteSearchVO note : itemBasedRecommendations) {
            Long noteId = Long.parseLong(note.getId());
            double score = (itemBasedRecommendations.size() - itemRank) * itemBasedWeight;
            mergedRecommendations.compute(noteId, (k, v) -> {
                if (v == null) {
                    return new HybridScore(note, score);
                } else {
                    v.totalScore += score;
                    v.count++;
                    return v;
                }
            });
            recommendedNoteIds.add(noteId);
            itemRank++;
        }
        
        // 4. 如果推荐结果不足，使用fallback策略补充
        if (mergedRecommendations.size() < n) {
            int needMore = n - mergedRecommendations.size();
            log.info("推荐结果不足，需要补充 {} 条，使用fallback策略", needMore);
            
            // 使用热门推荐作为fallback
            List<NoteSearchVO> hotNotes = getHotNotes(needMore * 2, recommendedNoteIds);
            
            int hotRank = 0;
            for (NoteSearchVO note : hotNotes) {
                if (mergedRecommendations.size() >= n) {
                    break;
                }
                
                Long noteId = Long.parseLong(note.getId());
                if (!recommendedNoteIds.contains(noteId)) {
                    double score = (hotNotes.size() - hotRank) * config.getHotWeight(); // 热门推荐权重
                    mergedRecommendations.put(noteId, new HybridScore(note, score));
                    recommendedNoteIds.add(noteId);
                }
                hotRank++;
            }
            
            // 如果热门推荐还不够，使用最新推荐
            if (mergedRecommendations.size() < n) {
                needMore = n - mergedRecommendations.size();
                log.info("热门推荐仍不足，需要补充 {} 条，使用最新推荐", needMore);
                
                List<NoteSearchVO> latestNotes = getLatestNotes(needMore * 2, recommendedNoteIds);
                
                int latestRank = 0;
                for (NoteSearchVO note : latestNotes) {
                    if (mergedRecommendations.size() >= n) {
                        break;
                    }
                    
                    Long noteId = Long.parseLong(note.getId());
                    if (!recommendedNoteIds.contains(noteId)) {
                        double score = (latestNotes.size() - latestRank) * config.getLatestWeight(); // 最新推荐权重
                        mergedRecommendations.put(noteId, new HybridScore(note, score));
                        recommendedNoteIds.add(noteId);
                    }
                    latestRank++;
                }
            }
        }
        
        // 5. 按综合得分排序
        List<NoteSearchVO> finalRecommendations = mergedRecommendations.values().stream()
                .sorted((a, b) -> {
                    // 优先比较综合得分
                    int scoreCompare = Double.compare(b.totalScore, a.totalScore);
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    // 得分相同时，优先推荐被多种算法推荐的笔记
                    return Integer.compare(b.count, a.count);
                })
                .limit(n)
                .map(HybridScore::getNote)
                .collect(Collectors.toList());
        
        log.info("混合协同过滤推荐完成，推荐笔记数: {}", finalRecommendations.size());
        return finalRecommendations;
    }
    
    /**
     * 混合推荐得分内部类
     */
    private static class HybridScore {
        private NoteSearchVO note;
        private double totalScore;
        private int count;
        
        public HybridScore(NoteSearchVO note, double score) {
            this.note = note;
            this.totalScore = score;
            this.count = 1;
        }
        
        public NoteSearchVO getNote() {
            return note;
        }
    }

    /**
     * 获取随机用户列表
     *
     * @return 随机用户列表
     */
    private List<WebUser> getRandomUsers() {
        List<WebUser> users = userMapper.selectList(new QueryWrapper<WebUser>().last("limit 20"));
        Collections.shuffle(users);
        return users;
    }

    /**
     * 获取随机笔记列表
     *
     * @param n 数量
     * @return 随机笔记列表
     */
    @SneakyThrows
    private List<NoteSearchVO> getRandomNotes(int n) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .size(n * 2));
            SearchResponse<NoteSearchVO> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVO.class);
            
            List<NoteSearchVO> notes = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 为每个笔记设置收藏数、评论数和分类信息
            for (NoteSearchVO note : notes) {
                Long noteId = Long.parseLong(note.getId());
                // 查询收藏数
                QueryWrapper<WebLikeOrCollect> collectQuery = new QueryWrapper<>();
                collectQuery.eq("like_or_collection_id", noteId.toString());
                collectQuery.eq("type", 3); // 3表示收藏
                long collectionCount = likeOrCollectMapper.selectCount(collectQuery);
                note.setCollectionCount(collectionCount);
                
                // 查询评论数
                QueryWrapper<WebComment> commentQuery = new QueryWrapper<>();
                commentQuery.eq("nid", noteId.toString());
                long commentCount = commentMapper.selectCount(commentQuery);
                note.setCommentCount(commentCount);
                
                // 设置分类信息
                if (StringUtils.isNotBlank(note.getCpid())) {
                    WebNavbar category = navbarMapper.selectById(note.getCpid());
                    if (category != null) {
                        note.setCategoryName(category.getTitle());
                    }
                } else if (StringUtils.isNotBlank(note.getCid())) {
                    WebNavbar category = navbarMapper.selectById(note.getCid());
                    if (category != null) {
                        note.setCategoryName(category.getTitle());
                    }
                }
            }
            
            Collections.shuffle(notes);
            return notes.stream().limit(n).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取随机笔记失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门笔记列表（按点赞数排序）
     *
     * @param n 数量
     * @param excludeIds 排除的笔记ID
     * @return 热门笔记列表
     */
    @SneakyThrows
    private List<NoteSearchVO> getHotNotes(int n, Set<Long> excludeIds) {
        try {
            // 添加随机性，从热门笔记中随机选择
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .sort(o -> o.field(f -> f.field("likeCount").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                    .size(n * 5)); // 获取更多数据，用于随机选择
            SearchResponse<NoteSearchVO> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVO.class);
            
            List<NoteSearchVO> notes = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .filter(note -> !excludeIds.contains(Long.parseLong(note.getId())))
                    .collect(Collectors.toList());
            
            // 为每个笔记设置收藏数、评论数和分类信息
            for (NoteSearchVO note : notes) {
                Long noteId = Long.parseLong(note.getId());
                // 查询收藏数
                QueryWrapper<WebLikeOrCollect> collectQuery = new QueryWrapper<>();
                collectQuery.eq("like_or_collection_id", noteId.toString());
                collectQuery.eq("type", 3); // 3表示收藏
                long collectionCount = likeOrCollectMapper.selectCount(collectQuery);
                note.setCollectionCount(collectionCount);
                
                // 查询评论数
                QueryWrapper<WebComment> commentQuery = new QueryWrapper<>();
                commentQuery.eq("nid", noteId.toString());
                long commentCount = commentMapper.selectCount(commentQuery);
                note.setCommentCount(commentCount);
                
                // 设置分类信息
                if (StringUtils.isNotBlank(note.getCpid())) {
                    WebNavbar category = navbarMapper.selectById(note.getCpid());
                    if (category != null) {
                        note.setCategoryName(category.getTitle());
                    }
                } else if (StringUtils.isNotBlank(note.getCid())) {
                    WebNavbar category = navbarMapper.selectById(note.getCid());
                    if (category != null) {
                        note.setCategoryName(category.getTitle());
                    }
                }
            }
            
            // 随机打乱并返回前n个
            Collections.shuffle(notes);
            return notes.stream().limit(n).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取热门笔记失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取最新笔记列表（按创建时间排序）
     *
     * @param n 数量
     * @param excludeIds 排除的笔记ID
     * @return 最新笔记列表
     */
    @SneakyThrows
    private List<NoteSearchVO> getLatestNotes(int n, Set<Long> excludeIds) {
        try {
            // 添加随机性，从最新笔记中随机选择
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(NoteConstant.NOTE_INDEX)
                    .sort(o -> o.field(f -> f.field("createTime").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                    .size(n * 5)); // 获取更多数据，用于随机选择
            SearchResponse<NoteSearchVO> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVO.class);
            
            List<NoteSearchVO> notes = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .filter(note -> !excludeIds.contains(Long.parseLong(note.getId())))
                    .collect(Collectors.toList());
            
            // 为每个笔记设置收藏数、评论数和分类信息
            for (NoteSearchVO note : notes) {
                Long noteId = Long.parseLong(note.getId());
                // 查询收藏数
                QueryWrapper<WebLikeOrCollect> collectQuery = new QueryWrapper<>();
                collectQuery.eq("like_or_collection_id", noteId.toString());
                collectQuery.eq("type", 3); // 3表示收藏
                long collectionCount = likeOrCollectMapper.selectCount(collectQuery);
                note.setCollectionCount(collectionCount);
                
                // 查询评论数
                QueryWrapper<WebComment> commentQuery = new QueryWrapper<>();
                commentQuery.eq("nid", noteId.toString());
                long commentCount = commentMapper.selectCount(commentQuery);
                note.setCommentCount(commentCount);
                
                // 设置分类信息
                if (StringUtils.isNotBlank(note.getCpid())) {
                    WebNavbar category = navbarMapper.selectById(note.getCpid());
                    if (category != null) {
                        note.setCategoryName(category.getTitle());
                    }
                } else if (StringUtils.isNotBlank(note.getCid())) {
                    WebNavbar category = navbarMapper.selectById(note.getCid());
                    if (category != null) {
                        note.setCategoryName(category.getTitle());
                    }
                }
            }
            
            // 随机打乱并返回前n个
            Collections.shuffle(notes);
            return notes.stream().limit(n).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取最新笔记失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 记录推荐行为到web_visit表
     *
     * @param userId 用户ID
     * @param recommendations 推荐结果
     * @param algorithmType 算法类型
     */
    private void recordRecommendBehavior(Long userId, List<NoteSearchVO> recommendations, String algorithmType) {
        try {
            if (recommendations.isEmpty()) {
                return;
            }
            
            // 限制每次记录的推荐数量，避免数据库压力
            int maxRecords = 50;
            int recordCount = 0;
            
            for (NoteSearchVO note : recommendations) {
                if (recordCount >= maxRecords) {
                    break;
                }
                
                if (note == null || note.getId() == null) {
                    continue;
                }
                
                WebVisit visit = new WebVisit();
                visit.setUserUid(userId.toString());
                visit.setBehavior("推荐");
                visit.setModuleUid(note.getId());
                visit.setOtherData(algorithmType);
                visit.setStatus(1); // 设置为有效状态
                visit.setCreateTime(new Date());
                visit.setUpdateTime(new Date());
                
                // 确保必填字段都有值
                if (visit.getUserUid() == null || visit.getBehavior() == null || visit.getModuleUid() == null) {
                    continue;
                }
                
                try {
                    int result = visitMapper.insert(visit);
                    if (result > 0) {
                        recordCount++;
                    }
                } catch (Exception e) {
                    // 记录失败不影响推荐结果返回
                }
            }
        } catch (Exception e) {
            // 记录失败不影响推荐结果返回
        }
    }

    /**
     * 根据笔记ID列表获取NoteSearchVO列表
     *
     * @param noteIds 笔记ID列表
     * @return NoteSearchVO列表
     */
    @SneakyThrows
    private List<NoteSearchVO> getNoteSearchVOs(List<Long> noteIds) {
        if (CollectionUtil.isEmpty(noteIds)) {
            return new ArrayList<>();
        }
        
        List<NoteSearchVO> noteSearchVOs = new ArrayList<>();
        
        try {
            // 根据ID列表从Elasticsearch中获取笔记
            for (Long noteId : noteIds) {
                SearchRequest searchRequest = SearchRequest.of(s -> s
                        .index(NoteConstant.NOTE_INDEX)
                        .query(q -> q.match(m -> m.field("id").query(String.valueOf(noteId))))
                        .size(1));
                
                SearchResponse<NoteSearchVO> searchResponse = elasticsearchClient.search(searchRequest, NoteSearchVO.class);
                
                if (searchResponse.hits().hits().size() > 0) {
                    NoteSearchVO noteSearchVO = searchResponse.hits().hits().get(0).source();
                    if (noteSearchVO != null) {
                        // 查询收藏数
                        QueryWrapper<WebLikeOrCollect> collectQuery = new QueryWrapper<>();
                        collectQuery.eq("like_or_collection_id", noteId.toString());
                        collectQuery.eq("type", 3); // 3表示收藏
                        long collectionCount = likeOrCollectMapper.selectCount(collectQuery);
                        noteSearchVO.setCollectionCount(collectionCount);
                        
                        // 查询评论数
                        QueryWrapper<WebComment> commentQuery = new QueryWrapper<>();
                        commentQuery.eq("nid", noteId.toString());
                        long commentCount = commentMapper.selectCount(commentQuery);
                        noteSearchVO.setCommentCount(commentCount);
                        
                        // 设置分类信息
                        if (StringUtils.isNotBlank(noteSearchVO.getCpid())) {
                            WebNavbar category = navbarMapper.selectById(noteSearchVO.getCpid());
                            if (category != null) {
                                noteSearchVO.setCategoryName(category.getTitle());
                            }
                        } else if (StringUtils.isNotBlank(noteSearchVO.getCid())) {
                            WebNavbar category = navbarMapper.selectById(noteSearchVO.getCid());
                            if (category != null) {
                                noteSearchVO.setCategoryName(category.getTitle());
                            }
                        }
                        
                        noteSearchVOs.add(noteSearchVO);
                    }
                }
            }
        } catch (Exception e) {
            log.error("从Elasticsearch获取笔记失败", e);
            // Elasticsearch连接失败时返回空列表
        }
        
        return noteSearchVOs;
    }
}
