package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 资源 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    /**
     * 资源服务，表示当前对象中的对应属性。
     */
    private final ResourceService resourceService;

    /**
     * 查询 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:resource:list")
    @GetMapping("/{id}")
    public Result<ResourceResponse> getById(@PathVariable Long id) {
        return Result.success(resourceService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    @SaCheckPermission("edu:resource:list")
    @GetMapping
    public Result<PageData<ResourceResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                resourceService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * 查询 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param subject 用于完成本次业务处理的 subject 参数。
     */
    @SaCheckPermission("edu:resource:list")
    @GetMapping("/subject")
    public Result<List<ResourceResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(
                resourceService.listBySubjectCode(
                        ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    /**
     * 查询 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param type 用于完成本次业务处理的 type 参数。
     */
    @SaCheckPermission("edu:resource:list")
    @GetMapping("/type")
    public Result<List<ResourceResponse>> listByType(@RequestParam String type) {
        return Result.success(
                resourceService.listByType(ActorContextHttpAdapter.currentActor(), type));
    }

    /**
     * 执行 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:resource:upload")
    public Result<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        return Result.success(
                resourceService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("edu:resource:upload")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        request.setId(id);
        resourceService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("edu:resource:delete")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
