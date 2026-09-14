package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ResourceService;
import com.shiyu.ai.education.implementation.web.dto.ResourceResponse;
import com.shiyu.ai.education.implementation.web.request.ResourceRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ResourceController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
@SaCheckPermission("edu:resource:list")
public class ResourceController {

    /**
     * 资源服务，表示当前对象中的对应属性。
     */
    private final ResourceService resourceService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<ResourceResponse> getById(@RequestParam Long id) {
        return Result.success(resourceService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<PageData<ResourceResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                resourceService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * {@code listBySubjectCode} 查询并返回当前操作所需的数据。
     *
     * @param subjectCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/subject")
    public Result<List<ResourceResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(
                resourceService.listBySubjectCode(
                        ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    /**
     * {@code listByType} 查询并返回当前操作所需的数据。
     *
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/type")
    public Result<List<ResourceResponse>> listByType(@RequestParam String type) {
        return Result.success(
                resourceService.listByType(ActorContextHttpAdapter.currentActor(), type));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:resource:upload")
    public Result<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        return Result.success(
                resourceService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/update")
    @SaCheckPermission("edu:resource:upload")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ResourceRequest request) {
        request.setId(id);
        resourceService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:resource:delete")
    public Result<Void> delete(@RequestParam Long id) {
        resourceService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
