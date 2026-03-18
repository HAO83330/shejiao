package com.shejiao.web.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.common.constant.UserConstant;
import com.shejiao.common.enums.ResultCodeEnum;
import com.shejiao.common.exception.web.shejiaoException;
import com.shejiao.common.utils.ConvertUtils;
import com.shejiao.common.utils.WebUtils;
import com.shejiao.web.auth.AuthContextHolder;
import com.shejiao.web.domain.dto.NoteDTO;
import com.shejiao.web.domain.entity.WebComment;
import com.shejiao.web.domain.entity.WebCommentSync;
import com.shejiao.web.domain.entity.WebLikeOrCollect;
import com.shejiao.web.domain.entity.WebNavbar;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.domain.entity.WebTag;
import com.shejiao.web.domain.entity.WebTagNoteRelation;
import com.shejiao.web.domain.entity.WebMention;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.NoteVO;
import com.shejiao.web.mapper.WebNoteMapper;
import com.shejiao.web.mapper.WebUserMapper;
import com.shejiao.web.service.*;
import com.shejiao.web.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author shejiao
 */
@Service
public class WebNoteServiceImpl extends ServiceImpl<WebNoteMapper, WebNote> implements IWebNoteService {

    @Autowired
    IWebUserService userService;
    @Autowired
    private WebUserMapper userMapper;
    @Autowired
    IWebTagNoteRelationService tagNoteRelationService;
    @Autowired
    IWebTagService tagService;
    @Autowired
    IWebNavbarService categoryService;
    @Autowired
    IWebEsNoteService esNoteService;
    @Autowired
    IWebFollowService followerService;
    @Autowired
    IWebLikeOrCollectService likeOrCollectionService;
    @Autowired
    IWebCommentService commentService;
    @Autowired
    IWebCommentSyncService commentSyncService;
    @Autowired
    private IWebOssService ossService;
    @Autowired
    WebNoteMapper noteMapper;
    @Autowired
    com.shejiao.web.websocket.im.ChatUtils chatUtils;
    @Autowired
    IWebMentionService mentionService;


    @NotNull
    private StringBuilder getTags(WebNote note, NoteDTO noteDTO) {
        List<String> tagList = noteDTO.getTagList();
        List<WebTagNoteRelation> tagNoteRelationList = new ArrayList<>();
        List<WebTag> tagList1 = tagService.list();
        Map<String, WebTag> tagMap = tagList1.stream().collect(Collectors.toMap(WebTag::getTitle, tag -> tag));
        StringBuilder tags = new StringBuilder();
        if (!tagList.isEmpty()) {
            for (String tag : tagList) {
                WebTagNoteRelation tagNoteRelation = new WebTagNoteRelation();
                if (tagMap.containsKey(tag)) {
                    WebTag tagModel = tagMap.get(tag);
                    tagNoteRelation.setTid(String.valueOf(tagModel.getId()));
                } else {
                    WebTag model = new WebTag();
                    model.setTitle(tag);
                    model.setLikeCount(1L);
                    tagService.save(model);
                    tagNoteRelation.setTid(String.valueOf(model.getId()));
                }
                tagNoteRelation.setNid(String.valueOf(note.getId()));
                tagNoteRelationList.add(tagNoteRelation);
                tags.append(tag);
            }
            tagNoteRelationService.saveBatch(tagNoteRelationList);
        }
        return tags;
    }

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    @Override
    public NoteVO getNoteById(String noteId) {
        WebNote note = this.getById(noteId);
        if (note == null) {
            throw new shejiaoException(ResultCodeEnum.FAIL);
        }
        // 检查审核状态，只有笔记作者可以查看审核中的笔记
        if ("0".equals(note.getAuditStatus())) {
            String currentUid = AuthContextHolder.getUserId();
            if (!currentUid.equals(note.getUid())) {
                throw new shejiaoException("笔记正在审核中，暂不可查看", ResultCodeEnum.FAIL.getCode());
            }
        }
        note.setViewCount(note.getViewCount() + 1);
        WebUser user = userService.getById(note.getUid());
        NoteVO noteVo = ConvertUtils.sourceToTarget(note, NoteVO.class);
        noteVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(note.getUpdateTime().getTime())
                .setAuditStatus(note.getAuditStatus());

        boolean follow = followerService.isFollow(String.valueOf(user.getId()));
        noteVo.setIsFollow(follow);

        String currentUid = AuthContextHolder.getUserId();
        List<WebLikeOrCollect> likeOrCollectionList = likeOrCollectionService.list(new QueryWrapper<WebLikeOrCollect>().eq("like_or_collection_id", noteId).eq("uid", currentUid));

        Set<Integer> types = likeOrCollectionList.stream().map(WebLikeOrCollect::getType).collect(Collectors.toSet());
        noteVo.setIsLike(types.contains(1));
        noteVo.setIsCollection(types.contains(3));


        //得到标签
        List<WebTagNoteRelation> tagNoteRelationList = tagNoteRelationService.list(new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId));
        List<String> tids = tagNoteRelationList.stream().map(WebTagNoteRelation::getTid).collect(Collectors.toList());

        if (!tids.isEmpty()) {
            List<WebTag> tagList = tagService.listByIds(tids);
            noteVo.setTagList(tagList);
        }

        this.updateById(note);
        return noteVo;
    }

    /**
     * 新增笔记
     *
     * @param noteData 笔记对象
     * @param files    图片文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveNoteByDTO(String noteData, MultipartFile[] files) {

        String currentUid = AuthContextHolder.getUserId();
        // 更新用户笔记数量
        WebUser user = userMapper.selectById(currentUid);
        user.setTrendCount(user.getTrendCount() + 1);
        userMapper.updateById(user);

        // 保存笔记
        NoteDTO noteDTO = JSONUtil.toBean(noteData, NoteDTO.class);
        WebNote note = ConvertUtils.sourceToTarget(noteDTO, WebNote.class);
        note.setUid(currentUid);
        note.setAuthor(user.getUsername());
        note.setAuditStatus("0");
        note.setNoteType("0");
        note.setCreator(user.getUsername());
        note.setTime(System.currentTimeMillis());
        note.setCreateTime(new Date());
        note.setUpdateTime(new Date());

        // 批量上传图片
        List<String> dataList = null;
        try {
            dataList = ossService.saveBatch(files);
        } catch (Exception e) {
            log.error("图片上传失败");
            e.printStackTrace();
        }
        String[] urlArr = Objects.requireNonNull(dataList).toArray(new String[dataList.size()]);
        String urls = JSONUtil.toJsonStr(urlArr);
        note.setUrls(urls);
        note.setNoteCover(urlArr[0]);
        this.save(note);

        // 检测@提及并存储@提及记录（待审核通过后发送通知）
        String content = noteDTO.getContent();
        if (content != null && content.contains("@")) {
            // 提取@提及的用户名
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@([\u4e00-\u9fa5a-zA-Z0-9_]+)");
            java.util.regex.Matcher matcher = pattern.matcher(content);
            java.util.Set<String> mentionedUsernames = new java.util.HashSet<>();
            while (matcher.find()) {
                mentionedUsernames.add(matcher.group(1));
            }

            // 存储@提及记录（待审核通过后发送通知）
            for (String username : mentionedUsernames) {
                WebUser mentionedUser = userMapper.selectOne(new QueryWrapper<WebUser>().eq("username", username));
                if (mentionedUser != null && !mentionedUser.getId().equals(currentUid)) {
                    // 存储@提及记录
                    WebMention mention = new WebMention();
                    mention.setNid(note.getId());
                    mention.setNoteUid(currentUid);
                    mention.setUid(mentionedUser.getId());
                    mention.setMentionUid(currentUid);
                    mention.setContent(content);
                    mention.setCreator(user.getUsername());
                    mention.setCreateTime(new Date());
                    mentionService.save(mention);
                }
            }
        }

        return note.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoteByIds(List<String> noteIds) {
        String currentUid = AuthContextHolder.getUserId();
        List<WebNote> noteList = this.listByIds(noteIds);
        // 检查是否是笔记的作者
        for (WebNote note : noteList) {
            if (!currentUid.equals(note.getUid())) {
                throw new shejiaoException("无权限删除该笔记", ResultCodeEnum.FAIL.getCode());
            }
        }
        // TODO 这里需要优化，数据一致性问题
        noteList.forEach(item -> {
            String noteId = item.getId();
            esNoteService.deleteNote(noteId);

            String urls = item.getUrls();
            JSONArray objects = JSONUtil.parseArray(urls);
            Object[] array = objects.toArray();
            List<String> pathArr = new ArrayList<>();
            for (Object o : array) {
                pathArr.add((String) o);
            }
            try {
                ossService.batchDelete(pathArr);
            } catch (Exception e) {
                log.error("删除图片文件失败，笔记ID: " + noteId, e);
                // 继续执行，不中断整个删除流程
            }
            // TODO 可以使用多线程优化，
            // 删除点赞图片，评论，标签关系，收藏关系
            likeOrCollectionService.remove(new QueryWrapper<WebLikeOrCollect>().eq("like_or_collection_id", noteId));
            List<WebComment> commentList = commentService.list(new QueryWrapper<WebComment>().eq("nid", noteId));
            List<WebCommentSync> commentSyncList = commentSyncService.list(new QueryWrapper<WebCommentSync>().eq("nid", noteId));
            List<String> cids = commentList.stream().map(WebComment::getId).collect(Collectors.toList());
            List<String> cids2 = commentSyncList.stream().map(WebCommentSync::getId).collect(Collectors.toList());
            if (!cids.isEmpty()) {
                likeOrCollectionService.remove(new QueryWrapper<WebLikeOrCollect>().in("like_or_collection_id", cids).eq("type", 2));
            }
            commentService.removeBatchByIds(cids);
            commentSyncService.removeBatchByIds(cids2);
            tagNoteRelationService.remove(new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId));
        });
        this.removeBatchByIds(noteIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNoteByDTO(String noteData, MultipartFile[] files) {
        String currentUid = AuthContextHolder.getUserId();
        NoteDTO noteDTO = JSONUtil.toBean(noteData, NoteDTO.class);
        WebNote note = ConvertUtils.sourceToTarget(noteDTO, WebNote.class);
        // 检查是否是笔记的作者
        WebNote existingNote = this.getById(note.getId());
        if (!currentUid.equals(existingNote.getUid())) {
            throw new shejiaoException("无权限更新该笔记", ResultCodeEnum.FAIL.getCode());
        }
        note.setUid(currentUid);
        boolean flag = this.updateById(note);
        if (!flag) {
            return;
        }
        WebNavbar category = categoryService.getById(note.getCid());
        WebNavbar parentCategory = categoryService.getById(note.getCpid());
        List<String> dataList = null;
        try {
            dataList = ossService.saveBatch(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 删除原来图片的地址
        String urls = note.getUrls();
        JSONArray objects = JSONUtil.parseArray(urls);
        Object[] array = objects.toArray();
        List<String> pathArr = new ArrayList<>();
        for (Object o : array) {
            pathArr.add((String) o);
        }
        ossService.batchDelete(pathArr);

        String[] urlArr = Objects.requireNonNull(dataList).toArray(new String[dataList.size()]);
        String newUrls = JSONUtil.toJsonStr(urlArr);
        note.setUrls(newUrls);
        note.setNoteCover(urlArr[0]);
        note.setAuditStatus("0");
        note.setTime(System.currentTimeMillis());
        note.setNoteType("0");
        note.setUpdateTime(new Date());
        this.updateById(note);

        // 删除原来的标签绑定关系
        tagNoteRelationService.remove(new QueryWrapper<WebTagNoteRelation>().eq("nid", note.getId()));
        // 重新绑定标签关系
        StringBuilder tags = getTags(note, noteDTO);

        esNoteService.deleteNote(note.getId());

//        WebUser user = userService.getById(currentUid);
//
//        NoteSearchVo noteSearchVo = ConvertUtils.sourceToTarget(note, NoteSearchVo.class);
//        noteSearchVo.setUsername(user.getUsername())
//                .setAvatar(user.getAvatar())
//                .setCategoryName(category.getTitle())
//                .setCategoryParentName(parentCategory.getTitle())
//                .setTags(tags.toString())
//                .setTime(note.getUpdateTime().getTime());
//        esNoteService.updateNote(noteSearchVo);
    }

    @Override
    public Page<NoteVO> getHotPage(long currentPage, long pageSize) {
        return null;
    }

    @Override
    public boolean pinnedNote(String noteId) {
        String currentUid = AuthContextHolder.getUserId();
        WebNote note = this.getById(noteId);
        if ("1".equals(note.getPinned())) {
            note.setPinned("0");
        } else {
            List<WebNote> noteList = this.list(new QueryWrapper<WebNote>().eq("uid", currentUid));
            long count = noteList.stream().filter(item -> "1".equals(item.getPinned())).count();
            if (count >= 3) {
                throw new shejiaoException("最多只能置顶3个笔记");
            }
            note.setPinned("1");
            note.setUpdateTime(new Date());
        }
        return this.updateById(note);
    }
}
