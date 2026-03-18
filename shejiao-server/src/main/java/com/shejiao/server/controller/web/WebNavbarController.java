package com.shejiao.server.controller.web;

import com.shejiao.common.enums.Result;
import com.shejiao.common.validator.myVaildator.noLogin.NoLoginIntercept;
import com.shejiao.web.domain.vo.NavbarVO;
import com.shejiao.web.service.IWebNavbarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类
 *
 * @Author shejiao
 */
@RequestMapping("/web/category")
@RestController
public class WebNavbarController {

    @Autowired
    private IWebNavbarService navbarService;


    /**
     * 获取树形分类数据
     */
    @GetMapping("getCategoryTreeData")
    @NoLoginIntercept
    public Result<?> getCategoryTreeData() {
        List<NavbarVO> navbarList = navbarService.getNavbarTreeData();
        return Result.ok(navbarList);
    }
}
