package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.api.response.AuthCodeResponse;
import com.shiyu.ai.iam.implementation.request.AuthCodePageRequest;
import com.shiyu.ai.iam.implementation.service.AuthCodeService;
import com.shiyu.ai.iam.implementation.vo.AuthCodeOptionVO;
import com.shiyu.ai.kernel.context.TenantId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuthCodeController 控制器，负责处理身份与访问领域相关 HTTP 请求并返回响应。
 */
@Tag(name = "Auth Code", description = "Auth Code")
@RestController
@RequestMapping("/api/iam/auth-codes")
@RequiredArgsConstructor
public class AuthCodeController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final AuthCodeService service;

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "List Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/list")
    public Result<List<AuthCodeOptionVO>> list() {
        return Result.success(service.list(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * {@code listRoleAuthCodes} 查询并返回当前操作所需的数据。
     *
     * @param roleId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "List role auth codes")
    @SaCheckPermission("system:role:list")
    @GetMapping("/roles/list")
    public Result<List<String>> listRoleAuthCodes(
            @RequestParam Long roleId, @RequestParam Long tenantId) {
        return Result.success(
                service.listRoleAuthCodes(
                        ActorContextHttpAdapter.currentActor(), roleId, new TenantId(tenantId)));
    }

    /**
     * {@code options} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Auth code options")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/options")
    public Result<List<AuthCodeOptionVO>> options() {
        return Result.success(service.options(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param authCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Create Auth Code")
    @SaCheckPermission("system:auth-code:create")
    @PostMapping("/create")
    public Result<AuthCodeResponse> create(@RequestBody AuthCodeRequest authCode) {
        return Result.success(service.create(ActorContextHttpAdapter.currentActor(), authCode));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param authCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Update Auth Code")
    @SaCheckPermission("system:auth-code:update")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeRequest authCode) {
        return service.update(ActorContextHttpAdapter.currentActor(), id, authCode)
                ? Result.success()
                : Result.fail("权限码不存在");
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Delete Auth Code")
    @SaCheckPermission("system:auth-code:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        return service.delete(ActorContextHttpAdapter.currentActor(), id)
                ? Result.success()
                : Result.fail("权限码不存在");
    }

    /**
     * {@code grant} 执行当前类型定义的业务操作。
     *
     * @param roleId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param authCodeIds 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Grant role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/grant")
    public Result<Void> grant(
            @RequestParam Long roleId,
            @RequestParam Long tenantId,
            @RequestBody List<Long> authCodeIds) {
        return service.grant(
                        ActorContextHttpAdapter.currentActor(),
                        roleId,
                        new TenantId(tenantId),
                        authCodeIds)
                ? Result.success()
                : Result.fail("角色、作用域或权限码参数无效");
    }

    /**
     * {@code replace} 执行当前类型定义的业务操作。
     *
     * @param roleId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param authCodes 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Replace role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/replace")
    public Result<Void> replace(
            @RequestParam Long roleId,
            @RequestParam Long tenantId,
            @RequestBody List<String> authCodes) {
        return service.replace(
                        ActorContextHttpAdapter.currentActor(),
                        roleId,
                        new TenantId(tenantId),
                        authCodes)
                ? Result.success()
                : Result.fail("角色不属于当前租户作用域");
    }

    /**
     * {@code revoke} 执行当前类型定义的业务操作。
     *
     * @param roleId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param authCodeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Revoke role auth code")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/revoke")
    public Result<Void> revoke(
            @RequestParam Long roleId, @RequestParam Long tenantId, @RequestParam Long authCodeId) {
        return service.revoke(
                        ActorContextHttpAdapter.currentActor(),
                        roleId,
                        new TenantId(tenantId),
                        authCodeId)
                ? Result.success()
                : Result.fail("角色不属于当前租户作用域");
    }

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Page Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/page")
    public Result<PageData<AuthCodeOptionVO>> page(AuthCodePageRequest request) {
        return Result.success(service.page(ActorContextHttpAdapter.currentActor(), request));
    }
}
