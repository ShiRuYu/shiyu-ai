package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.ResourceDO;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.education.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "资源管理")
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/{id}")
    @Operation(summary = "获取资源详情")
    public Result<ResourceResponse> getById(@PathVariable Long id) {
        ResourceDO resource = resourceService.getById(id);
        return Result.success(toResponse(resource));
    }

    @GetMapping
    @Operation(summary = "分页获取资源")
    public Result<PageData<ResourceResponse>> listAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageData<ResourceDO> page = resourceService.page(pageNum, pageSize);
        List<ResourceResponse> items = page.getItems().stream().map(this::toResponse).toList();
        return Result.success(new PageData<>(items, page.getTotal()));
    }

    @GetMapping("/subject/{subjectCode}")
    @Operation(summary = "根据学科获取资源")
    public Result<List<ResourceResponse>> listBySubjectCode(@PathVariable String subjectCode) {
        List<ResourceDO> resources = resourceService.listBySubjectCode(subjectCode);
        return Result.success(resources.stream().map(this::toResponse).toList());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取资源")
    public Result<List<ResourceResponse>> listByType(@PathVariable String type) {
        List<ResourceDO> resources = resourceService.listByType(type);
        return Result.success(resources.stream().map(this::toResponse).toList());
    }

    @PostMapping
    @Operation(summary = "创建资源")
    public Result<ResourceResponse> create(@Valid @RequestBody ResourceDO resource) {
        ResourceDO created = resourceService.create(resource);
        return Result.success(toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资源")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ResourceDO resource) {
        resource.setId(id);
        resourceService.update(resource);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.deleteById(id);
        return Result.success();
    }

    private ResourceResponse toResponse(ResourceDO resource) {
        if (resource == null) return null;
        return new ResourceResponse(
                resource.getId(), resource.getName(), resource.getType(),
                resource.getUrl(), resource.getSubjectCode(), resource.getGrade(),
                resource.getDifficulty(), resource.getCoverUrl(), resource.getDescription(),
                resource.getViewCount());
    }
}
