package com.shiyu.ai.web.prompt;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.runtime.PromptService;
import com.shiyu.ai.runtime.PromptTemplate;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/prompts")
public class PromptController {
    private final PromptService prompts;
    public PromptController(PromptService prompts) { this.prompts = prompts; }
    @GetMapping public Result<List<PromptTemplate>> list() { return Result.success(prompts.list(tenant(), user())); }
    @PostMapping public Result<PromptTemplate> create(@Valid @RequestBody CreateRequest request) { return Result.success(prompts.create(tenant(), user(), request.name, request.template, request.variables)); }
    @PostMapping("/{id}/publish") public Result<PromptTemplate> publish(@PathVariable String id) { return Result.success(prompts.publish(id, tenant(), user())); }
    @PostMapping("/preview") public Result<PromptService.PromptPreview> preview(@Valid @RequestBody PreviewRequest request) { return Result.success(prompts.preview(request.template, request.variables)); }
    private long tenant() { Long id = UserContextHolder.getCurrentTenantId(); if (id == null) throw new IllegalStateException("tenant context is required"); return id; }
    private long user() { Long id = UserContextHolder.getUserId(); if (id == null) throw new IllegalStateException("login is required"); return id; }
    @Data public static class CreateRequest { private String name; private String template; private List<String> variables; }
    @Data public static class PreviewRequest { private String template; private Map<String,Object> variables; }
}
