package com.shejiao.server.controller.system.web;

import cn.hutool.json.JSONUtil;
import com.shejiao.common.annotation.Log;
import com.shejiao.common.core.controller.BaseController;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.core.domain.Query;
import com.shejiao.common.core.page.TableDataInfo;
import com.shejiao.common.enums.BusinessType;
import com.shejiao.common.enums.Result;
import com.shejiao.common.utils.StringUtils;
import com.shejiao.common.utils.poi.ExcelUtil;
import com.shejiao.web.domain.entity.WebNote;
import com.shejiao.web.service.ISysNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 笔记操作处理
 *
 * @Author shejiao
 */
@RestController
@RequestMapping("/note")
public class SysNoteController extends BaseController {

    @Autowired
    private ISysNoteService noteService;


    /**
     * 获取笔记列表
     */
    @GetMapping("/allList")
    public TableDataInfo list() {
        this.startPage();
        List<WebNote> noteList = noteService.getAllNoteList();
        return getDataTable(noteList);
    }

    /**
     * 获取笔记列表
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<WebNote> noteList = noteService.selectNoteList(new Query(map));
        return getDataTable(noteList);
    }

    /**
     * 获取未审核笔记列表
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/unAuditList")
    public TableDataInfo UnAuditList(@RequestParam Map map) {
        this.startPage();
        List<WebNote> noteList = noteService.selectUnAuditNoteList(new Query(map));
        return getDataTable(noteList);
    }

    /**
     * 根据笔记编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:note:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(noteService.selectNoteById(id));
    }

    /**
     * 新增笔记
     */
    @PreAuthorize("@ss.hasPermi('web:note:add')")
    @Log(title = "笔记管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestParam("note") String note, @RequestParam(value = "file", required = false) MultipartFile file) {
        WebNote webNote = JSONUtil.toBean(note, WebNote.class);
        return toAjax(noteService.insertNote(webNote, file));
    }

    /**
     * 修改笔记
     */
    @PreAuthorize("@ss.hasPermi('web:note:edit')")
    @Log(title = "笔记管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestParam("note") String note, @RequestParam(value = "file", required = false) MultipartFile file) {
        WebNote webNote = JSONUtil.toBean(note, WebNote.class);
        return toAjax(noteService.updateNote(webNote, file));
    }

    /**
     * 删除笔记
     */
    @PreAuthorize("@ss.hasPermi('web:note:remove')")
    @Log(title = "笔记管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(noteService.deleteNoteByIds(ids));
    }

    /**
     * 导出笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('web:note:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam Map map) {
        Query query = new Query(map);
        List<WebNote> noteList;
        
        // 检查是否有ids参数，如果有则导出指定的笔记
        String idsStr = (String) map.get("ids");
        if (StringUtils.isNotEmpty(idsStr)) {
            String[] idArray = idsStr.split(",");
            List<Long> ids = new ArrayList<>();
            for (String id : idArray) {
                ids.add(Long.parseLong(id));
            }
            noteList = noteService.selectNoteByIds(ids);
        } else {
            noteList = noteService.selectNoteList(query);
        }
        
        ExcelUtil<WebNote> util = new ExcelUtil<>(WebNote.class);
        util.exportExcel(response, noteList, "笔记数据");
    }

    /**
     * 审核管理 (0:未审核 1：通过 2：拒绝)
     */
    @PutMapping("auditNote")
    public Result<?> auditNote(@RequestBody Map<String, String> map) {
        String noteId = map.get("noteId");
        String auditType = map.get("auditType");
        String rejectReason = map.get("rejectReason");
        noteService.auditNote(noteId, auditType, rejectReason);
        return Result.ok();
    }
}
