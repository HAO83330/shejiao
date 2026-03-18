package com.shejiao.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.common.utils.ConvertUtils;
import com.shejiao.web.auth.AuthContextHolder;
import com.shejiao.web.domain.entity.WebMention;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.MentionVO;
import com.shejiao.web.mapper.WebMentionMapper;
import com.shejiao.web.mapper.WebNoteMapper;
import com.shejiao.web.mapper.WebUserMapper;
import com.shejiao.web.service.IWebMentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author shejiao
 */
@Service
public class WebMentionServiceImpl extends ServiceImpl<WebMentionMapper, WebMention> implements IWebMentionService {

    @Autowired
    WebMentionMapper mentionMapper;
    @Autowired
    WebUserMapper userMapper;
    @Autowired
    WebNoteMapper noteMapper;

    @Override
    public List<MentionVO> getNoticeMention(long currentPage, long pageSize) {
        String currentUid = AuthContextHolder.getUserId();
        List<WebMention> mentionList = mentionMapper.selectList(new QueryWrapper<WebMention>().eq("uid", currentUid).orderByDesc("create_time"));

        List<MentionVO> mentionVOList = new ArrayList<>();
        if (!mentionList.isEmpty()) {
            // 获取所有@人的用户ID
            Set<String> mentionUids = mentionList.stream().map(WebMention::getMentionUid).collect(Collectors.toSet());
            Map<String, WebUser> userMap = userMapper.selectBatchIds(mentionUids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));

            // 获取所有笔记ID
            Set<String> noteIds = mentionList.stream().map(WebMention::getNid).collect(Collectors.toSet());
            Map<String, WebNote> noteMap = noteMapper.selectBatchIds(noteIds).stream().collect(Collectors.toMap(WebNote::getId, note -> note));

            for (WebMention mention : mentionList) {
                WebNote note = noteMap.get(mention.getNid());
                // 只显示审核通过的笔记的@提及通知
                if (note != null && "1".equals(note.getAuditStatus())) {
                    MentionVO mentionVo = ConvertUtils.sourceToTarget(mention, MentionVO.class);
                    WebUser user = userMap.get(mention.getMentionUid());
                    if (user != null) {
                        mentionVo.setUsername(user.getUsername());
                        mentionVo.setAvatar(user.getAvatar());
                    }
                    mentionVo.setNoteCover(note.getNoteCover());
                    mentionVo.setTime(mention.getCreateTime().getTime());
                    mentionVOList.add(mentionVo);
                }
            }
        }
        return mentionVOList;
    }
}
