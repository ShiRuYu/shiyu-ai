package com.shiyu.ai.education.controller;
import com.shiyu.ai.education.dto.ResourceResponse;

import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.bo.education.ResourceBO;
import com.shiyu.ai.education.request.ResourceRequest;
import com.shiyu.ai.education.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/detail")
    public Result<ResourceResponse> getById(@RequestParam Long id) {
        return Result.success(resourceService.getById(id));
    }

    @GetMapping("/list")
    public Result<PageData<ResourceResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(resourceService.page(pageNum, pageSize));
    }

    @GetMapping("/subject")
    public Result<List<ResourceResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(resourceService.listBySubjectCode(subjectCode));
    }

    @GetMapping("/type")
    public Result<List<ResourceResponse>> listByType(@RequestParam String type) {
        return Result.success(resourceService.listByType(type));
    }

    @PostMapping("/create")
    public Result<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        return Result.success(resourceService.create(request));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ResourceRequest request) {
        request.setId(id);
        resourceService.update(request);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        resourceService.deleteById(id);
        return Result.success();
    }
}
