package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.enums.TimezoneEnum;
import com.shiyu.ai.agent.domain.request.SetTimezoneRequest;
import com.shiyu.ai.agent.domain.vo.TimezoneOptionVO;
import com.shiyu.ai.common.core.api.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时区管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/timezone")
public class TimezoneController {
    
    /**
     * 获取系统支持的时区选项列表
     * GET /timezone/getTimezoneOptions
     * 
     * @return 时区选项列表
     */
    @GetMapping("/getTimezoneOptions")
    public ResponseEntity<Result<List<TimezoneOptionVO>>> getTimezoneOptions() {
        log.info("获取系统支持的时区选项列表");
        
        try {
            // 从枚举中获取所有时区选项
            List<TimezoneOptionVO> options = Arrays.stream(TimezoneEnum.values())
                    .map(timezone -> TimezoneOptionVO.builder()
                            .label(timezone.getLabel())
                            .value(timezone.getValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("成功获取时区选项列表，共 {} 个", options.size());
            return ResponseEntity.ok(Result.success(options));
            
        } catch (Exception e) {
            log.error("获取时区选项列表失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取时区选项列表失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户设置的时区
     * GET /timezone/getTimezone
     * 
     * @return 当前时区标识符
     */
    @GetMapping("/getTimezone")
    public ResponseEntity<Result<String>> getTimezone() {
        log.info("获取当前用户设置的时区");
        
        try {
            // TODO: 从用户上下文或数据库中获取用户的时区设置
            // 这里暂时返回默认时区（上海）
            String current_timezone = TimezoneEnum.ASIA_SHANGHAI.getValue();
            
            log.info("当前时区：{}", current_timezone);
            return ResponseEntity.ok(Result.success(current_timezone));
            
        } catch (Exception e) {
            log.error("获取当前时区失败", e);
            return ResponseEntity.badRequest().body(Result.fail("获取当前时区失败：" + e.getMessage()));
        }
    }
    
    /**
     * 设置用户时区
     * POST /timezone/setTimezone
     * 
     * @param request 时区设置请求
     * @return 设置结果
     */
    @PostMapping("/setTimezone")
    public ResponseEntity<Result<Void>> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        log.info("设置用户时区：{}", request.getTimezone());
        
        try {
            // 验证时区是否在预定义选项中
            if (!TimezoneEnum.isValid(request.getTimezone())) {
                log.warn("无效的时区：{}", request.getTimezone());
                return ResponseEntity.badRequest().body(Result.fail("无效的时区：" + request.getTimezone()));
            }
            
            // TODO: 将时区保存到用户配置或数据库中
            // 这里只是验证了时区的有效性
            
            log.info("时区设置成功：{}", request.getTimezone());
            return ResponseEntity.ok(Result.success());
            
        } catch (Exception e) {
            log.error("设置时区失败", e);
            return ResponseEntity.badRequest().body(Result.fail("设置时区失败：" + e.getMessage()));
        }
    }
}
