package com.shiyu.ai.agent.biz.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/dept")
public class DeptController {

    /**
     * 获取部门列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getDeptList(
            @RequestParam(required = false) String name) {
        log.info("获取部门，name: {}", name);
        
        // TODO: 实现真实的部门查询逻辑
        // 目前返回空列表
        List<Map<String, Object>> depts = new ArrayList<>();
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", depts);
        data.put("total", 0);
        
        return Result.success(data);
    }
}
