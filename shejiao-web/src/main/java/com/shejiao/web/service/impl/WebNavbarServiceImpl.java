package com.shejiao.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shejiao.common.utils.DozerUtil;
import com.shejiao.common.utils.TreeUtils;
import com.shejiao.web.domain.entity.WebNavbar;
import com.shejiao.web.domain.vo.NavbarVO;
import com.shejiao.web.mapper.WebNavbarMapper;
import com.shejiao.web.service.IWebNavbarService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类
 *
 * @Author shejiao
 */
@Service
public class WebNavbarServiceImpl extends ServiceImpl<WebNavbarMapper, WebNavbar> implements IWebNavbarService {

    /**
     * 获取树形分类数据
     */
    @Override
    public List<NavbarVO> getNavbarTreeData() {
        List<WebNavbar> list = this.list(new QueryWrapper<WebNavbar>().orderByAsc("sort"));
        List<NavbarVO> convertor = DozerUtil.convertor(list, NavbarVO.class);
        return TreeUtils.build(convertor);
    }
}
