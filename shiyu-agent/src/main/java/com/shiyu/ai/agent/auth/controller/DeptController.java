package com.shiyu.ai.agent.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/system/dept")
public class DeptController {

    /**
     * 获取部门列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getDeptList(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String name) {
        log.info("获取部门列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        // TODO: 实现真实的部门查询逻辑
        // 目前返回空列表
        List<Map<String, Object>> depts = new ArrayList<>();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", depts);
        data.put("total", 0);
        data.put("pageNo", pageNo != null ? pageNo : 1);
        data.put("pageSize", pageSize != null ? pageSize : 10);
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
}
