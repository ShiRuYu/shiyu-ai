package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.knowledge.web.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.common.storage.ResumableUploadHandler;
import com.shiyu.ai.common.storage.ResumableUploadService;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentUploadService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
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

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识文档")
@SaCheckPermission("knowledge:document:list")
public class KnowledgeDocumentController {

    private final EnterpriseDocumentService documentService;
    private final KnowledgeDocumentUploadService uploadService;
    private final ObjectStorage objectStorage;
    private final ResumableUploadService resumableUploadService;

    @GetMapping("/spaces/{spaceId}/documents")
    public Result<PageData<EnterpriseDocumentService.DocumentView>> page(
            @PathVariable Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lifecycleStatus,
            @RequestParam(required = false) String parseStatus,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.page(currentActor(), spaceId, pageNum, Math.min(pageSize, 100),
                keyword, lifecycleStatus, parseStatus));
    }

    @PostMapping(value = "/spaces/{spaceId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.UploadResult> upload(
            @PathVariable Long spaceId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        try {
            String originalName = file.getOriginalFilename() == null ? "document.txt" : file.getOriginalFilename();
            return Result.success(uploadService.upload(currentActor(), spaceId, title, originalName,
                    file.getContentType(), file.getBytes()));
        } catch (IOException exception) {
            throw new ServiceException("读取上传文件失败: " + exception.getMessage());
        }
    }

    @PostMapping("/spaces/{spaceId}/documents/import-url")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.UploadResult> importUrl(
            @PathVariable Long spaceId,
            @RequestBody @Valid ImportUrlRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(uploadService.importUrl(currentActor(), spaceId, request.title(), request.url()));
    }

    @PostMapping("/spaces/{spaceId}/documents/upload-sessions")
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> beginUpload(
            @PathVariable Long spaceId,
            @RequestBody @Valid ResumableUploadService.BeginRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(resumableUploadService.begin(currentUploadActor(), spaceId, request));
    }

    @GetMapping("/documents/upload-sessions/{sessionId}")
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> uploadStatus(
            @PathVariable String sessionId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(resumableUploadService.status(currentUploadActor(), sessionId));
    }

    @PostMapping(value = "/documents/upload-sessions/{sessionId}/chunks/{index}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> uploadChunk(
            @PathVariable String sessionId,
            @PathVariable int index,
            @RequestParam int totalChunks,
            @RequestPart("file") MultipartFile chunk,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        try {
            return Result.success(resumableUploadService.writeChunk(
                    currentUploadActor(), sessionId, index, totalChunks, chunk.getBytes()));
        } catch (IOException exception) {
            throw new ServiceException("读取上传分片失败: " + exception.getMessage());
        }
    }

    @PostMapping("/documents/upload-sessions/{sessionId}/complete")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.UploadResult> completeUpload(
            @PathVariable String sessionId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        ResumableUploadHandler.RegistrationResult registration = resumableUploadService.complete(
                currentUploadActor(), sessionId);
        if (!(registration.value() instanceof EnterpriseDocumentService.UploadResult result)) {
            throw new ServiceException("上传结果未完成知识文档注册");
        }
        return Result.success(result);
    }

    @DeleteMapping("/documents/upload-sessions/{sessionId}")
    @SaCheckPermission("knowledge:document:upload")
    public Result<Void> cancelUpload(
            @PathVariable String sessionId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        resumableUploadService.cancel(currentUploadActor(), sessionId);
        return Result.success();
    }

    @GetMapping("/documents/{id}")
    public Result<EnterpriseDocumentService.DocumentView> get(@PathVariable Long id,
                                                              @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                      defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.get(currentActor(), id));
    }

    @GetMapping("/documents/{id}/versions")
    public Result<List<EnterpriseDocumentService.VersionView>> versions(@PathVariable Long id,
                                                                         @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                                 defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.versions(currentActor(), id));
    }

    @PostMapping("/documents/{id}/submit")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> submit(@PathVariable Long id,
                                                                  @RequestParam(required = false) String comment,
                                                                  @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                          defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.submit(currentActor(), id, comment));
    }

    @PostMapping("/documents/{id}/approve")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> approve(@PathVariable Long id,
                                                                   @RequestParam(required = false) String comment,
                                                                   @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                           defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.approve(currentActor(), id, comment));
    }

    @PostMapping("/documents/{id}/reject")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> reject(@PathVariable Long id,
                                                                  @RequestParam(required = false) String comment,
                                                                  @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                          defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.reject(currentActor(), id, comment));
    }

    @PostMapping("/documents/{id}/publish")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> publish(@PathVariable Long id,
                                                                   @RequestParam(required = false) String comment,
                                                                   @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                           defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.publish(currentActor(), id, comment));
    }

    @PostMapping("/documents/{id}/archive")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> archive(@PathVariable Long id,
                                                                   @RequestParam(required = false) String comment,
                                                                   @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                           defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.archive(currentActor(), id, comment));
    }

    @PostMapping("/documents/{id}/versions/{versionId}/rollback")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> rollback(@PathVariable Long id,
                                                                    @PathVariable Long versionId,
                                                                    @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                            defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.rollback(currentActor(), id, versionId));
    }

    @DeleteMapping("/documents/{id}")
    @SaCheckPermission("knowledge:document:delete")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        documentService.delete(currentActor(), id);
        return Result.success();
    }

    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id,
                                          @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                  defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        EnterpriseDocumentService.DocumentView document = documentService.get(currentActor(), id);
        try (ObjectStorage.ReadableObject object = objectStorage.open(document.objectKey())) {
            String encodedName = URLEncoder.encode(object.originalName(), StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(object.contentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE : object.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName)
                    .body(object.inputStream().readAllBytes());
        } catch (IOException exception) {
            throw new ServiceException("文件预览失败: " + exception.getMessage());
        }
    }

    public record ImportUrlRequest(@NotBlank String url, String title) {
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }

    private ResumableUploadHandler.UploadActor currentUploadActor() {
        ActorContext actor = currentActor();
        return new ResumableUploadHandler.UploadActor(actor.tenantId(), actor.userId(), actor.activeRoleId(),
                actor.platformAdmin());
    }
}
