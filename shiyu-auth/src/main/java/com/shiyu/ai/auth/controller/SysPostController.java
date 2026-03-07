package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysPostBO;
import com.shiyu.ai.auth.service.SysPostService;
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
 * 岗位管理控制器
 *
 * @author shiyu-ai
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class SysPostController {

    private final SysPostService sysPostService;

    /**
     * 获取岗位列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysPostBO>>> getPosts(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysPostService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/{postId}")
    public Result<SysPostBO> getPost(@PathVariable Long postId) {
        return Result.success(sysPostService.getById(postId));
    }

    /**
     * 创建岗位
     */
    @PostMapping
    public Result<SysPostBO> createPost(@RequestBody SysPostBO sysPostBO) {
        return Result.success(sysPostService.create(sysPostBO));
    }

    /**
     * 更新岗位
     */
    @PutMapping("/{postId}")
    public Result<SysPostBO> updatePost(@PathVariable Long postId, @RequestBody SysPostBO sysPostBO) {
        sysPostBO.setPostId(postId);
        return Result.success(sysPostService.update(sysPostBO));
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        sysPostService.deleteById(postId);
        return Result.success();
    }
}
