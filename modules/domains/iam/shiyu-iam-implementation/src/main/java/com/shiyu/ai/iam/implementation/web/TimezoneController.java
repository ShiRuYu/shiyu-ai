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
 * TimezoneController 控制器，负责处理身份与访问领域相关 HTTP 请求并返回响应。
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
     * {@code getTimezoneOptions} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/options")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() {
        return Result.success(service.getTimezoneOptions());
    }

    /**
     * {@code getTimezone} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/current")
    public Result<String> getTimezone() {
        return Result.success(service.getTimezone(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * {@code setTimezone} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/set")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        return service.setTimezone(ActorContextHttpAdapter.currentActor(), request)
                ? Result.success()
                : Result.fail("时区设置失败");
    }
}
