package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.auth.service.DemoService;
import com.shiyu.ai.agent.domain.vo.MenuAllVO;
import com.shiyu.ai.agent.domain.vo.PageResult;
import com.shiyu.ai.agent.domain.vo.ProductVO;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    /**
     * 获取所有菜单
     */
    @GetMapping("/menu/all")
    public ResponseEntity<Result<List<MenuAllVO>>> getAllMenus() {
        log.info("获取所有菜单列表");
        List<MenuAllVO> menus = demoService.getAllMenus();
        return ResponseEntity.ok(Result.success(menus));
    }

    /**
     * 获取表格数据列表
     *
     * @param page 页码，默认1
     * @param pageSize 每页大小，默认20
     * @param category 分类过滤（可选）
     * @param start 开始日期（可选）
     * @param end 结束日期（可选）
     */
    @GetMapping("/table/list")
    public ResponseEntity<Result<PageResult<ProductVO>>> getTableList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        log.info("获取表格数据列表 - page: {}, pageSize: {}, category: {}, start: {}, end: {}", 
                page, pageSize, category, start, end);
        PageResult<ProductVO> result = demoService.getTableList(page, pageSize, category, start, end);
        return ResponseEntity.ok(Result.success(result));
    }
}
