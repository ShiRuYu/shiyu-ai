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
        log.warn("部门查询尚未实现，返回空列表，name: {}", name);
        
        List<Map<String, Object>> depts = new ArrayList<>();
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", depts);
        data.put("total", 0);
        
        return Result.success(data);
    }
}
