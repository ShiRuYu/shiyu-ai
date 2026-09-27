package com.shiyu.ai.runtimeconsole.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/console/api/config")
public final class ConfigConsoleController {

    private final ConfigService configs;

    public ConfigConsoleController(ConfigService configs) {
        this.configs = configs;
    }

    @GetMapping
    public ConfigService.ConfigResponse describe() {
        return configs.describe();
    }

    @PostMapping("/validate")
    public ValidationResponse validate(@RequestBody ConfigChangeSet change) {
        var issues = configs.validate(change);
        return new ValidationResponse(issues.isEmpty(), issues);
    }

    @PostMapping
    public ConfigApplyResult save(@RequestBody ConfigChangeSet change) {
        try {
            return configs.save(change);
        } catch (ConfigService.ConfigValidationException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (ConfigSnapshotStore.VersionConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (DpapiSecretProtector.SecretProtectionException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/restore-last-applied")
    public ConfigApplyResult restore(@RequestBody RestoreRequest request) {
        try {
            return configs.restoreLastApplied(request.expectedVersion());
        } catch (ConfigSnapshotStore.VersionConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    public record ValidationResponse(boolean valid, java.util.List<String> issues) {}
    public record RestoreRequest(long expectedVersion) {}
}
