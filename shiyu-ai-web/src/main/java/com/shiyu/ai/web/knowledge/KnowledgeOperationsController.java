package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/knowledge/system")
@RequiredArgsConstructor
@Tag(name = "知识引擎运维")
@SaCheckPermission("system:tenant:update")
public class KnowledgeOperationsController {

    private final EmbeddedBackupService backupService;

    @GetMapping("/status")
    public Result<Map<String, Object>> status(
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(backupService.status());
    }

    @PostMapping("/backup")
    public Result<EmbeddedBackupService.BackupResult> backup(
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(backupService.backup());
    }

    @PostMapping("/restore-check")
    public Result<EmbeddedBackupService.RestoreCheckResult> restoreCheck(
            @RequestParam String fileName,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(backupService.restoreCheck(fileName));
    }
}
