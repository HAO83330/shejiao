package com.shejiao.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shejiao.common.annotation.Excel;
import com.shejiao.web.domain.shejiaoBaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 用户
 *
 * @Author shejiao
 */
@Data
@TableName("web_user")
public class WebUser extends shejiaoBaseEntity {

    /**
     * 红薯ID
     */
    @Excel(name = "红薯ID", sort = 1)
    private Long hsId;

    /**
     * 用户名
     */
    @Excel(name = "用户名", sort = 2)
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    @Excel(name = "头像", sort = 3)
    private String avatar;

    /**
     * 性别
     */
    @Excel(name = "性别", sort = 4)
    private String gender;

    /**
     * 电话
     */
    @Excel(name = "电话", sort = 5)
    private String phone;

    /**
     * email
     */
    @Excel(name = "邮箱", sort = 6)
    private String email;

    /**
     * 描述
     */
    @Excel(name = "描述", sort = 7)
    private String description;

    /**
     * 用户状态
     */
    @Excel(name = "状态", sort = 8)
    private String status;

    /**
     * 生日
     */
    @Excel(name = "生日", sort = 9)
    private String birthday;

    /**
     * 地址
     */
    @Excel(name = "地址", sort = 10)
    private String address;

    /**
     * 用户封面
     */
    @Excel(name = "用户封面", sort = 11)
    private String userCover;

    /**
     * 用户标签
     */
    @Excel(name = "用户标签", sort = 12)
    private String tags;

    /**
     * 笔记数量
     */
    @Excel(name = "笔记数量", sort = 13)
    private Long trendCount;

    /**
     * 关注数量
     */
    @Excel(name = "关注数量", sort = 14)
    private Long followerCount;

    /**
     * 粉丝数量
     */
    @Excel(name = "粉丝数量", sort = 15)
    private Long fanCount;

    /**
     * 最后登录IP
     */
    @Excel(name = "最后登录IP", sort = 16)
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Excel(name = "最后登录时间", sort = 17, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    /**
     * 备注
     */
    @Excel(name = "备注", sort = 18)
    private String remark;
}
