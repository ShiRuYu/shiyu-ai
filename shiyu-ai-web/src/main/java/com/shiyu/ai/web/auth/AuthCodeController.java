package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.auth.service.impl.AuthCodeServiceImpl;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapter for authorization-code use cases. */
@Tag(name = "Auth Code", description = "Auth Code")
@RestController
@RequestMapping("/auth-code")
@RequiredArgsConstructor
public class AuthCodeController {

    private final AuthCodeServiceImpl service;

    @Operation(summary = "List Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/list")
    public Result<List<AuthCodeOptionVO>> list() { return service.list(); }

    @Operation(summary = "List role auth codes")
    @SaCheckPermission("system:role:list")
    @GetMapping("/roles/list")
    public Result<List<String>> listRoleAuthCodes(@RequestParam Long roleId,
                                                  @RequestParam Long tenantId) {
        return service.listRoleAuthCodes(roleId, tenantId);
    }

    @Operation(summary = "Auth code options")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/options")
    public Result<List<AuthCodeOptionVO>> options() { return service.options(); }

    @Operation(summary = "Create Auth Code")
    @SaCheckPermission("system:auth-code:create")
    @PostMapping("/create")
    public Result<AuthCodeDO> create(@RequestBody AuthCodeDO authCode) { return service.create(authCode); }

    @Operation(summary = "Update Auth Code")
    @SaCheckPermission("system:auth-code:update")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeDO authCode) {
        return service.update(id, authCode);
    }

    @Operation(summary = "Delete Auth Code")
    @SaCheckPermission("system:auth-code:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) { return service.delete(id); }

    @Operation(summary = "Grant role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/grant")
    public Result<Void> grant(@RequestParam Long roleId, @RequestParam Long tenantId,
                              @RequestBody List<Long> authCodeIds) {
        return service.grant(roleId, tenantId, authCodeIds);
    }

    @Operation(summary = "Replace role auth codes")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/replace")
    public Result<Void> replace(@RequestParam Long roleId, @RequestParam Long tenantId,
                                @RequestBody List<String> authCodes) {
        return service.replace(roleId, tenantId, authCodes);
    }

    @Operation(summary = "Revoke role auth code")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/revoke")
    public Result<Void> revoke(@RequestParam Long roleId, @RequestParam Long tenantId,
                               @RequestParam Long authCodeId) {
        return service.revoke(roleId, tenantId, authCodeId);
    }

    @Operation(summary = "Page Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/page")
    public Result<PageData<AuthCodeOptionVO>> page(AuthCodePageRequest request) {
        return service.page(request);
    }
}
