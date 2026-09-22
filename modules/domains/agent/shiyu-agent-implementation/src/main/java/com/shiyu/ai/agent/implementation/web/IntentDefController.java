package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.request.IntentDefRequest;
import com.shiyu.ai.agent.implementation.service.IntentDefService;
import com.shiyu.ai.agent.implementation.vo.IntentDefVO;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 Intent Def 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    @SaCheckPermission("agent:intent:list")
    @GetMapping
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
     * 执行 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("agent:intent:list")
    @GetMapping("/{id}")
    public Result<IntentDefVO> detail(@PathVariable Long id) {
        var v = service.detailView(ActorContextHttpAdapter.currentActor(), id);
        return v == null ? Result.fail("意图定义不存在") : Result.success(v);
    }

    /**
     * 执行 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @SaCheckPermission("agent:intent:create")
    @PostMapping
    public Result<IntentDefVO> create(@Valid @RequestBody IntentDefRequest r) {
        return Result.success(service.create(ActorContextHttpAdapter.currentActor(), r));
    }

    /**
     * 执行 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @SaCheckPermission("agent:intent:edit")
    @PutMapping("/{id}")
    public Result<IntentDefVO> update(
            @PathVariable Long id, @Valid @RequestBody IntentDefRequest r) {
        return Result.success(service.update(ActorContextHttpAdapter.currentActor(), id, r));
    }

    /**
     * 执行 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @SaCheckPermission("agent:intent:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    /**
     * 执行 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @SaCheckPermission("agent:intent:delete")
    @DeleteMapping
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        service.deleteByIds(ActorContextHttpAdapter.currentActor(), ids);
        return Result.success();
    }

    /**
     * 查询 Intent Def 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param options 用于完成本次业务处理的 options 参数。
     */
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> options() {
        return Result.success(service.listAllOptions(ActorContextHttpAdapter.currentActor()));
    }
}
