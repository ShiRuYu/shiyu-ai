package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.storage.ContentSecurityScanner;
import com.shiyu.ai.knowledge.storage.ObjectStorage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@RestController
@RequestMapping("/knowledge/v2")
@RequiredArgsConstructor
@Tag(name = "知识文档 V2")
@SaCheckPermission("knowledge:document:list")
public class KnowledgeDocumentV2Controller {

    private final EnterpriseDocumentService documentService;
    private final ObjectStorage objectStorage;
    private final ContentSecurityScanner securityScanner;

    @GetMapping("/spaces/{spaceId}/documents")
    public Result<PageData<EnterpriseDocumentService.DocumentView>> page(
            @PathVariable Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lifecycleStatus) {
        return Result.success(documentService.page(spaceId, pageNum,
                Math.min(pageSize, 100), keyword, lifecycleStatus));
    }

    @PostMapping(value = "/spaces/{spaceId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.UploadResult> upload(
            @PathVariable Long spaceId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title) {
        String originalName = file.getOriginalFilename() == null ? "document.txt"
                : file.getOriginalFilename();
        try {
            byte[] content = file.getBytes();
            securityScanner.validate(originalName, file.getContentType(), content);
            String checksum = sha256(content);
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            if (tenantId == null) throw new ServiceException("当前租户上下文不存在");
            String namespace = "knowledge/" + tenantId + "/" + spaceId;
            ObjectStorage.StoredObject stored = objectStorage.put(namespace, originalName,
                    file.getContentType(), content.length,
                    new java.io.ByteArrayInputStream(content));
            EnterpriseDocumentService.StoredFileRequest request =
                    new EnterpriseDocumentService.StoredFileRequest(spaceId,
                            title == null || title.isBlank() ? originalName : title,
                            originalName, stored.objectKey(), stored.provider(),
                            stored.contentType(), stored.size(), checksum);
            EnterpriseDocumentService.UploadResult result = documentService.registerStoredFile(request);
            if (result.duplicate()) objectStorage.delete(stored.objectKey());
            return Result.success(result);
        } catch (IOException exception) {
            throw new ServiceException("文件存储失败: " + exception.getMessage());
        }
    }

    @GetMapping("/documents/{id}")
    public Result<EnterpriseDocumentService.DocumentView> get(@PathVariable Long id) {
        return Result.success(documentService.get(id));
    }

    @GetMapping("/documents/{id}/versions")
    public Result<List<EnterpriseDocumentService.VersionView>> versions(@PathVariable Long id) {
        return Result.success(documentService.versions(id));
    }

    @PostMapping("/documents/{id}/submit")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> submit(
            @PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(documentService.submit(id, comment));
    }

    @PostMapping("/documents/{id}/approve")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> approve(
            @PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(documentService.approve(id, comment));
    }

    @PostMapping("/documents/{id}/reject")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> reject(
            @PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(documentService.reject(id, comment));
    }

    @PostMapping("/documents/{id}/publish")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> publish(
            @PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(documentService.publish(id, comment));
    }

    @PostMapping("/documents/{id}/versions/{versionId}/rollback")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> rollback(
            @PathVariable Long id, @PathVariable Long versionId) {
        return Result.success(documentService.rollback(id, versionId));
    }

    @DeleteMapping("/documents/{id}")
    @SaCheckPermission("knowledge:document:delete")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        EnterpriseDocumentService.DocumentView document = documentService.get(id);
        try (ObjectStorage.ReadableObject object = objectStorage.open(document.objectKey())) {
            String encodedName = URLEncoder.encode(object.originalName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(object.contentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE : object.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename*=UTF-8''" + encodedName)
                    .body(object.inputStream().readAllBytes());
        } catch (IOException exception) {
            throw new ServiceException("文件预览失败: " + exception.getMessage());
        }
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
