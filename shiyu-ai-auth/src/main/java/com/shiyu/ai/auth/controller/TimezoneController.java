package com.shiyu.ai.auth.controller;

import com.shiyu.ai.dal.repository.UserRepository;
import com.shiyu.ai.model.bo.UserBO;
import com.shiyu.ai.model.enums.TimezoneEnum;
import com.shiyu.ai.model.request.SetTimezoneRequest;
import com.shiyu.ai.model.vo.TimezoneOptionVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/timezone")
public class TimezoneController {

    private final UserRepository userRepository;

    public TimezoneController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/getTimezoneOptions")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() {
        log.info("鑾峰彇绯荤粺鏀寔鐨勬椂鍖洪€夐」鍒楄〃");
        try {
            List<TimezoneOptionVO> options = Arrays.stream(TimezoneEnum.values())
                    .map(timezone -> TimezoneOptionVO.builder()
                            .label(timezone.getLabel())
                            .value(timezone.getValue())
                            .build())
                    .collect(Collectors.toList());
            log.info("鎴愬姛鑾峰彇鏃跺尯閫夐」鍒楄〃锛屽叡 {} 涓?, options.size());
            return Result.success(options);
        } catch (Exception e) {
            log.error("Failed to get timezone options", e);
            return Result.fail("Failed to get timezone options");
        }
    }

    @GetMapping("/getTimezone")
    public Result<String> getTimezone() {
        log.info("鑾峰彇褰撳墠鐢ㄦ埛璁剧疆鐨勬椂鍖?);
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

            log.info("褰撳墠鏃跺尯锛歿}", currentTimeZone);
            return Result.success(currentTimeZone);
        } catch (Exception e) {
            log.error("鑾峰彇褰撳墠鏃跺尯澶辫触", e);
            return Result.fail("鑾峰彇褰撳墠鏃跺尯澶辫触");
        }
    }

    @PostMapping("/setTimezone")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        log.info("璁剧疆鐢ㄦ埛鏃跺尯锛歿}", request.getTimezone());
        try {
            if (!TimezoneEnum.isValid(request.getTimezone())) {
                log.warn("鏃犳晥鐨勬椂鍖猴細{}", request.getTimezone());
                return Result.fail("鏃犳晥鐨勬椂鍖?);
            }

            Long userId = LoginContextHolder.getUserId();
            if (userId == null) {
                return Result.fail("鐢ㄦ埛鏈櫥褰?);
            }

            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                return Result.fail("鐢ㄦ埛涓嶅瓨鍦?);
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

            log.info("鏃跺尯璁剧疆鎴愬姛锛歿}", request.getTimezone());
            return Result.success();
        } catch (Exception e) {
            log.error("璁剧疆鏃跺尯澶辫触", e);
            return Result.fail("璁剧疆鏃跺尯澶辫触");
        }
    }
}
