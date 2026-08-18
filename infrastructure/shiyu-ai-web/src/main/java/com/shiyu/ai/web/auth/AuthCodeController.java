package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.api.response.AuthCodeResponse;
import com.shiyu.ai.auth.service.AuthCodeService;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for authorization-code use cases. */
@Tag(name = "Auth Code", description = "Auth Code")
@RestController
@RequestMapping("/v1/system/auth-codes")
@RequiredArgsConstructor
public class AuthCodeController {

    private final AuthCodeService service;

    @Operation(summary = "List Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/list")
    public Result<List<AuthCodeOptionVO>> list() { return Result.success(service.list()); }

    @Operation(summary = "List role auth codes")
    @SaCheckPermission("system:role:list")
    @GetMapping("/roles/list")
    public Result<List<String>> listRoleAuthCodes(@RequestParam Long roleId,
                                                  @RequestParam Long tenantId) {
        try { return Result.success(service.listRoleAuthCodes(roleId, tenantId)); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Auth code options")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/options")
    public Result<List<AuthCodeOptionVO>> options() { return Result.success(service.options()); }

    @Operation(summary = "Create Auth Code")
    @SaCheckPermission("system:auth-code:create")
    @PostMapping("/create")
    public Result<AuthCodeResponse> create(@RequestBody AuthCodeRequest authCode) {
        try { return Result.success(service.create(authCode)); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Update Auth Code")
    @SaCheckPermission("system:auth-code:update")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeRequest authCode) {
        try { return service.update(id, authCode) ? Result.success() : Result.fail("权限码不存在"); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Delete Auth Code")
    @SaCheckPermission("system:auth-code:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        try { return service.delete(id) ? Result.success() : Result.fail("权限码不存在"); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Grant role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/grant")
    public Result<Void> grant(@RequestParam Long roleId, @RequestParam Long tenantId,
                              @RequestBody List<Long> authCodeIds) {
        try { return service.grant(roleId, tenantId, authCodeIds) ? Result.success() : Result.fail("角色、作用域或权限码参数无效"); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Replace role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/replace")
    public Result<Void> replace(@RequestParam Long roleId, @RequestParam Long tenantId,
                                @RequestBody List<String> authCodes) {
        try { return service.replace(roleId, tenantId, authCodes) ? Result.success() : Result.fail("角色不属于当前租户作用域"); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Revoke role auth code")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/revoke")
    public Result<Void> revoke(@RequestParam Long roleId, @RequestParam Long tenantId,
                               @RequestParam Long authCodeId) {
        try { return service.revoke(roleId, tenantId, authCodeId) ? Result.success() : Result.fail("角色不属于当前租户作用域"); }
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @Operation(summary = "Page Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/page")
    public Result<PageData<AuthCodeOptionVO>> page(AuthCodePageRequest request) {
        return Result.success(service.page(request));
    }
}
