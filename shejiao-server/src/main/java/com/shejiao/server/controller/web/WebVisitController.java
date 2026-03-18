package com.shejiao.server.controller.web;

import com.shejiao.common.enums.Result;
import com.shejiao.web.domain.entity.WebVisit;
import com.shejiao.web.mapper.SysVisitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 用户访问行为记录
 *
 * @Author shejiao
 */
@RequestMapping("/web/visit")
@RestController
public class WebVisitController {

    @Autowired
    private SysVisitMapper visitMapper;

    /**
     * 记录用户浏览行为
     *
     * @param visit 访问记录实体
     */
    @PostMapping("record")
    public Result<?> recordVisit(@RequestBody WebVisit visit) {
        visit.setCreateTime(new Date());
        visit.setUpdateTime(new Date());
        visit.setStatus(1);
        visitMapper.insert(visit);
        return Result.ok();
    }

    /**
     * 记录用户点击文章
     *
     * @param userUid 用户ID
     * @param noteUid 文章ID
     */
    @PostMapping("clickNote")
    public Result<?> clickNote(@RequestParam String userUid, @RequestParam String noteUid) {
        WebVisit visit = new WebVisit();
        visit.setUserUid(userUid);
        visit.setModuleUid(noteUid);
        visit.setBehavior("点击了文章");
        visit.setCreateTime(new Date());
        visit.setUpdateTime(new Date());
        visit.setStatus(1);
        visitMapper.insert(visit);
        return Result.ok();
    }

    /**
     * 记录用户搜索行为
     *
     * @param userUid 用户ID
     * @param keyword 搜索关键词
     */
    @PostMapping("search")
    public Result<?> search(@RequestParam String userUid, @RequestParam String keyword) {
        WebVisit visit = new WebVisit();
        visit.setUserUid(userUid);
        visit.setOtherData(keyword);
        visit.setBehavior("进行了搜索");
        visit.setCreateTime(new Date());
        visit.setUpdateTime(new Date());
        visit.setStatus(1);
        visitMapper.insert(visit);
        return Result.ok();
    }

    /**
     * 记录用户点击标签
     *
     * @param userUid 用户ID
     * @param tagUid 标签ID
     */
    @PostMapping("clickTag")
    public Result<?> clickTag(@RequestParam String userUid, @RequestParam String tagUid) {
        WebVisit visit = new WebVisit();
        visit.setUserUid(userUid);
        visit.setModuleUid(tagUid);
        visit.setBehavior("点击了标签");
        visit.setCreateTime(new Date());
        visit.setUpdateTime(new Date());
        visit.setStatus(1);
        visitMapper.insert(visit);
        return Result.ok();
    }

    /**
     * 记录用户点击分类
     *
     * @param userUid 用户ID
     * @param categoryUid 分类ID
     */
    @PostMapping("clickCategory")
    public Result<?> clickCategory(@RequestParam String userUid, @RequestParam String categoryUid) {
        WebVisit visit = new WebVisit();
        visit.setUserUid(userUid);
        visit.setModuleUid(categoryUid);
        visit.setBehavior("点击了分类");
        visit.setCreateTime(new Date());
        visit.setUpdateTime(new Date());
        visit.setStatus(1);
        visitMapper.insert(visit);
        return Result.ok();
    }
}
