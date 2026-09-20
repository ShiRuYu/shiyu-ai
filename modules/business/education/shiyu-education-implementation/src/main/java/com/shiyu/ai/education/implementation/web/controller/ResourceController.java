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
 * 处理 资源 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @GetMapping("/detail")
    public Result<ResourceResponse> getById(@RequestParam Long id) {
        return Result.success(resourceService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    @GetMapping("/list")
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
    @PostMapping("/create")
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
    @PostMapping("/update")
    @SaCheckPermission("edu:resource:upload")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody ResourceRequest request) {
        request.setId(id);
        resourceService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:resource:delete")
    public Result<Void> delete(@RequestParam Long id) {
        resourceService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
