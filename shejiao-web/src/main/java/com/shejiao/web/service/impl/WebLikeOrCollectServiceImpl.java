package com.shejiao.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.common.constant.UserConstant;
import com.shejiao.common.utils.ConvertUtils;
import com.shejiao.common.utils.WebUtils;
import com.shejiao.web.auth.AuthContextHolder;
import com.shejiao.web.domain.dto.LikeOrCollectDTO;
import com.shejiao.web.domain.entity.WebComment;
import com.shejiao.web.domain.entity.WebCommentSync;
import com.shejiao.web.domain.entity.WebLikeOrCollect;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.CommentVO;
import com.shejiao.web.domain.vo.LikeOrCollectVO;
import com.shejiao.web.mapper.WebCommentMapper;
import com.shejiao.web.mapper.WebCommentSyncMapper;
import com.shejiao.web.mapper.WebLikeOrCollectMapper;
import com.shejiao.web.mapper.WebNoteMapper;
import com.shejiao.web.mapper.WebUserMapper;
import com.shejiao.web.service.IWebEsNoteService;
import com.shejiao.web.service.IWebLikeOrCollectService;
import com.shejiao.web.domain.vo.NoteSearchVO;
import com.shejiao.web.websocket.im.ChatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 点赞/收藏
 *
 * @Author shejiao
 */
@Service
public class WebLikeOrCollectServiceImpl extends ServiceImpl<WebLikeOrCollectMapper, WebLikeOrCollect> implements IWebLikeOrCollectService {

    @Autowired
    WebUserMapper userMapper;
    @Autowired
    WebNoteMapper noteMapper;
    @Autowired
    WebCommentMapper commentMapper;
    @Autowired
    WebCommentSyncMapper commentSyncMapper;
    @Autowired
    ChatUtils chatUtils;
    @Autowired
    private IWebEsNoteService esNoteService;


    /**
     * 点赞或收藏
     *
     * @param likeOrCollectDTO 点赞收藏
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void likeOrCollectionByDTO(LikeOrCollectDTO likeOrCollectDTO) {
        String currentUid = AuthContextHolder.getUserId();
        // 点赞
        if (isLikeOrCollection(likeOrCollectDTO)) {
            this.remove(new QueryWrapper<WebLikeOrCollect>().eq("uid", currentUid).eq("like_or_collection_id", likeOrCollectDTO.getLikeOrCollectionId()).eq("type", likeOrCollectDTO.getType()));
            this.updateLikeCollectionCount(likeOrCollectDTO, -1);
        } else {
            // 点赞评论或者笔记
            WebLikeOrCollect likeOrCollection = ConvertUtils.sourceToTarget(likeOrCollectDTO, WebLikeOrCollect.class);
            likeOrCollection.setTimestamp(System.currentTimeMillis());
            likeOrCollection.setUid(currentUid);
            likeOrCollection.setCreateTime(new Date());
            likeOrCollection.setUpdateTime(new Date());
            this.save(likeOrCollection);
            this.updateLikeCollectionCount(likeOrCollectDTO, 1);
            // 不是当前用户才进行通知
            if (!likeOrCollectDTO.getPublishUid().equals(currentUid)) {
                chatUtils.sendMessage(likeOrCollectDTO.getPublishUid(), 0);
            }
        }
    }

    /**
     * 是否点赞或收藏
     *
     * @param likeOrCollectDTO 点赞收藏
     */
    @Override
    public boolean isLikeOrCollection(LikeOrCollectDTO likeOrCollectDTO) {
        String currentUid = AuthContextHolder.getUserId();
        long count = this.count(new QueryWrapper<WebLikeOrCollect>().eq("uid", currentUid).eq("like_or_collection_id", likeOrCollectDTO.getLikeOrCollectionId()).eq("type", likeOrCollectDTO.getType()));
        return count > 0;
    }

    /**
     * 获取当前用户最新的点赞和收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<LikeOrCollectVO> getNoticeLikeOrCollection(long currentPage, long pageSize) {
        Page<LikeOrCollectVO> result = new Page<>();
        String currentUid = AuthContextHolder.getUserId();

        Page<WebLikeOrCollect> likeOrCollectionPage = this.page(new Page<>((int) currentPage, (int) pageSize), new QueryWrapper<WebLikeOrCollect>().eq("publish_uid", currentUid).ne("uid", currentUid).orderByDesc("create_time"));
        List<WebLikeOrCollect> likeOrCollectionList = likeOrCollectionPage.getRecords();
        long total = likeOrCollectionPage.getTotal();
        // TODO 可以使用多线程优化
        // 得到所有用户
        Set<String> uids = likeOrCollectionList.stream().map(WebLikeOrCollect::getUid).collect(Collectors.toSet());
        Map<String, WebUser> userMap = new HashMap<>(16);
        if (!uids.isEmpty()) {
            userMap = userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));
        }
        // notes
        Set<String> nids = likeOrCollectionList.stream().filter(e -> e.getType() == 1 || e.getType() == 3).map(WebLikeOrCollect::getLikeOrCollectionId).collect(Collectors.toSet());
        Map<String, WebNote> noteMap = new HashMap<>(16);
        if (!nids.isEmpty()) {
            noteMap = noteMapper.selectBatchIds(nids).stream().collect(Collectors.toMap(WebNote::getId, note -> note));
        }
        // comments
        Set<String> cids = likeOrCollectionList.stream().filter(e -> e.getType() == 2).map(WebLikeOrCollect::getLikeOrCollectionId).collect(Collectors.toSet());
        Map<String, CommentVO> commentVoMap = new HashMap<>(16);
        if (!cids.isEmpty()) {
            List<WebComment> commentList = commentMapper.selectBatchIds(cids);
            Set<String> noteIds = commentList.stream().map(WebComment::getNid).collect(Collectors.toSet());
            Map<String, WebNote> noteMap1 = noteMapper.selectBatchIds(noteIds).stream().collect(Collectors.toMap(WebNote::getId, note -> note));

            commentList.forEach((item -> {
                CommentVO commentVo = ConvertUtils.sourceToTarget(item, CommentVO.class);
                WebNote note = noteMap1.get(item.getNid());
                commentVo.setNoteCover(note.getNoteCover());
                commentVoMap.put(String.valueOf(item.getId()), commentVo);
            }));
        }
        List<LikeOrCollectVO> likeOrCollectVOList = new ArrayList<>();
        for (WebLikeOrCollect model : likeOrCollectionList) {
            LikeOrCollectVO likeOrCollectVo = new LikeOrCollectVO();
            WebUser user = userMap.get(model.getUid());
            likeOrCollectVo.setUid(String.valueOf(user.getId()))
                    .setUsername(user.getUsername())
                    .setAvatar(user.getAvatar())
                    .setTime(model.getTimestamp())
                    .setType(model.getType());
            switch (model.getType()) {
                case 2:
                    CommentVO commentVo = commentVoMap.get(model.getLikeOrCollectionId());
                    likeOrCollectVo.setItemId(commentVo.getId())
                            .setItemCover(commentVo.getNoteCover())
                            .setContent(commentVo.getContent());
                    break;
                default:
                    WebNote note = noteMap.get(model.getLikeOrCollectionId());
                    if (note != null) {
                        likeOrCollectVo.setItemId(String.valueOf(note.getId()))
                                .setItemCover(note.getNoteCover());
                    }
                    break;
            }
            likeOrCollectVOList.add(likeOrCollectVo);
        }
        result.setRecords(likeOrCollectVOList);
        result.setTotal(total);
        return result;
    }

    /**
     * 点赞/收藏
     */
    private void updateLikeCollectionCount(LikeOrCollectDTO likeOrCollectDTO, int val) {
        switch (likeOrCollectDTO.getType()) {
            case 1:
                WebNote likeNote = noteMapper.selectById(likeOrCollectDTO.getLikeOrCollectionId());
                if (likeNote != null) {
                    // 确保点赞数不会小于0
                    long newLikeCount = likeNote.getLikeCount() + val;
                    likeNote.setLikeCount(Math.max(0L, newLikeCount));
                    noteMapper.updateById(likeNote);
                    updateElasticsearchNote(likeNote.getId(), likeNote.getLikeCount());
                }
                break;
            case 2:
                WebComment comment = commentMapper.selectById(likeOrCollectDTO.getLikeOrCollectionId());
                if (comment != null) {
                    // 确保点赞数不会小于0
                    long newLikeCount = comment.getLikeCount() + val;
                    comment.setLikeCount(Math.max(0L, newLikeCount));
                    commentMapper.updateById(comment);
                } else {
                    WebCommentSync commentSync = commentSyncMapper.selectById(likeOrCollectDTO.getLikeOrCollectionId());
                    if (commentSync != null) {
                        // 确保点赞数不会小于0
                        long newLikeCount = commentSync.getLikeCount() + val;
                        commentSync.setLikeCount(Math.max(0L, newLikeCount));
                        commentSyncMapper.updateById(commentSync);
                    }
                }
                break;
            case 3:
                WebNote collectionNote = noteMapper.selectById(likeOrCollectDTO.getLikeOrCollectionId());
                if (collectionNote != null) {
                    // 确保收藏数不会小于0
                    long newCollectionCount = collectionNote.getCollectionCount() + val;
                    collectionNote.setCollectionCount(Math.max(0L, newCollectionCount));
                    noteMapper.updateById(collectionNote);
                    updateElasticsearchNote(collectionNote.getId(), collectionNote.getLikeCount());
                }
                break;
        }
    }

    /**
     * 更新Elasticsearch中的笔记数据（独立事务，避免影响主业务）
     */
    private void updateElasticsearchNote(String noteId, Long likeCount) {
        if (esNoteService != null) {
            try {
                NoteSearchVO noteSearchVO = new NoteSearchVO();
                noteSearchVO.setId(noteId);
                noteSearchVO.setLikeCount(likeCount);
                esNoteService.updateNote(noteSearchVO);
            } catch (Exception e) {
                // 记录日志但不影响主业务
            }
        }
    }
}
