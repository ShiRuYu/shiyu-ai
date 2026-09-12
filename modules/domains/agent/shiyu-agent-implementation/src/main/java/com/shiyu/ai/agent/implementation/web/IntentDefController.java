package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.request.IntentDefRequest;
import com.shiyu.ai.agent.implementation.service.IntentDefService;
import com.shiyu.ai.agent.implementation.vo.IntentDefVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code IntentDefController} 是智能体模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/agent/intents")
@RequiredArgsConstructor
public class IntentDefController {
    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final IntentDefService service;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     * @param pageNo 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("agent:intent:list")
    @GetMapping("/page")
    public Result<PageData<IntentDefVO>> page(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        var p =
                service.pageView(
                        ActorContextHttpAdapter.currentActor(),
                        pageNo,
                        pageSize,
                        agentId,
                        name,
                        code,
                        category);
        return Result.success(new PageData<>(p.getRight(), p.getLeft()));
    }

    /**
     * {@code detail} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("agent:intent:list")
    @GetMapping("/detail")
    public Result<IntentDefVO> detail(@RequestParam Long id) {
        var v = service.detailView(ActorContextHttpAdapter.currentActor(), id);
        return v == null ? Result.fail("意图定义不存在") : Result.success(v);
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("agent:intent:create")
    @PostMapping("/create")
    public Result<IntentDefVO> create(@Valid @RequestBody IntentDefRequest r) {
        return Result.success(service.create(ActorContextHttpAdapter.currentActor(), r));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("agent:intent:create")
    @PostMapping("/update")
    public Result<IntentDefVO> update(
            @RequestParam Long id, @Valid @RequestBody IntentDefRequest r) {
        return Result.success(service.update(ActorContextHttpAdapter.currentActor(), id, r));
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("agent:intent:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        service.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    /**
     * {@code deleteBatch} 释放或移除当前操作涉及的资源。
     *
     * @param ids 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("agent:intent:delete")
    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        service.deleteByIds(ActorContextHttpAdapter.currentActor(), ids);
        return Result.success();
    }

    /**
     * {@code options} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> options() {
        return Result.success(service.listAllOptions(ActorContextHttpAdapter.currentActor()));
    }
}
