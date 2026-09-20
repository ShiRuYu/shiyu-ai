package com.shiyu.ai.model.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.model.implementation.application.service.AiPlatformService;
import com.shiyu.ai.model.implementation.infrastructure.service.ModelManager;
import com.shiyu.ai.model.implementation.web.request.AiPlatformRequest;
import com.shiyu.ai.model.implementation.web.response.AiPlatformResponse;
import com.shiyu.ai.model.implementation.web.response.AiPlatformVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 AI 平台 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Ai Platform", description = "Ai Platform")
@SaCheckPermission("agent:platform:list")
@RestController
@RequestMapping("/api/model/providers")
public class AiPlatformController {

    /**
     * aiPlatformService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiPlatformService aiPlatformService;
    /**
     * modelManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ModelManager modelManager;

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param aiPlatformService 用于完成本次业务处理的 aiPlatformService 参数。
     * @param modelManager 用于完成本次业务处理的 modelManager 参数。
     */
    public AiPlatformController(AiPlatformService aiPlatformService, ModelManager modelManager) {
        this.aiPlatformService = aiPlatformService;
        this.modelManager = modelManager;
    }

    /**
     * {@code getPage} 查询并返回当前操作所需的数据。
     *
     * @param name 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param pageNo 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Page")
    @GetMapping("/page")
    public Result<PageData<AiPlatformVO>> getPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        var result =
                aiPlatformService.pageResponse(
                        ActorContextHttpAdapter.currentActor(), pageNo, pageSize, name, code);
        var vos =
                com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                        result.getRight(), AiPlatformVO.class);
        return Result.success(new PageData<>(vos, result.getLeft()));
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Enabled 用于完成本次业务处理的 Enabled 参数。
     */
    @Operation(summary = "Get All Enabled")
    @GetMapping("/enabled")
    public Result<List<AiPlatformVO>> getAllEnabled() {
        var list = aiPlatformService.enabledResponse(ActorContextHttpAdapter.currentActor());
        return Result.success(
                com.shiyu.ai.common.core.utils.MapstructUtils.convert(list, AiPlatformVO.class));
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Options 用于完成本次业务处理的 Options 参数。
     */
    @Operation(summary = "Get Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions() {
        return Result.success(aiPlatformService.getOptions(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Id 用于定位目标业务对象的标识。
     */
    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<AiPlatformResponse> getById(@RequestParam Long id) {
        AiPlatformResponse response =
                aiPlatformService.detailResponse(ActorContextHttpAdapter.currentActor(), id);
        if (response != null) {
            return Result.success(response);
        }
        return Result.fail(BizResultCode.NOT_FOUND, "平台不存在");
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Code 用于定位或筛选目标业务对象的业务值。
     */
    @Operation(summary = "Get by Code")
    @GetMapping("/code")
    public Result<AiPlatformResponse> getByCode(@RequestParam String code) {
        AiPlatformResponse response =
                aiPlatformService.codeResponse(ActorContextHttpAdapter.currentActor(), code);
        if (response != null) {
            return Result.success(response);
        }
        return Result.fail(BizResultCode.NOT_FOUND, "平台不存在");
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Default 用于完成本次业务处理的 Default 参数。
     */
    @Operation(summary = "Get Default")
    @GetMapping("/default")
    public Result<AiPlatformResponse> getDefault() {
        AiPlatformResponse response =
                aiPlatformService.defaultResponse(ActorContextHttpAdapter.currentActor());
        if (response != null) {
            return Result.success(response);
        }
        return Result.fail(BizResultCode.NOT_FOUND, "未配置默认平台");
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Create 用于完成本次业务处理的 Create 参数。
     */
    @Operation(summary = "Create")
    @SaCheckPermission("agent:platform:create")
    @PostMapping("/create")
    public Result<AiPlatformVO> create(@Valid @RequestBody AiPlatformRequest request) {
        try {
            AiPlatformResponse created =
                    aiPlatformService.createResponse(
                            ActorContextHttpAdapter.currentActor(), request);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            created, AiPlatformVO.class));
        } catch (Exception e) {
            log.error("新增平台失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Update 用于完成本次业务处理的 Update 参数。
     */
    @Operation(summary = "Update")
    @SaCheckPermission("agent:platform:edit")
    @PostMapping("/update")
    public Result<AiPlatformVO> update(
            @RequestParam Long id, @Valid @RequestBody AiPlatformRequest request) {
        try {
            AiPlatformResponse updated =
                    aiPlatformService.updateResponse(
                            ActorContextHttpAdapter.currentActor(), id, request);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            updated, AiPlatformVO.class));
        } catch (Exception e) {
            log.error("修改平台失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Delete 用于完成本次业务处理的 Delete 参数。
     */
    @Operation(summary = "Delete")
    @SaCheckPermission("agent:platform:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        try {
            aiPlatformService.deleteById(ActorContextHttpAdapter.currentActor(), id);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除平台失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Default 用于完成本次业务处理的 Default 参数。
     */
    @Operation(summary = "Set Default")
    @SaCheckPermission("agent:platform:set-default")
    @PostMapping("/set-default")
    public Result<AiPlatformVO> setDefault(@RequestParam Long id) {
        try {
            AiPlatformResponse response =
                    aiPlatformService.setDefaultResponse(
                            ActorContextHttpAdapter.currentActor(), id);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            response, AiPlatformVO.class));
        } catch (Exception e) {
            log.error("设置默认平台失败", e);
            return Result.fail("设置失败");
        }
    }

    /**
     * 执行 AI 平台 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Reload 用于完成本次业务处理的 Reload 参数。
     */
    @Operation(summary = "Reload")
    @SaCheckPermission("agent:platform:edit")
    @PostMapping("/reload")
    public Result<Void> reload() {
        try {
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("重新加载失败", e);
            return Result.fail("重新加载失败");
        }
    }
}
