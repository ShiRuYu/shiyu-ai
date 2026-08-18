package com.shiyu.ai.web.common;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.storage.FileStorageManager;
import com.shiyu.ai.common.storage.StorageObject;
import com.shiyu.ai.common.storage.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Tag(name = "File", description = "文件管理")
@RestController
@RequestMapping("/v1/system/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageManager storageManager;

    @Operation(summary = "获取文件存储配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(Map.of(
                "currentType", storageManager.type(),
                "supportedTypes", FileStorageManager.SUPPORTED_TYPES));
    }

    @Operation(summary = "获取文件列表")
    @GetMapping("/list")
    public Result<List<FileView>> list() {
        try {
            return Result.success(storageManager.list(tenantNamespace()).stream().map(this::toView).toList());
        } catch (IOException ex) {
            log.error("读取文件列表失败", ex);
            return Result.fail(ex.getMessage());
        }
    }

    @Operation(summary = "上传文件")
    @SaCheckPermission("file:upload")
    @PostMapping("/upload")
    public Result<FileView> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        try (var inputStream = file.getInputStream()) {
            StoredFile storedFile = storageManager.upload(
                    tenantNamespace(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    inputStream);
            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), storedFile.key());
            return Result.success(toView(storedFile));
        } catch (IOException ex) {
            log.error("文件上传失败", ex);
            return Result.fail(ex.getMessage());
        }
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam String key) throws IOException {
        verifyTenantKey(key);
        StorageObject object;
        try {
            object = storageManager.open(key);
        } catch (FileNotFoundException ex) {
            throw new ResponseStatusException(NOT_FOUND, "文件不存在", ex);
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(object.name(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(object.contentType()))
                .contentLength(object.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(object.inputStream()));
    }

    @Operation(summary = "删除文件")
    @SaCheckPermission("file:delete")
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam String key) {
        try {
            verifyTenantKey(key);
            storageManager.delete(key);
            return Result.success(true);
        } catch (IOException ex) {
            log.error("删除文件失败: {}", key, ex);
            return Result.fail(ex.getMessage());
        }
    }

    private String tenantNamespace() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("当前租户上下文不存在");
        }
        return "tenant/" + tenantId;
    }

    private void verifyTenantKey(String key) {
        String prefix = tenantNamespace() + "/";
        if (key == null || !key.startsWith(prefix)) {
            throw new ResponseStatusException(FORBIDDEN, "无权访问该文件");
        }
    }

    private FileView toView(StoredFile file) {
        String url = file.url() == null
                ? "/api/v1/system/files/download?key=" + URLEncoder.encode(file.key(), StandardCharsets.UTF_8)
                : file.url();
        return new FileView(file.key(), file.name(), file.size(), file.contentType(),
                file.lastModified(), url, file.storageType());
    }

    public record FileView(String key, String name, long size, String contentType,
                           java.time.Instant lastModified, String url, String storageType) {
    }
}
