package com.shiyu.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.api.response.AuthCodeResponse;
import com.shiyu.ai.auth.service.AuthCodeService;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.kernel.context.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for authorization-code use cases. */
@Tag(name = "Auth Code", description = "Auth Code")
@RestController
@RequestMapping("/api/iam/auth-codes")
@RequiredArgsConstructor
public class AuthCodeController {

    private final AuthCodeService service;

    @Operation(summary = "List Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/list")
    public Result<List<AuthCodeOptionVO>> list() { return Result.success(service.list(ActorContextHttpAdapter.currentActor())); }

    @Operation(summary = "List role auth codes")
    @SaCheckPermission("system:role:list")
    @GetMapping("/roles/list")
    public Result<List<String>> listRoleAuthCodes(@RequestParam Long roleId,
                                                  @RequestParam Long tenantId) {
        return Result.success(service.listRoleAuthCodes(ActorContextHttpAdapter.currentActor(), roleId, new TenantId(tenantId)));
    }

    @Operation(summary = "Auth code options")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/options")
    public Result<List<AuthCodeOptionVO>> options() { return Result.success(service.options(ActorContextHttpAdapter.currentActor())); }

    @Operation(summary = "Create Auth Code")
    @SaCheckPermission("system:auth-code:create")
    @PostMapping("/create")
    public Result<AuthCodeResponse> create(@RequestBody AuthCodeRequest authCode) {
        return Result.success(service.create(ActorContextHttpAdapter.currentActor(), authCode));
    }

    @Operation(summary = "Update Auth Code")
    @SaCheckPermission("system:auth-code:update")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeRequest authCode) {
        return service.update(ActorContextHttpAdapter.currentActor(), id, authCode) ? Result.success() : Result.fail("权限码不存在");
    }

    @Operation(summary = "Delete Auth Code")
    @SaCheckPermission("system:auth-code:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        return service.delete(ActorContextHttpAdapter.currentActor(), id) ? Result.success() : Result.fail("权限码不存在");
    }

    @Operation(summary = "Grant role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/grant")
    public Result<Void> grant(@RequestParam Long roleId, @RequestParam Long tenantId,
                              @RequestBody List<Long> authCodeIds) {
        return service.grant(ActorContextHttpAdapter.currentActor(), roleId, new TenantId(tenantId), authCodeIds) ? Result.success() : Result.fail("角色、作用域或权限码参数无效");
    }

    @Operation(summary = "Replace role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/replace")
    public Result<Void> replace(@RequestParam Long roleId, @RequestParam Long tenantId,
                                @RequestBody List<String> authCodes) {
        return service.replace(ActorContextHttpAdapter.currentActor(), roleId, new TenantId(tenantId), authCodes) ? Result.success() : Result.fail("角色不属于当前租户作用域");
    }

    @Operation(summary = "Revoke role auth code")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/revoke")
    public Result<Void> revoke(@RequestParam Long roleId, @RequestParam Long tenantId,
                               @RequestParam Long authCodeId) {
        return service.revoke(ActorContextHttpAdapter.currentActor(), roleId, new TenantId(tenantId), authCodeId) ? Result.success() : Result.fail("角色不属于当前租户作用域");
    }

    @Operation(summary = "Page Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/page")
    public Result<PageData<AuthCodeOptionVO>> page(AuthCodePageRequest request) {
        return Result.success(service.page(ActorContextHttpAdapter.currentActor(), request));
    }
}
