package com.shejiao.web.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
public class UserVO implements Serializable {

    private String id;

    private Long hsId;

    private String username;

    private String password;

    private String avatar;

    private String gender;

    private String phone;

    private String email;

    private String description;

    private String status;

    private String birthday;

    private String address;

    private String userCover;

    private String tags;

    private Long trendCount;

    private Long followerCount;

    private Long fanCount;

    private String loginIp;

    private Date loginDate;

    private String remark;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;

    private Boolean isFollow;
}
