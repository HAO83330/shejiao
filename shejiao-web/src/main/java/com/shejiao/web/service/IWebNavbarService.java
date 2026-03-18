package com.shejiao.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shejiao.web.domain.entity.WebNavbar;
import com.shejiao.web.domain.vo.NavbarVO;

import java.util.List;

/**
 * 分类
 *
 * @Author shejiao
 */
public interface IWebNavbarService extends IService<WebNavbar> {

    /**
     * 获取树形分类数据
     */
    List<NavbarVO> getNavbarTreeData();

}
