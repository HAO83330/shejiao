package com.shejiao.web.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shejiao.common.constant.AuthConstant;
import com.shejiao.common.core.domain.Query;
import com.shejiao.common.global.BaseSQLConf;
import com.shejiao.common.utils.RandomUtil;
import com.shejiao.common.utils.StringUtils;
import com.shejiao.common.utils.ip.AddressUtils;
import com.shejiao.common.utils.ip.IpUtils;
import com.shejiao.common.validator.ValidatorUtil;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.mapper.SysMemberMapper;
import com.shejiao.web.service.ISysMemberService;
import com.shejiao.web.service.IWebOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 会员信息 服务层处理
 *
 * @Author shejiao
 */
@Slf4j
@Service
public class SysMemberServiceImpl implements ISysMemberService {

    @Autowired
    private SysMemberMapper memberMapper;
    @Autowired
    private IWebOssService ossService;


    /**
     * 查询会员信息集合
     *
     * @param query 会员信息
     */
    @Override
    public List<WebUser> selectMemberList(Query query) {
        QueryWrapper<WebUser> qw = new QueryWrapper<>();
        qw.lambda().like(ValidatorUtil.isNotNull(query.getHsId()), WebUser::getHsId, query.getHsId());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getUsername()), WebUser::getUsername, query.getUsername());
        qw.lambda().like(ValidatorUtil.isNotNull(query.getPhone()), WebUser::getPhone, query.getPhone());
        
        // 处理状态参数，使用精确匹配
        if (ValidatorUtil.isNotNull(query.getStatus())) {
            qw.lambda().eq(WebUser::getStatus, String.valueOf(query.getStatus()));
        }
        
        // 处理日期范围参数
        Object paramsObj = query.get("params");
        if (paramsObj instanceof Map) {
            Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;
            qw.lambda().ge(ValidatorUtil.isNotNull(paramsMap.get("beginTime")), WebUser::getCreateTime, paramsMap.get("beginTime"));
            qw.lambda().le(ValidatorUtil.isNotNull(paramsMap.get("endTime")), WebUser::getCreateTime, paramsMap.get("endTime"));
        } else {
            // 兼容旧的参数格式
            qw.lambda().ge(ValidatorUtil.isNotNull(query.get("params[beginTime]")), WebUser::getCreateTime, query.get("params[beginTime]"));
            qw.lambda().le(ValidatorUtil.isNotNull(query.get("params[endTime]")), WebUser::getCreateTime, query.get("params[endTime]"));
        }
        
        qw.orderByDesc("create_time");
        return memberMapper.selectList(qw);
    }

    /**
     * 查询所有会员
     */
    @Override
    public List<WebUser> selectMemberAll() {
        return memberMapper.selectList(new QueryWrapper<>());
    }

    /**
     * 通过会员ID查询会员信息
     *
     * @param id 会员ID
     */
    @Override
    public WebUser selectMemberById(Long id) {
        return memberMapper.selectById(id);
    }

    /**
     * 新增会员信息
     *
     * @param user 会员信息
     */
    @Override
    public int insertMember(WebUser user, MultipartFile file) {
        // 上传头像
        if (ObjectUtils.isNotEmpty(file)) {
            String avatar = ossService.save(file);
            user.setAvatar(avatar);
        } else {
            // 设置默认头像
            user.setAvatar(AuthConstant.DEFAULT_AVATAR);
        }
        
        // 生成默认用户名
        if (StringUtils.isEmpty(user.getUsername())) {
            String defaultUsername = "社交平台";
            String username = defaultUsername;
            int count = 1;
            
            // 检查用户名是否存在
            while (memberMapper.selectCount(new QueryWrapper<WebUser>().eq("username", username)) > 0) {
                username = defaultUsername + count;
                count++;
            }
            
            user.setUsername(username);
        }
        
        user.setHsId(Long.valueOf(RandomUtil.randomNumbers(10)));
        user.setPassword(SecureUtil.md5(user.getPassword()));
        user.setLoginIp(IpUtils.getIpAddr());
        user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
        user.setCreator("System");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        return memberMapper.insert(user);
    }

    /**
     * 修改保存会员信息
     *
     * @param user 会员信息
     */
    @Override
    public int updateMember(WebUser user, MultipartFile file) {
        WebUser webUser = memberMapper.selectById(user.getId());
        // 更新头像
        if (ObjectUtils.isNotEmpty(file)) {
            String avatar = ossService.save(file);
            user.setAvatar(avatar);
        }
        String newPassword = webUser.getPassword();
        String originPassword = SecureUtil.md5(user.getPassword());
        // 确认更换了新密码
        if (!originPassword.equals(newPassword)) {
            user.setPassword(SecureUtil.md5(user.getPassword()));
        }
        user.setPassword(webUser.getPassword());
        user.setLoginIp(IpUtils.getIpAddr());
        user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
        user.setUpdater("System");
        user.setUpdateTime(new Date());
        return memberMapper.updateById(user);
    }

    /**
     * 删除会员信息
     *
     * @param id 会员ID
     */
    @Override
    public int deleteMemberById(Long id) {
        return memberMapper.deleteById(id);
    }

    /**
     * 批量删除会员信息
     *
     * @param ids 需要删除的会员ID
     */
    @Override
    public int deleteMemberByIds(Long[] ids) {
        List<Long> longs = Arrays.asList(ids);
        for (Long id : ids) {
            WebUser user = selectMemberById(id);
            if (ValidatorUtil.isNull(user)) {
                log.info("用户不存在:{}", id);
                longs.remove(id);
            }
        }
        return memberMapper.deleteBatchIds(longs);
    }

    @Override
    public Integer getMemberCount(int status) {
        QueryWrapper<WebUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BaseSQLConf.STATUS, status);
        return Math.toIntExact(memberMapper.selectCount(queryWrapper));
    }

    @Override
    public Map<String, Object> getUserGrowthTrend(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> totalUsers = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        // 获取今天的结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endDate = calendar.getTime();

        // 获取指定天数前的开始时间
        calendar.add(Calendar.DAY_OF_MONTH, -(days - 1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();

        // 查询所有用户
        List<WebUser> allUsers = memberMapper.selectList(new QueryWrapper<WebUser>()
                .ge("create_time", startDate)
                .le("create_time", endDate)
                .orderByAsc("create_time"));

        // 按日期统计
        Map<String, Integer> dailyCount = new HashMap<>();
        for (WebUser user : allUsers) {
            String dateStr = sdf.format(user.getCreateTime());
            dailyCount.put(dateStr, dailyCount.getOrDefault(dateStr, 0) + 1);
        }

        // 生成日期列表和统计数据
        calendar.setTime(startDate);
        int cumulativeCount = 0;

        // 获取开始日期之前的累计用户数
        QueryWrapper<WebUser> beforeQuery = new QueryWrapper<>();
        beforeQuery.lt("create_time", startDate);
        cumulativeCount = Math.toIntExact(memberMapper.selectCount(beforeQuery));

        for (int i = 0; i < days; i++) {
            String dateStr = sdf.format(calendar.getTime());
            dates.add(dateStr);

            int count = dailyCount.getOrDefault(dateStr, 0);
            newUsers.add(count);

            cumulativeCount += count;
            totalUsers.add(cumulativeCount);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("totalUsers", totalUsers);

        return result;
    }

    @Override
    public Integer getMemberCount(int status, String month) {
        if (month == null || month.isEmpty()) {
            return getMemberCount(status);
        }
        
        // 根据月份参数查询对应的数据
        // 构建月份的开始和结束时间
        String startTime = month + "-01 00:00:00";
        int daysInMonth = java.time.YearMonth.parse(month).lengthOfMonth();
        String endTime = month + "-" + daysInMonth + " 23:59:59";
        
        QueryWrapper<WebUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BaseSQLConf.STATUS, status)
                   .ge("create_time", startTime)
                   .le("create_time", endTime);
        return Math.toIntExact(memberMapper.selectCount(queryWrapper));
    }

    @Override
    public Map<String, Object> getUserGrowthTrend(int days, String month) {
        if (month == null || month.isEmpty()) {
            return getUserGrowthTrend(days);
        }
        
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> totalUsers = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        // 构建月份的开始和结束时间
        String monthStart = month + "-01";
        String monthEnd = month + "-" + java.time.YearMonth.parse(month).lengthOfMonth();
        
        try {
            Date startDate = sdf.parse(monthStart);
            Date endDate = sdf.parse(monthEnd);
            
            // 查询指定月份的所有用户
            List<WebUser> allUsers = memberMapper.selectList(new QueryWrapper<WebUser>()
                    .ge("create_time", startDate)
                    .le("create_time", endDate)
                    .orderByAsc("create_time"));

            // 按日期统计
            Map<String, Integer> dailyCount = new HashMap<>();
            for (WebUser user : allUsers) {
                String dateStr = sdf.format(user.getCreateTime());
                dailyCount.put(dateStr, dailyCount.getOrDefault(dateStr, 0) + 1);
            }

            // 生成日期列表和统计数据
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int cumulativeCount = 0;

            // 获取开始日期之前的累计用户数
            QueryWrapper<WebUser> beforeQuery = new QueryWrapper<>();
            beforeQuery.lt("create_time", startDate);
            cumulativeCount = Math.toIntExact(memberMapper.selectCount(beforeQuery));

            int daysInMonth = java.time.YearMonth.parse(month).lengthOfMonth();
            for (int i = 0; i < daysInMonth; i++) {
                String dateStr = sdf.format(calendar.getTime());
                dates.add(dateStr);

                int count = dailyCount.getOrDefault(dateStr, 0);
                newUsers.add(count);

                cumulativeCount += count;
                totalUsers.add(cumulativeCount);

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            log.error("获取用户增长趋势失败", e);
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("totalUsers", totalUsers);

        return result;
    }
}
