package com.shejiao.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shejiao.web.domain.entity.WebPictureSort;
import com.shejiao.web.domain.vo.PictureSortVO;

/**
 * 图片分类表 服务类
 *
 * @Author shejiao
 */
public interface IPictureSortService extends SuperService<WebPictureSort> {

    /**
     * 获取图片分类列表
     *
     * @param pictureSortVO
     * @return
     */
    IPage<WebPictureSort> getPageList(PictureSortVO pictureSortVO);

    /**
     * 新增图片分类
     *
     * @param pictureSortVO
     */
    String addPictureSort(PictureSortVO pictureSortVO);

    /**
     * 编辑图片分类
     *
     * @param pictureSortVO
     */
    String editPictureSort(PictureSortVO pictureSortVO);

    /**
     * 删除图片分类
     *
     * @param pictureSortVO
     */
    String deletePictureSort(PictureSortVO pictureSortVO);

    /**
     * 置顶图片分类
     *
     * @param pictureSortVO
     */
    String stickPictureSort(PictureSortVO pictureSortVO);
}
