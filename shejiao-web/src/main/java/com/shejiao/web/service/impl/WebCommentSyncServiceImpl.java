package com.shejiao.web.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.web.domain.entity.WebCommentSync;
import com.shejiao.web.mapper.WebCommentSyncMapper;
import com.shejiao.web.service.IWebCommentSyncService;
import org.springframework.stereotype.Service;

/**
 * @Author shejiao
 */
@Service
public class WebCommentSyncServiceImpl extends ServiceImpl<WebCommentSyncMapper, WebCommentSync> implements IWebCommentSyncService {
}
