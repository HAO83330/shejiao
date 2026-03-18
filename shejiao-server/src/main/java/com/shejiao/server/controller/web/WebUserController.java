package com.shejiao.server.controller.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.shejiao.common.constant.HttpStatus;
import com.shejiao.common.core.page.TableDataInfo;
import com.shejiao.common.enums.Result;
import com.shejiao.common.enums.ResultCodeEnum;
import com.shejiao.web.domain.entity.WebUser;
import com.shejiao.web.domain.vo.NoteSearchVO;
import com.shejiao.web.domain.vo.UserVO;
import com.shejiao.web.service.IWebUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.shejiao.common.utils.PageUtils.startPage;

/**
 * 用户
 *
 * @Author shejiao
 */
@RequestMapping("/web/user")
@RestController
public class WebUserController {

    private static final Logger logger = LoggerFactory.getLogger(WebUserController.class);

    @Autowired
    private IWebUserService userService;


    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型
     */
    @GetMapping("getTrendByUser/{currentPage}/{pageSize}")
    public Result<?> getTrendByUser(@PathVariable long currentPage, @PathVariable long pageSize, String userId, Integer type) {
        Page<NoteSearchVO> pageInfo = userService.getTrendByUser(currentPage, pageSize, userId, type);
        return Result.ok(pageInfo);
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     */
    @GetMapping("getUserById")
    public Result<?> getUserById(String userId) {
        logger.info("获取用户信息，userId: {}", userId);
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            logger.warn("获取用户信息失败，用户ID不能为空");
            return Result.build(ResultCodeEnum.NOT_NULL.getCode(), "用户ID不能为空");
        }
        try {
            WebUser user = userService.getUserById(userId);
            logger.info("获取用户信息成功，userId: {}", userId);
            return Result.ok(user);
        } catch (com.shejiao.common.exception.web.shejiaoException e) {
            logger.error("获取用户信息失败，userId: {}", userId, e);
            return Result.build(201, e.getMessage());
        } catch (Exception e) {
            logger.error("获取用户信息失败，userId: {}", userId, e);
            return Result.build(500, "系统内部错误");
        }
    }

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    @PostMapping("updateUser")
    public Result<?> updateUser(@RequestBody WebUser user) {
        WebUser updateUser = userService.updateUser(user);
        return Result.ok(updateUser);
    }

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     */
    @GetMapping("getUserByKeyword/{currentPage}/{pageSize}")
    public Result<?> getUserByKeyword(@PathVariable long currentPage, @PathVariable long pageSize, String keyword) {
        Page<UserVO> pageInfo = userService.getUserByKeyword(currentPage, pageSize, keyword);
        return Result.ok(pageInfo);
    }

    /**
     * 保存用户的搜索记录
     *
     * @param keyword 关键词
     */
    @GetMapping("saveUserSearchRecord")
    public Result<?> saveUserSearchRecord(String keyword) {
        userService.saveUserSearchRecord(keyword);
        return Result.ok();
    }

    /**
     * 会员列表
     *
     * @param user 用户
     */
    @GetMapping("getUserList")
    public TableDataInfo getUserList(WebUser user) {
        startPage();
        List<WebUser> userList = userService.getUserList(user);
        return getDataTable(userList);
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }
}
