package com.shiyu.ai.common.storage.web;
import com.shiyu.ai.common.storage.file.service.FileStorageManager;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * {@code FileController} 是平台基础设施模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@Tag(name = "File", description = "文件管理")
@RestController
@RequestMapping("/api/iam/files")
@RequiredArgsConstructor
public class FileController {

    /**
     * storageManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final FileStorageManager storageManager;

    /**
     * {@code config} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "获取文件存储配置")
    @SaCheckPermission("file:list")
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(
                Map.of(
                        "currentType",
                        storageManager.type(),
                        "supportedTypes",
                        FileStorageManager.SUPPORTED_TYPES));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "获取文件列表")
    @SaCheckPermission("file:list")
    @GetMapping("/list")
    public Result<List<FileView>> list() {
        try {
            return Result.success(
                    storageManager.list(tenantNamespace()).stream().map(this::toView).toList());
        } catch (IOException ex) {
            log.error(
                    "读取文件列表失败: errorType={}, errorMessageLength={}",
                    ex.getClass().getSimpleName(),
                    messageLength(ex));
            return Result.fail("读取文件列表失败");
        }
    }

    /**
     * {@code upload} 执行当前类型定义的业务操作。
     *
     * @param file 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "上传文件")
    @SaCheckPermission("file:upload")
    @PostMapping("/upload")
    public Result<FileView> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        try (var inputStream = file.getInputStream()) {
            StoredFile storedFile =
                    storageManager.upload(
                            tenantNamespace(),
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getSize(),
                            inputStream);
            log.info(
                    "文件上传成功: keyLength={}, size={}, contentType={}",
                    storedFile.key() == null ? 0 : storedFile.key().length(),
                    file.getSize(),
                    file.getContentType());
            return Result.success(toView(storedFile));
        } catch (IOException ex) {
            log.error(
                    "文件上传失败: errorType={}, errorMessageLength={}",
                    ex.getClass().getSimpleName(),
                    messageLength(ex));
            return Result.fail("文件上传失败");
        }
    }

    /**
     * {@code download} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam String key)
            throws IOException {
        verifyTenantKey(key);
        StorageObject object;
        try {
            object = storageManager.open(key);
        } catch (FileNotFoundException ex) {
            throw new ResponseStatusException(NOT_FOUND, "文件不存在", ex);
        }
        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(object.name(), StandardCharsets.UTF_8)
                        .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(object.contentType()))
                .contentLength(object.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(object.inputStream()));
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "删除文件")
    @SaCheckPermission("file:delete")
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam String key) {
        try {
            verifyTenantKey(key);
            storageManager.delete(key);
            return Result.success(true);
        } catch (IOException ex) {
            log.error(
                    "删除文件失败: keyLength={}, errorType={}, errorMessageLength={}",
                    key == null ? 0 : key.length(),
                    ex.getClass().getSimpleName(),
                    messageLength(ex));
            return Result.fail("文件删除失败");
        }
    }

    private String tenantNamespace() {
        long tenantId = ActorContextHttpAdapter.tenantId();
        return "tenant/" + tenantId;
    }

    private void verifyTenantKey(String key) {
        String prefix = tenantNamespace() + "/";
        if (key == null || !key.startsWith(prefix)) {
            throw new ResponseStatusException(FORBIDDEN, "无权访问该文件");
        }
    }

    private int messageLength(Throwable exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }

    private FileView toView(StoredFile file) {
        String url =
                file.url() == null
                        ? "/api/iam/files/download?key="
                                + URLEncoder.encode(file.key(), StandardCharsets.UTF_8)
                        : file.url();
        return new FileView(
                file.key(),
                file.name(),
                file.size(),
                file.contentType(),
                file.lastModified(),
                url,
                file.storageType());
    }

    /**
     * {@code FileView} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param key key 属性，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param lastModified lastModified 属性，表示该记录组件承载的数据。
     * @param url url 属性，表示该记录组件承载的数据。
     * @param storageType storageType 属性，表示该记录组件承载的数据。
     */
    public record FileView(
            String key,
            String name,
            long size,
            String contentType,
            java.time.Instant lastModified,
            String url,
            String storageType) {}
}
