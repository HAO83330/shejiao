package com.shejiao.web.service;

import java.util.Map;

/**
 * 会员信息 服务层
 *
 * @Author shejiao
 */
public interface ISysVisitService {


    /**
     * 获取今日网站访问人数
     */
    int getWebVisitCount();
    
    /**
     * 获取指定月份网站访问人数
     */
    int getWebVisitCount(String month);

    /**
     * 获取近30天的访问量
     *
     * @return {
     * date: ["01-20","01-21",...,"02-18"]
     * pv: [10,5,6,7,5,3,2,...]
     * uv: [5,3,4,4,5,2,1,...]
     * }
     * 注：PV表示访问量   UV表示独立用户数
     */
    Map<String, Object> getVisitByWeek();
    
    /**
     * 获取指定月份的访问量
     */
    Map<String, Object> getVisitByWeek(String month);

}
