package com.shejiao.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.common.constant.UserConstant;
import com.shejiao.common.enums.ResultCodeEnum;
import com.shejiao.common.exception.web.shejiaoException;
import com.shejiao.common.utils.ConvertUtils;
import com.shejiao.common.utils.WebUtils;
import com.shejiao.web.auth.AuthContextHolder;
import com.shejiao.web.domain.entity.WebFollow;
import com.shejiao.web.domain.entity.WebLikeOrCollect;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.NoteSearchVO;
import com.shejiao.web.domain.vo.UserVO;
import com.shejiao.web.mapper.WebLikeOrCollectMapper;
import com.shejiao.web.mapper.WebNoteMapper;
import com.shejiao.web.mapper.WebUserMapper;
import com.shejiao.web.mapper.WebFollowMapper;
import com.shejiao.web.service.IWebEsNoteService;
import com.shejiao.web.service.IWebUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户
 *
 * @Author shejiao
 */
@Service
public class WebUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IWebUserService {

    private static final Logger logger = LoggerFactory.getLogger(WebUserServiceImpl.class);

    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    private WebNoteMapper noteMapper;
    @Autowired
    private IWebEsNoteService esNoteService;
    @Autowired
    private WebLikeOrCollectMapper likeOrCollectionMapper;
    @Autowired
    private WebFollowMapper followMapper;


    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    @Override
    public Page<NoteSearchVO> getTrendByUser(long currentPage, long pageSize, String userId, Integer type) {
        Page<NoteSearchVO> resultPage;
        if (type == 1) {
            resultPage = this.getLikeOrCollectionPageByUser(currentPage, pageSize, userId);
        } else {
            resultPage = this.getLikeOrCollectionPageByUser(currentPage, pageSize, userId, type);
        }
        return resultPage;
    }

    @Override
    public WebUser getUserById(String userId) {
        logger.info("查询用户信息，userId: {}", userId);
        WebUser user = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(user)) {
            logger.warn("用户不存在，userId: {}", userId);
            throw new shejiaoException("用户不存在", 201);
        }
        List<WebNote> noteList = noteMapper.selectList(new QueryWrapper<WebNote>().eq("uid", userId));
        user.setTrendCount((long) noteList.size());
        logger.info("查询用户信息成功，userId: {}, 笔记数量: {}", userId, noteList.size());
        return user;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    @Override
    public WebUser updateUser(WebUser user) {
        WebUser webUser = userMapper.selectById(user.getId());
        if (ObjectUtils.isEmpty(webUser)) {
            throw new shejiaoException(ResultCodeEnum.FAIL);
        }
        webUser.setAvatar(user.getAvatar());
        webUser.setUsername(user.getUsername());
        webUser.setDescription(user.getDescription());
        webUser.setTags(user.getTags());
        webUser.setUpdateTime(new Date());
        userMapper.updateById(webUser);

        // TODO 更新用户ES数据(待优化)
        esNoteService.refreshNoteData();

        return webUser;
    }

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     * @return
     */
    @Override
    public Page<UserVO> getUserByKeyword(long currentPage, long pageSize, String keyword) {
        Page<WebUser> userPage = userMapper.selectPage(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<WebUser>().like("username", keyword));
        
        String currentUserId = AuthContextHolder.getUserId();
        
        List<WebUser> userList = userPage.getRecords();
        
        Set<String> userIds = userList.stream().map(WebUser::getId).collect(Collectors.toSet());
        
        List<WebFollow> followList = followMapper.selectList(new QueryWrapper<WebFollow>().eq("uid", currentUserId).in("fid", userIds));
        Set<String> followedUserIds = followList.stream().map(WebFollow::getFid).collect(Collectors.toSet());
        
        List<UserVO> userVOList = new ArrayList<>();
        userList.forEach(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVO.setId(user.getId());
            userVO.setIsFollow(followedUserIds.contains(user.getId()));
            userVOList.add(userVO);
        });
        
        Page<UserVO> resultPage = new Page<>();
        resultPage.setRecords(userVOList);
        resultPage.setTotal(userPage.getTotal());
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        
        return resultPage;
    }

    /**
     * 保存用户的搜索记录
     *
     * @param keyword 关键词
     */
    @Override
    public void saveUserSearchRecord(String keyword) {
    }

    /**
     * 用户数据
     */
    private Page<NoteSearchVO> getLikeOrCollectionPageByUser(long currentPage, long pageSize, String userId, Integer type) {
        Page<NoteSearchVO> noteSearchVoPage = new Page<>();
        Page<WebLikeOrCollect> likeOrCollectionPage;
        // 得到当前用户发布的所有图片
        if (type == 2) {
            // 所有点赞图片
            likeOrCollectionPage = likeOrCollectionMapper.selectPage(new Page<>(currentPage, pageSize), new QueryWrapper<WebLikeOrCollect>().eq("uid", userId).eq("type", 1).orderByDesc("create_time"));
        } else {
            // 所有收藏图片
            likeOrCollectionPage = likeOrCollectionMapper.selectPage(new Page<>(currentPage, pageSize), new QueryWrapper<WebLikeOrCollect>().eq("uid", userId).eq("type", 3).orderByDesc("create_time"));
        }
        List<WebLikeOrCollect> likeOrCollectionList = likeOrCollectionPage.getRecords();
        long total = likeOrCollectionPage.getTotal();

        Set<String> uids = likeOrCollectionList.stream().map(WebLikeOrCollect::getPublishUid).collect(Collectors.toSet());
        Map<String, WebUser> userMap = this.listByIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));

        Set<String> nids = likeOrCollectionList.stream().map(WebLikeOrCollect::getLikeOrCollectionId).collect(Collectors.toSet());
        Map<String, WebNote> noteMap = noteMapper.selectBatchIds(nids).stream().collect(Collectors.toMap(WebNote::getId, note -> note));

        List<NoteSearchVO> noteSearchVOList = new ArrayList<>();

        for (WebLikeOrCollect model : likeOrCollectionList) {
            WebNote note = noteMap.get(model.getLikeOrCollectionId());
            NoteSearchVO noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVO.class);
            WebUser user = userMap.get(model.getPublishUid());
            noteSearchVo.setUsername(user.getUsername())
                    .setAvatar(user.getAvatar());
            noteSearchVOList.add(noteSearchVo);
        }
        
        // 查询当前用户的点赞状态并更新isLike字段
        this.updateLikeStatus(noteSearchVOList);
        
        noteSearchVoPage.setRecords(noteSearchVOList);
        noteSearchVoPage.setTotal(total);
        return noteSearchVoPage;
    }

    /**
     * 根据条件分页查询角色数据
     *
     * @param user 角色信息
     * @return 角色数据集合信息
     */
    @Override
//    @DataScope(deptAlias = "d")
    public List<WebUser> getUserList(WebUser user) {
        return userMapper.getUserList(user);
    }

    /**
     * 更新笔记列表的点赞状态
     * @param noteSearchVOList 笔记列表
     */
    private void updateLikeStatus(List<NoteSearchVO> noteSearchVOList) {
        if (CollectionUtil.isEmpty(noteSearchVOList)) {
            return;
        }
        
        // 获取当前登录用户ID
        String currentUserId = AuthContextHolder.getUserId();
        if (StringUtils.isBlank(currentUserId)) {
            // 未登录用户，所有笔记都标记为未点赞
            for (NoteSearchVO noteSearchVO : noteSearchVOList) {
                noteSearchVO.setIsLike(false);
            }
            return;
        }
        
        // 查询当前用户的所有点赞记录
        List<WebLikeOrCollect> likeOrCollections = likeOrCollectionMapper.selectList(
                new QueryWrapper<WebLikeOrCollect>()
                        .eq("uid", currentUserId)
                        .eq("type", 1)); // 1表示点赞
        
        // 提取所有点赞的笔记ID
        List<String> likeIds = likeOrCollections.stream()
                .map(WebLikeOrCollect::getLikeOrCollectionId)
                .collect(Collectors.toList());
        
        // 更新每个笔记的isLike字段
        for (NoteSearchVO noteSearchVO : noteSearchVOList) {
            noteSearchVO.setIsLike(likeIds.contains(noteSearchVO.getId()));
        }
    }
    
    /**
     * 用户数据
     */
    private Page<NoteSearchVO> getLikeOrCollectionPageByUser(long currentPage, long pageSize, String userId) {
        Page<NoteSearchVO> noteSearchVoPage = new Page<>();
        // 得到当前用户发布的所有专辑
        String currentUserId = AuthContextHolder.getUserId();
        Page<WebNote> notePage;
        notePage = noteMapper.selectPage(new Page<>(currentPage, pageSize), new QueryWrapper<WebNote>().eq("uid", userId).orderByDesc("pinned", "update_time"));
        List<WebNote> noteList = notePage.getRecords();
        long total = notePage.getTotal();

        // 得到所有用户的信息
        Set<String> uids = noteList.stream().map(WebNote::getUid).collect(Collectors.toSet());

        if (CollectionUtil.isNotEmpty(uids)) {
            Map<String, WebUser> userMap = this.listByIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            List<NoteSearchVO> noteSearchVOList = new ArrayList<>();
            for (WebNote note : noteList) {
                NoteSearchVO noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVO.class);
                WebUser user = userMap.get(note.getUid());
                noteSearchVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(note.getUpdateTime().getTime());
                if (!currentUserId.equals(userId)) {
                    noteSearchVo.setViewCount(null);
                }
                noteSearchVOList.add(noteSearchVo);
            }
            
            // 查询当前用户的点赞状态并更新isLike字段
            this.updateLikeStatus(noteSearchVOList);
            
            noteSearchVoPage.setRecords(noteSearchVOList);
            noteSearchVoPage.setTotal(total);
        }
        return noteSearchVoPage;
    }
}
