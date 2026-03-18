package com.shejiao.server.controller.web;

import com.shejiao.common.enums.Result;
import com.shejiao.web.service.IWebMentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author shejiao
 */
@RequestMapping("/web/mention")
@RestController
public class WebMentionController {

    @Autowired
    private IWebMentionService mentionService;

    /**
     * 获取当前用户的@提及通知列表
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @GetMapping("getNoticeMention/{currentPage}/{pageSize}")
    public Result<?> getNoticeMention(@PathVariable long currentPage, @PathVariable long pageSize) {
        return Result.ok(mentionService.getNoticeMention(currentPage, pageSize));
    }
}
