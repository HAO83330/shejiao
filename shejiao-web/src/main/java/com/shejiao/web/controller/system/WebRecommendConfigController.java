package com.shejiao.web.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shejiao.common.core.controller.BaseController;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.validator.myVaildator.noLogin.NoLoginIntercept;
import com.shejiao.web.domain.entity.WebRecommendConfig;
import com.shejiao.web.domain.entity.WebRecommendMetric;
import com.shejiao.web.domain.vo.NoteSearchVO;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.mapper.WebRecommendConfigMapper;
import com.shejiao.web.mapper.WebRecommendMetricMapper;
import com.shejiao.web.mapper.WebUserMapper;
import com.shejiao.web.service.IWebRecommendService;
import com.shejiao.web.service.impl.WebRecommendMetricServiceImpl;
import com.shejiao.web.mapper.WebFollowMapper;
import com.shejiao.web.mapper.WebLikeOrCollectMapper;
import com.shejiao.web.mapper.WebNoteMapper;
import com.shejiao.web.domain.entity.WebFollow;
import com.shejiao.web.domain.entity.WebLikeOrCollect;
import com.shejiao.web.domain.entity.WebNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 推荐算法配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/web/recommend")
public class WebRecommendConfigController extends BaseController {

    @Autowired
    private WebRecommendConfigMapper recommendConfigMapper;
    
    @Autowired
    private WebRecommendMetricMapper recommendMetricMapper;
    
    @Autowired
    private WebUserMapper userMapper;
    
    @Autowired
    private IWebRecommendService recommendService;
    
    @Autowired
    private WebRecommendMetricServiceImpl recommendMetricService;
    
    @Autowired
    private WebFollowMapper followMapper;
    
    @Autowired
    private WebLikeOrCollectMapper likeOrCollectMapper;
    
    @Autowired
    private WebNoteMapper noteMapper;

    /**
     * 获取推荐算法配置
     */
    @NoLoginIntercept
    @GetMapping("/config")
    public AjaxResult getConfig() {
        List<WebRecommendConfig> configs = recommendConfigMapper.selectList(null);
        if (configs != null && !configs.isEmpty()) {
            return AjaxResult.success(configs.get(0));
        }
        // 返回默认配置
        return AjaxResult.success(WebRecommendConfig.getDefaultConfig());
    }

    /**
     * 更新推荐算法配置
     */
    @NoLoginIntercept
    @PutMapping("/config")
    public AjaxResult updateConfig(@RequestBody WebRecommendConfig config) {
        List<WebRecommendConfig> configs = recommendConfigMapper.selectList(null);
        if (configs != null && !configs.isEmpty()) {
            // 更新现有配置
            config.setId(configs.get(0).getId());
            recommendConfigMapper.updateById(config);
        } else {
            // 新增配置
            recommendConfigMapper.insert(config);
        }
        return AjaxResult.success("配置更新成功");
    }

    /**
     * 重置推荐算法配置为默认值
     */
    @NoLoginIntercept
    @PostMapping("/config/reset")
    public AjaxResult resetConfig() {
        WebRecommendConfig defaultConfig = WebRecommendConfig.getDefaultConfig();
        List<WebRecommendConfig> configs = recommendConfigMapper.selectList(null);
        if (configs != null && !configs.isEmpty()) {
            defaultConfig.setId(configs.get(0).getId());
            recommendConfigMapper.updateById(defaultConfig);
        } else {
            recommendConfigMapper.insert(defaultConfig);
        }
        return AjaxResult.success("配置已重置为默认值");
    }

    /**
     * 获取推荐算法指标
     */
    @NoLoginIntercept
    @GetMapping("/metrics")
    public AjaxResult getMetrics() {
        try {
            // 先计算最新的指标数据
            recommendMetricService.calculateMetrics();
            // 然后获取计算结果
            List<WebRecommendMetric> metrics = recommendMetricMapper.selectList(null);
            return AjaxResult.success(metrics);
        } catch (Exception e) {
            log.warn("获取推荐指标失败: {}", e.getMessage());
            // 即使计算失败，也尝试返回已有数据
            try {
                List<WebRecommendMetric> metrics = recommendMetricMapper.selectList(null);
                return AjaxResult.success(metrics);
            } catch (Exception ex) {
                return AjaxResult.success(new ArrayList<>());
            }
        }
    }

    /**
     * 获取指定算法类型的指标
     */
    @NoLoginIntercept
    @GetMapping("/metrics/{algorithmType}")
    public AjaxResult getMetricsByType(@PathVariable String algorithmType) {
        try {
            QueryWrapper<WebRecommendMetric> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("algorithm_type", algorithmType);
            List<WebRecommendMetric> metrics = recommendMetricMapper.selectList(queryWrapper);
            return AjaxResult.success(metrics);
        } catch (Exception e) {
            log.warn("获取推荐指标失败，可能表不存在: {}", e.getMessage());
            return AjaxResult.success(new ArrayList<>());
        }
    }

    /**
     * 测试推荐算法
     */
    @NoLoginIntercept
    @GetMapping("/note/{userId}")
    public AjaxResult testRecommend(@PathVariable Long userId, @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        try {
            // 检查用户是否存在
            WebUser user = userMapper.selectById(userId);
            if (user == null) {
                return AjaxResult.warn("用户不存在", new ArrayList<>());
            }
            
            List<NoteSearchVO> recommendations = recommendService.getRecommendNote(userId);
            // 限制返回数量
            if (recommendations.size() > pageSize) {
                recommendations = recommendations.subList(0, pageSize);
            }
            return AjaxResult.success(recommendations);
        } catch (Exception e) {
            log.warn("测试推荐失败: {}", e.getMessage());
            return AjaxResult.success(new ArrayList<>());
        }
    }
    
    /**
     * 获取用户信息
     */
    @NoLoginIntercept
    @GetMapping("/user/{userId}")
    public AjaxResult getUserInfo(@PathVariable Long userId) {
        try {
            // 检查用户是否存在
            WebUser user = userMapper.selectById(userId);
            if (user == null) {
                return AjaxResult.warn("用户不存在");
            }
            
            // 构建用户信息响应
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            
            // 获取用户点赞的笔记
            QueryWrapper<WebLikeOrCollect> likeQuery = new QueryWrapper<>();
            likeQuery.eq("uid", userId.toString()).eq("type", 1); // type=1表示点赞笔记
            List<WebLikeOrCollect> likes = likeOrCollectMapper.selectList(likeQuery);
            java.util.List<java.util.Map<String, Object>> likedNotes = new java.util.ArrayList<>();
            for (WebLikeOrCollect like : likes) {
                WebNote note = noteMapper.selectById(like.getLikeOrCollectionId());
                if (note != null) {
                    java.util.Map<String, Object> noteInfo = new java.util.HashMap<>();
                    noteInfo.put("id", note.getId());
                    noteInfo.put("title", note.getTitle());
                    likedNotes.add(noteInfo);
                }
            }
            userInfo.put("likedNotes", likedNotes);
            
            // 获取用户收藏的笔记
            QueryWrapper<WebLikeOrCollect> collectQuery = new QueryWrapper<>();
            collectQuery.eq("uid", userId.toString()).eq("type", 3); // type=3表示收藏笔记
            List<WebLikeOrCollect> collects = likeOrCollectMapper.selectList(collectQuery);
            java.util.List<java.util.Map<String, Object>> collectedNotes = new java.util.ArrayList<>();
            for (WebLikeOrCollect collect : collects) {
                WebNote note = noteMapper.selectById(collect.getLikeOrCollectionId());
                if (note != null) {
                    java.util.Map<String, Object> noteInfo = new java.util.HashMap<>();
                    noteInfo.put("id", note.getId());
                    noteInfo.put("title", note.getTitle());
                    collectedNotes.add(noteInfo);
                }
            }
            userInfo.put("collectedNotes", collectedNotes);
            
            // 获取用户关注的用户
            QueryWrapper<WebFollow> followQuery = new QueryWrapper<>();
            followQuery.eq("uid", userId.toString());
            List<WebFollow> follows = followMapper.selectList(followQuery);
            java.util.List<java.util.Map<String, Object>> followedUsers = new java.util.ArrayList<>();
            for (WebFollow follow : follows) {
                WebUser followedUser = userMapper.selectById(follow.getFid());
                if (followedUser != null) {
                    java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", followedUser.getId());
                    userMap.put("username", followedUser.getUsername());
                    followedUsers.add(userMap);
                }
            }
            userInfo.put("followedUsers", followedUsers);
            
            // 获取关注用户的粉丝
            QueryWrapper<WebFollow> followerQuery = new QueryWrapper<>();
            followerQuery.eq("fid", userId.toString());
            List<WebFollow> followers = followMapper.selectList(followerQuery);
            java.util.List<java.util.Map<String, Object>> followerUsers = new java.util.ArrayList<>();
            for (WebFollow follower : followers) {
                WebUser followerUser = userMapper.selectById(follower.getUid());
                if (followerUser != null) {
                    java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", followerUser.getId());
                    userMap.put("username", followerUser.getUsername());
                    followerUsers.add(userMap);
                }
            }
            userInfo.put("followers", followerUsers);
            
            return AjaxResult.success(userInfo);
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return AjaxResult.error("获取用户信息失败");
        }
    }
}

