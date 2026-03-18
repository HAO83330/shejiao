package com.shejiao.web.domain.vo;

import com.shejiao.common.utils.TreeNode;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类
 *
 * @Author shejiao
 */
@Data
public class NavbarVO extends TreeNode<NavbarVO> implements Serializable {

    private String title;
    private long likeCount;
}
