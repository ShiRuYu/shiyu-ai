package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 处理 知识 Operations 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/system")
@RequiredArgsConstructor
@Tag(name = "知识引擎运维")
public class KnowledgeOperationsController {

    /**
     * backupService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EmbeddedBackupService backupService;

    /**
     * 执行 知识 Operations 相关业务数据，并返回处理结果。
     *
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 知识 Operations 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:list")
    @GetMapping("/status")
    public Result<Map<String, Object>> status(
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(backupService.status());
    }

    /**
     * 执行 知识 Operations 相关业务数据，并返回处理结果。
     *
     * @param backup 用于完成本次业务处理的 backup 参数。
     * @return 返回 知识 Operations 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:edit")
    @PostMapping("/backup")
    public Result<EmbeddedBackupService.BackupResult> backup(
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(backupService.backup());
    }

    /**
     * 执行 知识 Operations 相关业务数据，并返回处理结果。
     *
     * @param check 用于完成本次业务处理的 check 参数。
     * @return 返回 知识 Operations 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:list")
    @PostMapping("/restore-check")
    public Result<EmbeddedBackupService.RestoreCheckResult> restoreCheck(
            @RequestParam String fileName,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(backupService.restoreCheck(fileName));
    }
}
