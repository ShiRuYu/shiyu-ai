package com.shiyu.ai.agent.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.service.IntentDefService;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;

@RestController
@RequestMapping("/api/agent/intents")
@RequiredArgsConstructor
public class IntentDefController {
    private final IntentDefService service;
    @SaCheckPermission("agent:intent:list") @GetMapping("/page") public Result<PageData<IntentDefVO>> page(@RequestParam(required=false) String agentId,@RequestParam(required=false) String name,@RequestParam(required=false) String code,@RequestParam(required=false) String category,@RequestParam(defaultValue="1") Integer pageNo,@RequestParam(defaultValue="10") Integer pageSize){var p=service.pageView(ActorContextHttpAdapter.currentActor(),pageNo,pageSize,agentId,name,code,category);return Result.success(new PageData<>(p.getRight(),p.getLeft()));}
    @SaCheckPermission("agent:intent:list") @GetMapping("/detail") public Result<IntentDefVO> detail(@RequestParam Long id){var v=service.detailView(ActorContextHttpAdapter.currentActor(),id);return v==null?Result.fail("意图定义不存在"):Result.success(v);}
    @SaCheckPermission("agent:intent:create") @PostMapping("/create") public Result<IntentDefVO> create(@Valid @RequestBody IntentDefRequest r){return Result.success(service.create(ActorContextHttpAdapter.currentActor(),r));}
    @SaCheckPermission("agent:intent:create") @PostMapping("/update") public Result<IntentDefVO> update(@RequestParam Long id,@Valid @RequestBody IntentDefRequest r){return Result.success(service.update(ActorContextHttpAdapter.currentActor(),id,r));}
    @SaCheckPermission("agent:intent:delete") @PostMapping("/delete") public Result<Void> delete(@RequestParam Long id){service.deleteById(ActorContextHttpAdapter.currentActor(),id);return Result.success();}
    @SaCheckPermission("agent:intent:delete") @PostMapping("/batch-delete") public Result<Void> deleteBatch(@RequestBody List<Long> ids){service.deleteByIds(ActorContextHttpAdapter.currentActor(),ids);return Result.success();}
    @GetMapping("/options") public Result<List<IdNameOptionVO>> options(){return Result.success(service.listAllOptions(ActorContextHttpAdapter.currentActor()));}
}
