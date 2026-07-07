package com.shiyu.ai.auth.controller;

import com.shiyu.ai.dal.repository.auth.UserRepository;
import com.shiyu.ai.auth.bo.UserBO;
import com.shiyu.ai.model.enums.TimezoneEnum;
import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.vo.TimezoneOptionVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "Timezone", description = "Timezone")
@RestController
@RequestMapping("/api/timezone")
public class TimezoneController {

    private final UserRepository userRepository;

    public TimezoneController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get Timezone Options")
    @GetMapping("/getTimezoneOptions")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() {
        log.info("获取系统支持的时区选项列表");
        try {
            List<TimezoneOptionVO> options = Arrays.stream(TimezoneEnum.values())
                    .map(timezone -> TimezoneOptionVO.builder()
                            .label(timezone.getLabel())
                            .value(timezone.getValue())
                            .build())
                    .collect(Collectors.toList());
            log.info("成功获取时区选项列表，共 {} 个", options.size());
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取时区选项列表失败", e);
            return Result.fail("获取时区选项列表失败");
        }
    }

    @Operation(summary = "Get Timezone")
    @GetMapping("/getTimezone")
    public Result<String> getTimezone() {
        log.info("获取当前用户设置的时区");
        try {
            Long userId = LoginContextHolder.getUserId();
            String currentTimeZone = TimezoneEnum.ASIA_SHANGHAI.getValue();

            if (userId != null) {
                UserBO user = userRepository.selectById(userId);
                if (user != null && user.getExtInfo() != null) {
                    Map<String, Object> extMap = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                    if (extMap != null && extMap.containsKey("timezone")) {
                        currentTimeZone = (String) extMap.get("timezone");
                    }
                }
            }

            log.info("当前时区：{}", currentTimeZone);
            return Result.success(currentTimeZone);
        } catch (Exception e) {
            log.error("获取当前时区失败", e);
            return Result.fail("获取当前时区失败");
        }
    }

    @Operation(summary = "Set Timezone")
    @PostMapping("/setTimezone")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        log.info("设置用户时区：{}", request.getTimezone());
        try {
            if (!TimezoneEnum.isValid(request.getTimezone())) {
                log.warn("无效的时区：{}", request.getTimezone());
                return Result.fail("无效的时区");
            }

            Long userId = LoginContextHolder.getUserId();
            if (userId == null) {
                return Result.fail("用户未登录");
            }

            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }

            Map<String, Object> extMap = new HashMap<>();
            if (user.getExtInfo() != null) {
                Map<String, Object> existing = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                if (existing != null) {
                    extMap = existing;
                }
            }
            extMap.put("timezone", request.getTimezone());
            user.setExtInfo(JSONUtils.toJsonString(extMap));
            userRepository.update(user);

            log.info("时区设置成功：{}", request.getTimezone());
            return Result.success();
        } catch (Exception e) {
            log.error("设置时区失败", e);
            return Result.fail("设置时区失败");
        }
    }
}
