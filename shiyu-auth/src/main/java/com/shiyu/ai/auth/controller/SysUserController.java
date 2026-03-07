package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.domain.vo.SysUserVO;
import com.shiyu.ai.auth.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author shiyu-ai
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 获取用户列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysUserVO>>> getUsers(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysUserService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<SysUserVO> getUser(@PathVariable Long userId) {
        return Result.success(sysUserService.getById(userId));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<SysUserVO> createUser(@RequestBody SysUserBO sysUserBO) {
        return Result.success(sysUserService.create(sysUserBO));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public Result<SysUserVO> updateUser(@PathVariable Long userId, @RequestBody SysUserBO sysUserBO) {
        sysUserBO.setUserId(userId);
        return Result.success(sysUserService.update(sysUserBO));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        sysUserService.deleteById(userId);
        return Result.success();
    }
}
