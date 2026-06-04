package com.shiyu.ai.agent.biz.common.controller;

import com.shiyu.ai.agent.domain.enums.TimezoneEnum;
import com.shiyu.ai.agent.domain.request.SetTimezoneRequest;
import com.shiyu.ai.agent.domain.vo.TimezoneOptionVO;
import com.shiyu.ai.common.core.api.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/timezone")
public class TimezoneController {

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
            return Result.fail("获取时区选项列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/getTimezone")
    public Result<String> getTimezone() {
        log.info("获取当前用户设置的时区");

        try {
            log.warn("时区持久化存储尚未实现，返回默认时区");
            String currentTimeZone = TimezoneEnum.ASIA_SHANGHAI.getValue();

            log.info("当前时区：{}", currentTimeZone);
            return Result.success(currentTimeZone);

        } catch (Exception e) {
            log.error("获取当前时区失败", e);
            return Result.fail("获取当前时区失败：" + e.getMessage());
        }
    }

    @PostMapping("/setTimezone")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        log.info("设置用户时区：{}", request.getTimezone());

        try {
            if (!TimezoneEnum.isValid(request.getTimezone())) {
                log.warn("无效的时区：{}", request.getTimezone());
                return Result.fail("无效的时区：" + request.getTimezone());
            }

            log.warn("时区持久化存储尚未实现，后续需保存到用户配置或数据库中");
            log.info("时区设置成功：{}", request.getTimezone());
            return Result.success();

        } catch (Exception e) {
            log.error("设置时区失败", e);
            return Result.fail("设置时区失败：" + e.getMessage());
        }
    }
}
