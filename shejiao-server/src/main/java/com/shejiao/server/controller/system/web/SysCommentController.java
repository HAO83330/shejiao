package com.shejiao.server.controller.system.web;

import com.shejiao.common.annotation.Log;
import com.shejiao.common.core.controller.BaseController;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.core.domain.Query;
import com.shejiao.common.core.page.TableDataInfo;
import com.shejiao.common.enums.BusinessType;
import com.shejiao.common.utils.poi.ExcelUtil;
import com.shejiao.web.domain.vo.CommentVO;
import com.shejiao.web.service.ISysCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 评论操作处理
 *
 * @Author shejiao
 */
@RestController
@RequestMapping("/comment")
public class SysCommentController extends BaseController {

    @Autowired
    private ISysCommentService commentService;


    /**
     * 获取所有评论列表
     */
    @PreAuthorize("@ss.hasPermi('web:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<CommentVO> commentList = commentService.selectCommentList(new Query(map));
        return getDataTable(commentList);
    }

    /**
     * 查询一级以下评论
     */
    @PreAuthorize("@ss.hasPermi('web:comment:list')")
    @GetMapping("/treeList")
    public TableDataInfo treeList(@RequestParam Map map) {
        this.startPage();
        List<CommentVO> commentList = commentService.selectTreeList(new Query(map));
        return getDataTable(commentList);
    }

    /**
     * 根据评论编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:comment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(commentService.selectCommentById(id));
    }

    /**
     * 根据笔记编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:comment:query')")
    @GetMapping(value = "/list/{nid}")
    public AjaxResult getComment(@PathVariable Long nid) {
        return success(commentService.selectCommentByNid(nid));
    }

    /**
     * 删除评论
     */
    @PreAuthorize("@ss.hasPermi('web:comment:remove')")
    @Log(title = "评论管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(commentService.deleteCommentByIds(ids));
    }

    /**
     * 导出评论列表
     */
    @Log(title = "评论管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('web:comment:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam Map map) {
        List<CommentVO> commentList = commentService.selectCommentList(new Query(map));
        ExcelUtil<CommentVO> util = new ExcelUtil<>(CommentVO.class);
        util.exportExcel(response, commentList, "评论数据");
    }
}
