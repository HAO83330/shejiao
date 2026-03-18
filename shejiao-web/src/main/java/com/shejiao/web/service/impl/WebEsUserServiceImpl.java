package com.shejiao.web.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.ScriptLanguage;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.json.JsonData;
import com.shejiao.common.constant.NoteConstant;
import com.shejiao.web.service.IWebEsUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ES 用户服务实现类
 * 用于处理用户相关信息在 Elasticsearch 中的同步更新
 *
 * @Author shejiao
 */
@Service
@Slf4j
public class WebEsUserServiceImpl implements IWebEsUserService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * 更新 ES 中该用户的所有笔记的头像地址
     * 使用 _update_by_query 批量更新匹配 username 的所有文档的 avatar 字段
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param avatar   新的头像地址
     */
    @Override
    public void updateUserAvatarInEs(Long userId, String username, String avatar) {
        try {
            // 构建脚本参数
            Map<String, JsonData> params = new HashMap<>();
            params.put("newAvatar", JsonData.of(avatar));

            // 构建脚本
            Script script = Script.of(s -> s
                    .inline(i -> i
                            .lang(ScriptLanguage.Painless)
                            .source("ctx._source.avatar = params.newAvatar")
                            .params(params)
                    )
            );

            // 构建 update_by_query 请求
            UpdateByQueryRequest request = UpdateByQueryRequest.of(u -> u
                    .index(NoteConstant.NOTE_INDEX)
                    .query(q -> q
                            .match(m -> m
                                    .field("username")
                                    .query(username)
                            )
                    )
                    .script(script)
            );

            UpdateByQueryResponse response = elasticsearchClient.updateByQuery(request);
            log.info("用户 [{}] 头像同步到 ES 完成，更新文档数: {}, 版本冲突数: {}",
                    username,
                    response.updated(),
                    response.versionConflicts());
        } catch (Exception e) {
            log.error("用户 [{}] 头像同步到 ES 失败: {}", username, e.getMessage(), e);
            // 这里可以选择抛出异常或静默处理，根据业务需求决定
            // 抛出异常会导致头像更新事务回滚
            // 静默处理则只记录日志，不影响主流程
        }
    }
}
