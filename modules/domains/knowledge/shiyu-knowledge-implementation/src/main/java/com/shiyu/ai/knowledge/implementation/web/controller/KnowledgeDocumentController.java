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
import com.shiyu.ai.common.storage.file.service.ResumableUploadService;
import com.shiyu.ai.common.storage.file.port.ResumableUploadHandler;
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
 * 处理 知识 文档 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param documents 用于完成本次业务处理的 documents 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param url 用于完成本次业务处理的 url 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param sessions 用于完成本次业务处理的 sessions 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param sessionId 用于定位session的标识。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param complete 用于完成本次业务处理的 complete 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 校验或判断 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param sessionId 用于定位session的标识。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param versions 用于完成本次业务处理的 versions 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param submit 用于完成本次业务处理的 submit 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param approve 用于完成本次业务处理的 approve 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param reject 用于完成本次业务处理的 reject 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 发布或发送 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param publish 用于完成本次业务处理的 publish 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param archive 用于完成本次业务处理的 archive 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param rollback 用于完成本次业务处理的 rollback 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 删除或移除 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 执行 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param preview 用于完成本次业务处理的 preview 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 封装 Import Url 相关的不可变数据及其字段约束。
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
