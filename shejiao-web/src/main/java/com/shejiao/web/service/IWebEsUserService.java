package com.shejiao.web.service;

/**
 * ES 用户服务接口
 * 用于处理用户相关信息在 Elasticsearch 中的同步更新
 *
 * @Author shejiao
 */
public interface IWebEsUserService {

    /**
     * 更新 ES 中该用户的所有笔记的头像地址
     * 当用户修改头像时调用此方法同步更新 ES
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param avatar   新的头像地址
     */
    void updateUserAvatarInEs(Long userId, String username, String avatar);
}
