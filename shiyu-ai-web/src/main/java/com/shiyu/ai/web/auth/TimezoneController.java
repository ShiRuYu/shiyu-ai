package com.shiyu.ai.web.auth;

import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.auth.service.impl.TimezoneServiceImpl;
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

    private final TimezoneServiceImpl service;

    @GetMapping("/options")
    public Result<List<TimezoneOptionVO>> getTimezoneOptions() { return service.getTimezoneOptions(); }

    @GetMapping("/current")
    public Result<String> getTimezone() { return service.getTimezone(); }

    @PostMapping("/set")
    public Result<Void> setTimezone(@Valid @RequestBody SetTimezoneRequest request) {
        return service.setTimezone(request);
    }
}
