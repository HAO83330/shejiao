package com.shejiao.server.controller.system.web;

import com.shejiao.common.constant.Constantss;
import com.shejiao.common.core.controller.BaseController;
import com.shejiao.common.core.domain.AjaxResult;
import com.shejiao.common.enums.EStatus;
import com.shejiao.common.global.SysConf;
import com.shejiao.web.service.ISysCommentService;
import com.shejiao.web.service.ISysMemberService;
import com.shejiao.web.service.ISysNoteService;
import com.shejiao.web.service.ISysVisitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页RestApi
 *
 * @Author shejiao
 * @date 2025年10月22日下午3:27:24
 */
@RestController
@RequestMapping("/index")
@Api(value = "首页相关接口", tags = {"首页相关接口"})
@Slf4j
public class SysDashboardController extends BaseController {

    @Autowired
    private ISysNoteService noteService;
    @Autowired
    private ISysCommentService commentService;
    @Autowired
    private ISysVisitService visitService;
    @Autowired
    private ISysMemberService memberService;


    @ApiOperation(value = "首页初始化数据", notes = "首页初始化数据", response = String.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public AjaxResult init() {
        Map<String, Object> map = new HashMap<>(Constantss.NUM_FOUR);
        map.put(SysConf.VISIT_COUNT, visitService.getWebVisitCount());
        map.put(SysConf.USER_COUNT, memberService.getMemberCount(0));
        map.put(SysConf.BLOG_COUNT, noteService.getNoteCount(EStatus.ENABLE));
        map.put(SysConf.COMMENT_COUNT, commentService.getCommentCount(EStatus.ENABLE));
        return success(map);
    }

    @ApiOperation(value = "获取最近30天用户独立IP数和访问量", notes = "获取最近30天用户独立IP数和访问量", response = String.class)
    @RequestMapping(value = "/getVisitByWeek", method = RequestMethod.GET)
    public AjaxResult getVisitByWeek() {
        Map<String, Object> visitByWeek = visitService.getVisitByWeek();
        return success(visitByWeek);
    }

    @ApiOperation(value = "获取每个标签下文章数目", notes = "获取每个标签下文章数目", response = String.class)
    @RequestMapping(value = "/getBlogCountByTag", method = RequestMethod.GET)
    public AjaxResult getBlogCountByTag() {
        List<Map<String, Object>> blogCountByTag = noteService.getNoteCountByCategory();
        return success(blogCountByTag);
    }

    @ApiOperation(value = "获取每个分类下文章数目", notes = "获取每个分类下文章数目", response = String.class)
    @RequestMapping(value = "/getBlogCountByBlogSort", method = RequestMethod.GET)
    public AjaxResult getBlogCountByBlogSort() {
        List<Map<String, Object>> blogCountByTag = noteService.getNoteCountByCategory();
        return success(blogCountByTag);
    }

    @ApiOperation(value = "获取一年内的文章贡献数", notes = "获取一年内的文章贡献度", response = String.class)
    @RequestMapping(value = "/getBlogContributeCount", method = RequestMethod.GET)
    public AjaxResult getBlogContributeCount() {
        Map<String, Object> resultMap = noteService.getNoteContributeCount();
        return success(resultMap);
    }

    @ApiOperation(value = "获取用户增长趋势", notes = "获取最近30天的用户增长趋势", response = String.class)
    @RequestMapping(value = "/getUserGrowthTrend", method = RequestMethod.GET)
    public AjaxResult getUserGrowthTrend() {
        Map<String, Object> resultMap = memberService.getUserGrowthTrend(30);
        return success(resultMap);
    }

    @ApiOperation(value = "导出首页数据", notes = "导出首页所有数据和图表为Excel", response = String.class)
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(HttpServletResponse response) {
        try {
            // 创建工作簿
            XSSFWorkbook workbook = new XSSFWorkbook();

            // 1. 创建基础数据工作表
            createBasicDataSheet(workbook);

            // 2. 创建分类统计图表工作表
            createCategoryChartSheet(workbook);

            // 3. 创建访问趋势图表工作表
            createVisitTrendChartSheet(workbook);

            // 4. 创建用户增长趋势图表工作表
            createUserGrowthChartSheet(workbook);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("首页数据", "UTF-8") + ".xlsx";
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);

            // 写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            log.error("导出首页数据失败", e);
            // 如果发生异常，返回一个空的Excel文件
            try {
                XSSFWorkbook workbook = new XSSFWorkbook();
                workbook.createSheet("基础数据");
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("首页数据", "UTF-8") + ".xlsx";
                response.setHeader("Content-disposition", "attachment;filename=" + fileName);
                workbook.write(response.getOutputStream());
                workbook.close();
            } catch (Exception ex) {
                log.error("导出空Excel失败", ex);
            }
        }
    }

    /**
     * 创建基础数据工作表
     */
    private void createBasicDataSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("基础数据");

        // 创建表头
        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("数据类型");
        headerRow.createCell(1).setCellValue("数据名称");
        headerRow.createCell(2).setCellValue("数据值");
        headerRow.createCell(3).setCellValue("单位");
        headerRow.createCell(4).setCellValue("描述");

        // 添加基础统计数据
        Map<String, Object> initData = new HashMap<>();
        initData.put(SysConf.VISIT_COUNT, visitService.getWebVisitCount());
        initData.put(SysConf.USER_COUNT, memberService.getMemberCount(0));
        initData.put(SysConf.BLOG_COUNT, noteService.getNoteCount(EStatus.ENABLE));
        initData.put(SysConf.COMMENT_COUNT, commentService.getCommentCount(EStatus.ENABLE));

        // 填充数据
        int rowIndex = 1;

        XSSFRow visitRow = sheet.createRow(rowIndex++);
        visitRow.createCell(0).setCellValue("基础统计");
        visitRow.createCell(1).setCellValue("总访问量");
        visitRow.createCell(2).setCellValue(initData.get(SysConf.VISIT_COUNT) != null ? initData.get(SysConf.VISIT_COUNT).toString() : "0");
        visitRow.createCell(3).setCellValue("次");
        visitRow.createCell(4).setCellValue("网站总访问量");

        XSSFRow userRow = sheet.createRow(rowIndex++);
        userRow.createCell(0).setCellValue("基础统计");
        userRow.createCell(1).setCellValue("会员数");
        userRow.createCell(2).setCellValue(initData.get(SysConf.USER_COUNT) != null ? initData.get(SysConf.USER_COUNT).toString() : "0");
        userRow.createCell(3).setCellValue("人");
        userRow.createCell(4).setCellValue("网站注册会员总数");

        XSSFRow blogRow = sheet.createRow(rowIndex++);
        blogRow.createCell(0).setCellValue("基础统计");
        blogRow.createCell(1).setCellValue("笔记数");
        blogRow.createCell(2).setCellValue(initData.get(SysConf.BLOG_COUNT) != null ? initData.get(SysConf.BLOG_COUNT).toString() : "0");
        blogRow.createCell(3).setCellValue("篇");
        blogRow.createCell(4).setCellValue("网站发布的笔记总数");

        XSSFRow commentRow = sheet.createRow(rowIndex++);
        commentRow.createCell(0).setCellValue("基础统计");
        commentRow.createCell(1).setCellValue("评论数");
        commentRow.createCell(2).setCellValue(initData.get(SysConf.COMMENT_COUNT) != null ? initData.get(SysConf.COMMENT_COUNT).toString() : "0");
        commentRow.createCell(3).setCellValue("条");
        commentRow.createCell(4).setCellValue("网站收到的评论总数");

        // 调整列宽
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建分类统计图表工作表
     */
    private void createCategoryChartSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("分类统计");

        // 添加分类数据
        List<Map<String, Object>> categoryData = noteService.getNoteCountByCategory();
        if (categoryData != null && !categoryData.isEmpty()) {
            // 创建表头
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("分类名称");
            headerRow.createCell(1).setCellValue("笔记数量");

            // 填充数据
                int rowIndex = 1;
                for (Map<String, Object> category : categoryData) {
                    XSSFRow row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(category.get("name") != null ? category.get("name").toString() : (category.get("title") != null ? category.get("title").toString() : "未知分类"));
                    row.createCell(1).setCellValue(category.get("count") != null ? Double.parseDouble(category.get("count").toString()) : (category.get("value") != null ? Double.parseDouble(category.get("value").toString()) : 0));
                }

            // 调整列宽
            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }

            // 创建饼图
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, 1, 13, 15);
            XSSFChart chart = drawing.createChart(anchor);

            // 设置图表标题
            // chart.setTitleText("分类统计");
            // chart.setTitleOverlay(false);

            // 创建数据源
            XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 0, 0));
            XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 1, 1));

            // 创建饼图系列
            XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
            XDDFChartData.Series series = data.addSeries(categories, values);
            series.setTitle("笔记数量", null);

            // 设置饼图属性
            chart.plot(data);
        }
    }

    /**
     * 创建访问趋势图表工作表
     */
    private void createVisitTrendChartSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("访问趋势");

        // 添加访问趋势数据
        Map<String, Object> visitByWeek = visitService.getVisitByWeek();
        if (visitByWeek != null) {
            List<String> dates = (List<String>) visitByWeek.get("date");
            List<Integer> pv = (List<Integer>) visitByWeek.get("pv");
            List<Integer> uv = (List<Integer>) visitByWeek.get("uv");

            if (dates != null && pv != null && uv != null && dates.size() == pv.size() && dates.size() == uv.size()) {
                // 创建表头
                XSSFRow headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("日期");
                headerRow.createCell(1).setCellValue("访问量");
                headerRow.createCell(2).setCellValue("独立访客");

                // 填充数据
                int rowIndex = 1;
                for (int i = 0; i < dates.size(); i++) {
                    XSSFRow row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(dates.get(i));
                    row.createCell(1).setCellValue(pv.get(i) != null ? pv.get(i).doubleValue() : 0);
                    row.createCell(2).setCellValue(uv.get(i) != null ? uv.get(i).doubleValue() : 0);
                }

                // 调整列宽
                for (int i = 0; i < 3; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 创建折线图
                XSSFDrawing drawing = sheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 1, 14, 15);
                XSSFChart chart = drawing.createChart(anchor);

                // 设置图表标题
                // chart.setTitleText("访问趋势");
                // chart.setTitleOverlay(false);

                // 创建坐标轴
                XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);

                // 创建数据源
                XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 0, 0));
                XDDFNumericalDataSource<Double> pvValues = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 1, 1));
                XDDFNumericalDataSource<Double> uvValues = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 2, 2));

                // 创建折线图系列
                XDDFChartData data = chart.createData(ChartTypes.LINE, xAxis, yAxis);
                XDDFChartData.Series pvSeries = data.addSeries(categories, pvValues);
                pvSeries.setTitle("访问量", null);
                XDDFChartData.Series uvSeries = data.addSeries(categories, uvValues);
                uvSeries.setTitle("独立访客", null);

                // 设置折线图属性
            chart.plot(data);
            }
        }
    }

    /**
     * 创建用户增长趋势图表工作表
     */
    private void createUserGrowthChartSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("用户增长");

        // 添加用户增长趋势数据
        Map<String, Object> userGrowth = memberService.getUserGrowthTrend(30);
        if (userGrowth != null) {
            List<String> growthDates = (List<String>) userGrowth.get("dates");
            List<Integer> newUsers = (List<Integer>) userGrowth.get("newUsers");
            List<Integer> totalUsers = (List<Integer>) userGrowth.get("totalUsers");

            if (growthDates != null && newUsers != null && totalUsers != null && growthDates.size() == newUsers.size() && growthDates.size() == totalUsers.size()) {
                // 创建表头
                XSSFRow headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("日期");
                headerRow.createCell(1).setCellValue("新增用户");
                headerRow.createCell(2).setCellValue("累计用户");

                // 填充数据
                int rowIndex = 1;
                for (int i = 0; i < growthDates.size(); i++) {
                    XSSFRow row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(growthDates.get(i));
                    row.createCell(1).setCellValue(newUsers.get(i) != null ? newUsers.get(i).doubleValue() : 0);
                    row.createCell(2).setCellValue(totalUsers.get(i) != null ? totalUsers.get(i).doubleValue() : 0);
                }

                // 调整列宽
                for (int i = 0; i < 3; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 创建折线图
                XSSFDrawing drawing = sheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 1, 14, 15);
                XSSFChart chart = drawing.createChart(anchor);

                // 设置图表标题
                // chart.setTitleText("用户增长趋势");
                // chart.setTitleOverlay(false);

                // 创建坐标轴
                XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);

                // 创建数据源
                XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 0, 0));
                XDDFNumericalDataSource<Double> newUserValues = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 1, 1));
                XDDFNumericalDataSource<Double> totalUserValues = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 2, 2));

                // 创建折线图系列
                XDDFChartData data = chart.createData(ChartTypes.LINE, xAxis, yAxis);
                XDDFChartData.Series newUserSeries = data.addSeries(categories, newUserValues);
                newUserSeries.setTitle("新增用户", null);
                XDDFChartData.Series totalUserSeries = data.addSeries(categories, totalUserValues);
                totalUserSeries.setTitle("累计用户", null);

                // 设置折线图属性
            chart.plot(data);
            }
        }
    }
}
