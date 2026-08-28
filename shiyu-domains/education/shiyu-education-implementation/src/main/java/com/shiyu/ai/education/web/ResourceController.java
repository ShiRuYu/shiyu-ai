package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.education.request.ResourceRequest;
import com.shiyu.ai.education.service.ResourceService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
@SaCheckPermission("edu:resource:list")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/detail")
    public Result<ResourceResponse> getById(@RequestParam Long id) {
        return Result.success(resourceService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    @GetMapping("/list")
    public Result<PageData<ResourceResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(resourceService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    @GetMapping("/subject")
    public Result<List<ResourceResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(resourceService.listBySubjectCode(ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    @GetMapping("/type")
    public Result<List<ResourceResponse>> listByType(@RequestParam String type) {
        return Result.success(resourceService.listByType(ActorContextHttpAdapter.currentActor(), type));
    }

    @PostMapping("/create")
    @SaCheckPermission("edu:resource:upload")
    public Result<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        return Result.success(resourceService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    @PostMapping("/update")
    @SaCheckPermission("edu:resource:upload")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ResourceRequest request) {
        request.setId(id);
        resourceService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    @PostMapping("/delete")
    @SaCheckPermission("edu:resource:delete")
    public Result<Void> delete(@RequestParam Long id) {
        resourceService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
