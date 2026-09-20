package com.shiyu.ai.iam.implementation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.request.SetTimezoneRequest;
import com.shiyu.ai.iam.implementation.service.TimezoneService;
import com.shiyu.ai.iam.implementation.vo.TimezoneOptionVO;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 Timezone 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/iam/timezone")
@RequiredArgsConstructor
public class TimezoneController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final TimezoneService service;

    /**
     * 查询 Timezone 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param options 用于完成本次业务处理的 options 参数。
     */
    @GetMapping("/options")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() {
        return Result.success(service.getTimezoneOptions());
    }

    /**
     * 查询 Timezone 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param current 用于完成本次业务处理的 current 参数。
     */
    @GetMapping("/current")
    public Result<String> getTimezone() {
        return Result.success(service.getTimezone(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 Timezone 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param set 用于完成本次业务处理的 set 参数。
     */
    @PostMapping("/set")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        return service.setTimezone(ActorContextHttpAdapter.currentActor(), request)
                ? Result.success()
                : Result.fail("时区设置失败");
    }
}
