package com.shiyu.ai.auth.web;

import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.service.TimezoneService;
import com.shiyu.ai.auth.vo.TimezoneOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for timezone preferences. */
@RestController
@RequestMapping("/api/iam/timezone")
@RequiredArgsConstructor
public class TimezoneController {

    private final TimezoneService service;

    @GetMapping("/options")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() {
        return Result.success(service.getTimezoneOptions());
    }

    @GetMapping("/current")
    public Result<String> getTimezone() {
        return Result.success(service.getTimezone(ActorContextHttpAdapter.currentActor()));
    }

    @PostMapping("/set")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        return service.setTimezone(ActorContextHttpAdapter.currentActor(), request) ? Result.success() : Result.fail("时区设置失败");
    }
}
