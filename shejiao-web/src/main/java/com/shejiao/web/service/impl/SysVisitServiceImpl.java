package com.shejiao.web.service.impl;

import com.shejiao.common.constant.Constantss;
import com.shejiao.common.global.RedisConf;
import com.shejiao.common.utils.DateUtilss;
import com.shejiao.common.utils.JsonUtils;
import com.shejiao.common.utils.RedisUtil;
import com.shejiao.web.mapper.SysLoginInforMapper;
import com.shejiao.web.mapper.SysVisitMapper;
import com.shejiao.web.service.ISysVisitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 会员信息 服务层处理
 *
 * @Author shejiao
 */
@Slf4j
@Service
public class SysVisitServiceImpl implements ISysVisitService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SysVisitMapper visitMapper;
    @Autowired
    private SysLoginInforMapper loginInforMapper;


    @Override
    public int getWebVisitCount() {
        // 获取今日开始和结束时间
        String startTime = DateUtilss.getToDayStartTime();
        String endTime = DateUtilss.getToDayEndTime();
//        return visitMapper.getIpCount(startTime, endTime);
        return loginInforMapper.getIpCount(startTime, endTime);
    }

    @Override
    public Map<String, Object> getVisitByWeek() {
        // 从Redis中获取访问量
        String cacheKey = RedisConf.DASHBOARD + Constantss.SYMBOL_COLON + "MONTH_VISIT";
//        String weekVisitJson = redisUtil.get(cacheKey);
//        if (StringUtilss.isNotEmpty(weekVisitJson)) {
//            Map<String, Object> weekVisitMap = JsonUtils.jsonToMap(weekVisitJson);
//            return weekVisitMap;
//        }

        // 获取到今天结束的时间
        String todayEndTime = DateUtilss.getToDayEndTime();
        // 获取最近30天的日期
        Date thirtyDaysDate = DateUtilss.getDate(todayEndTime, -29);
        String thirtyDays = DateUtilss.getOneDayStartTime(thirtyDaysDate);
        // 获取最近30天的数组列表
        List<String> thirtyDaysList = DateUtilss.getDaysByN(30, "yyyy-MM-dd");
        // 获得最近30天的访问量
        List<Map<String, Object>> pvMap = visitMapper.getPVByWeek(thirtyDays, todayEndTime);
        // 获得最近30天的独立用户
        List<Map<String, Object>> uvMap = visitMapper.getUVByWeek(thirtyDays, todayEndTime);

        Map<String, Object> countPVMap = new HashMap<>();
        Map<String, Object> countUVMap = new HashMap<>();

        for (Map<String, Object> item : pvMap) {
            countPVMap.put(item.get("DATE").toString(), item.get("COUNT"));
        }
        for (Map<String, Object> item : uvMap) {
            countUVMap.put(item.get("DATE").toString(), item.get("COUNT"));
        }
        // 访问量数组
        List<Integer> pvList = new ArrayList<>();
        // 独立用户数组
        List<Integer> uvList = new ArrayList<>();

        for (String day : thirtyDaysList) {
            if (countPVMap.get(day) != null) {
                Number pvNumber = (Number) countPVMap.get(day);
                Number uvNumber = (Number) countUVMap.get(day);
                pvList.add(pvNumber.intValue());
                uvList.add(uvNumber != null ? uvNumber.intValue() : 0);
            } else {
                pvList.add(0);
                uvList.add(0);
            }
        }
        Map<String, Object> resultMap = new HashMap<>(Constantss.NUM_THREE);
        // 不含年份的数组格式
        List<String> resultThirtyDaysList = DateUtilss.getDaysByN(30, "MM-dd");
        resultMap.put("date", resultThirtyDaysList);
        resultMap.put("pv", pvList);
        resultMap.put("uv", uvList);

        //TODO 可能会存在短期的数据不一致的问题，即零点时不能准时更新，而是要在0:10才会重新刷新纪录。 后期考虑加入定时器处理这个问题
        // 将访问量存入Redis中【过期时间10分钟】
        redisUtil.setEx(cacheKey, JsonUtils.objectToJson(resultMap), 10, TimeUnit.MINUTES);
        return resultMap;
    }
}
