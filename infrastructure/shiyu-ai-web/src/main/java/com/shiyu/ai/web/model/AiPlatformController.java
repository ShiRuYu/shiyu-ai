package com.shiyu.ai.web.model;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.model.service.AiPlatformService;
import com.shiyu.ai.model.vo.AiPlatformVO;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * AI 平台管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Ai Platform", description = "Ai Platform")
@SaCheckPermission("agent:platform:list")
@RestController
@RequestMapping("/admin/platform")
public class AiPlatformController {

    private final AiPlatformService aiPlatformService;
    private final ModelManager modelManager;

    public AiPlatformController(AiPlatformService aiPlatformService, ModelManager modelManager) {
        this.aiPlatformService = aiPlatformService;
        this.modelManager = modelManager;
    }

    @Operation(summary = "Get Page")
    @GetMapping("/page")
    public Result<PageData<AiPlatformVO>> getPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        var result = aiPlatformService.pageResponse(pageNo, pageSize, name, code);
        var vos = com.shiyu.ai.common.core.utils.MapstructUtils.convert(result.getRight(), AiPlatformVO.class);
        return Result.success(new PageData<>(vos, result.getLeft()));
    }

    @Operation(summary = "Get All Enabled")
    @GetMapping("/enabled")
    public Result<List<AiPlatformVO>> getAllEnabled() {
        var list = aiPlatformService.enabledResponse();
        return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(list, AiPlatformVO.class));
    }

    @Operation(summary = "Get Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions() {
        return Result.success(aiPlatformService.getOptions());
    }

    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<AiPlatformResponse> getById(@RequestParam Long id) {
        AiPlatformResponse response = aiPlatformService.detailResponse(id);
        if (response != null) {
            return Result.success(response);
        }
        return Result.fail("平台不存在");
    }

    @Operation(summary = "Get by Code")
    @GetMapping("/code")
    public Result<AiPlatformResponse> getByCode(@RequestParam String code) {
        AiPlatformResponse response = aiPlatformService.codeResponse(code);
        if (response != null) {
            return Result.success(response);
        }
        return Result.fail("平台不存在");
    }

    @Operation(summary = "Get Default")
    @GetMapping("/default")
    public Result<AiPlatformResponse> getDefault() {
        AiPlatformResponse response = aiPlatformService.defaultResponse();
        if (response != null) {
            return Result.success(response);
        }
        return Result.fail("未配置默认平台");
    }

    @Operation(summary = "Create")
    @SaCheckPermission("agent:platform:create")
    @PostMapping("/create")
    public Result<AiPlatformVO> create(@Valid @RequestBody AiPlatformRequest request) {
        try {
            AiPlatformResponse created = aiPlatformService.createResponse(request);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(created, AiPlatformVO.class));
        } catch (Exception e) {
            log.error("新增平台失败", e);
            return Result.fail("新增失败");
        }
    }

    @Operation(summary = "Update")
    @SaCheckPermission("agent:platform:edit")
    @PostMapping("/update")
    public Result<AiPlatformVO> update(@RequestParam Long id, @Valid @RequestBody AiPlatformRequest request) {
        try {
            AiPlatformResponse updated = aiPlatformService.updateResponse(id, request);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(updated, AiPlatformVO.class));
        } catch (Exception e) {
            log.error("修改平台失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete")
    @SaCheckPermission("agent:platform:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        try {
            aiPlatformService.deleteById(id);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除平台失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Set Default")
    @SaCheckPermission("agent:platform:set-default")
    @PostMapping("/set-default")
    public Result<AiPlatformVO> setDefault(@RequestParam Long id) {
        try {
            AiPlatformResponse response = aiPlatformService.setDefaultResponse(id);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(response, AiPlatformVO.class));
        } catch (Exception e) {
            log.error("设置默认平台失败", e);
            return Result.fail("设置失败");
        }
    }

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
