package com.shejiao.server.controller.web;

import com.shejiao.common.enums.Result;
import com.shejiao.common.validator.myVaildator.noLogin.NoLoginIntercept;
import com.shejiao.web.domain.dto.EsRecordDTO;
import com.shejiao.web.service.IWebEsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ES
 *
 * @Author shejiao
 */
@RequestMapping("/web/es/record")
@RestController
public class WebEsRecordController {

    @Autowired
    private IWebEsRecordService esRecordService;


    /**
     * 获取搜索记录
     */
    @GetMapping("getRecordByKeyWord")
    public Result<?> getRecordByKeyWord(EsRecordDTO esRecordDTO) {
        return Result.ok(esRecordService.getRecordByKeyWord(esRecordDTO));
    }

    /**
     * 热门关键词
     */
    @NoLoginIntercept
    @GetMapping("getHotRecord")
    public Result<?> getHotRecord() {
        return Result.ok(esRecordService.getHotRecord());
    }

    /**
     * 增加搜索记录
     */
    @PostMapping("addRecord")
    public Result<?> addRecord(@RequestBody EsRecordDTO esRecordDTO) {
        esRecordService.addRecord(esRecordDTO);
        return Result.ok();
    }

    /**
     * 删除搜索记录
     */
    @PostMapping("clearRecordByUser")
    public Result<?> clearRecordByUser(@RequestBody EsRecordDTO esRecordDTO) {
        esRecordService.clearRecordByUser(esRecordDTO);
        return Result.ok();
    }

    /**
     * 获取个性化推荐搜索词（猜你想搜）
     */
    @NoLoginIntercept
    @GetMapping("getGuessYouWant")
    public Result<?> getGuessYouWant(String uid) {
        return Result.ok(esRecordService.getGuessYouWant(uid));
    }
}
