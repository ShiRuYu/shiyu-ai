package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import com.shiyu.ai.dal.auth.mapper.AuthCodeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限码管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Auth Code", description = "Auth Code")
@RestController
@RequestMapping("/auth-code")
public class AuthCodeController {

    private final AuthCodeMapper authCodeMapper;

    public AuthCodeController(AuthCodeMapper authCodeMapper) {
        this.authCodeMapper = authCodeMapper;
    }

    @Operation(summary = "List Auth Codes")
    @GetMapping("/list")
    public Result<List<AuthCodeDO>> list() {
        return Result.success(authCodeMapper.selectAll());
    }

    @Operation(summary = "Create Auth Code")
    @PostMapping("/create")
    public Result<AuthCodeDO> create(@RequestBody AuthCodeDO authCode) {
        authCode.setStatus(1);
        authCode.setDelFlag(0);
        authCode.setCreateTime(LocalDateTime.now());
        authCodeMapper.insertSelective(authCode);
        return Result.success(authCode);
    }

    @Operation(summary = "Update Auth Code")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeDO authCode) {
        authCode.setId(id);
        authCode.setUpdateTime(LocalDateTime.now());
        authCodeMapper.update(authCode);
        return Result.success();
    }

    @Operation(summary = "Delete Auth Code")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        authCodeMapper.deleteById(id);
        return Result.success();
    }
}
