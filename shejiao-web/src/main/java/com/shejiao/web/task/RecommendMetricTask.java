package com.shejiao.web.task;

import com.shejiao.web.service.impl.WebRecommendMetricServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 推荐算法指标计算定时任务
 */
@Component
@Slf4j
public class RecommendMetricTask {

    @Autowired
    private WebRecommendMetricServiceImpl recommendMetricService;

    /**
     * 每小时计算一次推荐算法指标
     *  cron表达式：0 0 * * * ? 表示每小时的第0分0秒执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calculateRecommendMetrics() {
        log.info("定时任务：开始计算推荐算法指标");
        try {
            recommendMetricService.calculateMetrics();
            log.info("定时任务：推荐算法指标计算完成");
        } catch (Exception e) {
            log.error("定时任务：计算推荐算法指标失败", e);
        }
    }

    /**
     * 启动时执行一次推荐算法指标计算
     */
    // @PostConstruct
    // public void initCalculate() {
    //     log.info("初始化：开始计算推荐算法指标");
    //     try {
    //         recommendMetricService.calculateMetrics();
    //         log.info("初始化：推荐算法指标计算完成");
    //     } catch (Exception e) {
    //         log.error("初始化：计算推荐算法指标失败", e);
    //     }
    // }
}
