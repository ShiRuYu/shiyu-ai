package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.DocumentView;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.UploadResult;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.VersionView;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.exception.ServiceException;
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
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentUploadService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * {@code KnowledgeDocumentController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识文档")
@SaCheckPermission("knowledge:document:list")
public class KnowledgeDocumentController {

    /**
     * documentService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EnterpriseDocumentService documentService;
    /**
     * uploadService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDocumentUploadService uploadService;
    /**
     * objectStorage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ObjectStorage objectStorage;
    /**
     * resumableUploadService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ResumableUploadService resumableUploadService;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param lifecycleStatus 参数值，用于执行当前操作。
     * @param parseStatus 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/spaces/{spaceId}/documents")
    public Result<PageData<EnterpriseDocumentService.DocumentView>> page(
            @PathVariable Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lifecycleStatus,
            @RequestParam(required = false) String parseStatus,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                documentService.page(
                        currentActor(),
                        spaceId,
                        pageNum,
                        Math.min(pageSize, 100),
                        keyword,
                        lifecycleStatus,
                        parseStatus));
   }

    /**
     * {@code upload} 执行当前类型定义的业务操作。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param file 参数值，用于执行当前操作。
     * @param title 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
   @PostMapping(
           value = "/spaces/{spaceId}/documents",
           consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @SaCheckPermission("knowledge:document:upload")
   public Result<EnterpriseDocumentService.UploadResult> upload(
            @PathVariable Long spaceId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        try {
            String originalName =
                    file.getOriginalFilename() == null
                            ? "document.txt"
                            : file.getOriginalFilename();
            return Result.success(
                    uploadService.upload(
                            currentActor(),
                            spaceId,
                            title,
                            originalName,
                            file.getContentType(),
                            file.getBytes()));
        } catch (IOException exception) {
            throw new ServiceException("读取上传文件失败");
        }
    }

    /**
     * {@code importUrl} 执行当前类型定义的业务操作。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/spaces/{spaceId}/documents/import-url")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.UploadResult> importUrl(
            @PathVariable Long spaceId,
            @RequestBody @Valid ImportUrlRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                uploadService.importUrl(currentActor(), spaceId, request.title(), request.url()));
    }

    /**
     * {@code beginUpload} 执行当前类型定义的业务操作。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/spaces/{spaceId}/documents/upload-sessions")
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> beginUpload(
            @PathVariable Long spaceId,
            @RequestBody @Valid ResumableUploadService.BeginRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(resumableUploadService.begin(currentUploadActor(), spaceId, request));
    }

    /**
     * {@code uploadStatus} 执行当前类型定义的业务操作。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/documents/upload-sessions/{sessionId}")
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> uploadStatus(
            @PathVariable String sessionId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(resumableUploadService.status(currentUploadActor(), sessionId));
   }

   /**
    * {@code uploadChunk} 执行当前类型定义的业务操作。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param index 参数值，用于执行当前操作。
     * @param totalChunks 参数值，用于执行当前操作。
     * @param chunk 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
    @PostMapping(
            value = "/documents/upload-sessions/{sessionId}/chunks/{index}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @SaCheckPermission("knowledge:document:upload")
   public Result<ResumableUploadService.UploadSession> uploadChunk(
            @PathVariable String sessionId,
            @PathVariable int index,
            @RequestParam int totalChunks,
            @RequestPart("file") MultipartFile chunk,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        try {
            return Result.success(
                    resumableUploadService.writeChunk(
                            currentUploadActor(), sessionId, index, totalChunks, chunk.getBytes()));
        } catch (IOException exception) {
            throw new ServiceException("读取上传分片失败");
        }
    }

    /**
     * {@code completeUpload} 执行当前类型定义的业务操作。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/upload-sessions/{sessionId}/complete")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.UploadResult> completeUpload(
            @PathVariable String sessionId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        ResumableUploadHandler.RegistrationResult registration =
                resumableUploadService.complete(currentUploadActor(), sessionId);
        if (!(registration.value() instanceof EnterpriseDocumentService.UploadResult result)) {
            throw new ServiceException("上传结果未完成知识文档注册");
        }
        return Result.success(result);
    }

    /**
     * {@code cancelUpload} 校验当前操作的输入或状态是否满足约束。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/documents/upload-sessions/{sessionId}")
    @SaCheckPermission("knowledge:document:upload")
    public Result<Void> cancelUpload(
            @PathVariable String sessionId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        resumableUploadService.cancel(currentUploadActor(), sessionId);
        return Result.success();
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/documents/{id}")
    public Result<EnterpriseDocumentService.DocumentView> get(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.get(currentActor(), id));
    }

    /**
     * {@code versions} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/documents/{id}/versions")
    public Result<List<EnterpriseDocumentService.VersionView>> versions(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.versions(currentActor(), id));
    }

    /**
     * {@code submit} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/{id}/submit")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> submit(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.submit(currentActor(), id, comment));
    }

    /**
     * {@code approve} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/{id}/approve")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.approve(currentActor(), id, comment));
    }

    /**
     * {@code reject} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/{id}/reject")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.reject(currentActor(), id, comment));
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param id 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/{id}/publish")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> publish(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.publish(currentActor(), id, comment));
    }

    /**
     * {@code archive} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/{id}/archive")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> archive(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.archive(currentActor(), id, comment));
    }

    /**
     * {@code rollback} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/documents/{id}/versions/{versionId}/rollback")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> rollback(
            @PathVariable Long id,
            @PathVariable Long versionId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.rollback(currentActor(), id, versionId));
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/documents/{id}")
    @SaCheckPermission("knowledge:document:delete")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        documentService.delete(currentActor(), id);
        return Result.success();
    }

    /**
     * {@code preview} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<byte[]> preview(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        EnterpriseDocumentService.DocumentView document = documentService.get(currentActor(), id);
        try (ObjectStorage.ReadableObject object = objectStorage.open(document.objectKey())) {
            String encodedName =
                    URLEncoder.encode(object.originalName(), StandardCharsets.UTF_8)
                            .replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    object.contentType() == null
                                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                                            : object.contentType()))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename*=UTF-8''" + encodedName)
                    .body(object.inputStream().readAllBytes());
        } catch (IOException exception) {
            throw new ServiceException("文件预览失败");
        }
    }

    /**
     * {@code ImportUrlRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param url url 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     */
    public record ImportUrlRequest(@NotBlank String url, String title) {}

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }

    private ResumableUploadHandler.UploadActor currentUploadActor() {
        ActorContext actor = currentActor();
        return new ResumableUploadHandler.UploadActor(
                actor.tenantId(), actor.userId(), actor.activeRoleId(), actor.platformAdmin());
    }
}
