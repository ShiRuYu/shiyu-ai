package com.shiyu.ai.web.auth;

import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.service.TimezoneService;
import com.shiyu.ai.auth.vo.TimezoneOptionVO;
import com.shiyu.ai.common.core.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for timezone preferences. */
@RestController
@RequestMapping("/system/timezone")
@RequiredArgsConstructor
public class TimezoneController {

    private final TimezoneService service;

    @GetMapping("/options")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() {
        try { return Result.success(service.getTimezoneOptions()); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @GetMapping("/current")
    public Result<String> getTimezone() {
        try { return Result.success(service.getTimezone()); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @PostMapping("/set")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        try { return service.setTimezone(request) ? Result.success() : Result.fail("时区设置失败"); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }
}
