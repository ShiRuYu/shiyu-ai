package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/knowledge/v2/system")
@RequiredArgsConstructor
@Tag(name = "知识引擎运维 V2")
@SaCheckPermission("system:tenant:update")
public class KnowledgeOperationsV2Controller {

    private final EmbeddedBackupService backupService;

    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        return Result.success(backupService.status());
    }

    @PostMapping("/backup")
    public Result<EmbeddedBackupService.BackupResult> backup() {
        return Result.success(backupService.backup());
    }

    @PostMapping("/restore-check")
    public Result<EmbeddedBackupService.RestoreCheckResult> restoreCheck(
            @RequestParam String fileName) {
        return Result.success(backupService.restoreCheck(fileName));
    }
}
