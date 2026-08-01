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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识文档")
@SaCheckPermission("knowledge:document:list")
public class KnowledgeDocumentController {

    private static final long MAX_IMPORT_BYTES = 200L * 1024 * 1024;

    private final EnterpriseDocumentService documentService;
    private final ObjectStorage objectStorage;
    private final ContentSecurityScanner securityScanner;
    private final ResumableUploadService resumableUploadService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

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
        return Result.success(documentService.page(spaceId, pageNum,
                Math.min(pageSize, 100), keyword, lifecycleStatus, parseStatus));
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
        String originalName = file.getOriginalFilename() == null ? "document.txt"
                : file.getOriginalFilename();
        ObjectStorage.StoredObject stored = null;
        try {
            byte[] content = file.getBytes();
            securityScanner.validate(originalName, file.getContentType(), content);
            String checksum = sha256(content);
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            if (tenantId == null) throw new ServiceException("当前租户上下文不存在");
            String namespace = "knowledge/" + tenantId + "/" + spaceId;
            stored = objectStorage.put(namespace, originalName,
                    file.getContentType(), content.length,
                    new java.io.ByteArrayInputStream(content));
            EnterpriseDocumentService.StoredFileRequest request =
                    new EnterpriseDocumentService.StoredFileRequest(spaceId,
                            title == null || title.isBlank() ? originalName : title,
                            originalName, stored.objectKey(), stored.provider(),
                            stored.contentType(), stored.size(), checksum);
            try {
                EnterpriseDocumentService.UploadResult result = documentService.registerStoredFile(request);
                if (result.duplicate()) objectStorage.delete(stored.objectKey());
                return Result.success(result);
            } catch (RuntimeException exception) {
                deleteQuietly(stored);
                throw exception;
            }
        } catch (IOException exception) {
            throw new ServiceException("文件存储失败: " + exception.getMessage());
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
        URI uri;
        ObjectStorage.StoredObject stored = null;
        try {
            uri = URI.create(request.url().trim());
            validateExternalUrl(uri);
            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "text/plain,text/html,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,*/*")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException("网页内容获取失败，HTTP 状态码: " + response.statusCode());
            }
            byte[] content = response.body();
            if (content.length == 0 || content.length > MAX_IMPORT_BYTES) {
                throw new ServiceException("网页内容为空或超过 200 MB 限制");
            }
            String originalName = fileName(uri);
            String contentType = response.headers().firstValue("Content-Type")
                    .map(value -> value.split(";", 2)[0].trim())
                    .orElse("text/html");
            securityScanner.validate(originalName, contentType, content);
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            if (tenantId == null) throw new ServiceException("当前租户上下文不存在");
            stored = objectStorage.put(
                    "knowledge/" + tenantId + "/" + spaceId,
                    originalName, contentType, content.length,
                    new java.io.ByteArrayInputStream(content));
            EnterpriseDocumentService.StoredFileRequest storedRequest =
                    new EnterpriseDocumentService.StoredFileRequest(spaceId,
                            request.title() == null || request.title().isBlank()
                                    ? originalName : request.title().trim(),
                            originalName, stored.objectKey(), stored.provider(),
                            stored.contentType(), stored.size(), sha256(content));
            try {
                EnterpriseDocumentService.UploadResult result = documentService.registerStoredFile(storedRequest);
                if (result.duplicate()) objectStorage.delete(stored.objectKey());
                return Result.success(result);
            } catch (RuntimeException exception) {
                deleteQuietly(stored);
                throw exception;
            }
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("URL 格式不正确");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ServiceException("网页内容获取被中断");
        } catch (IOException exception) {
            throw new ServiceException("网页内容获取失败: " + exception.getMessage());
        }
    }

    @PostMapping("/spaces/{spaceId}/documents/upload-sessions")
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> beginUpload(
            @PathVariable Long spaceId,
            @RequestBody @Valid ResumableUploadService.BeginRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(resumableUploadService.begin(spaceId, request));
    }

    @GetMapping("/documents/upload-sessions/{sessionId}")
    @SaCheckPermission("knowledge:document:upload")
    public Result<ResumableUploadService.UploadSession> uploadStatus(
            @PathVariable String sessionId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(resumableUploadService.status(sessionId));
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
                    sessionId, index, totalChunks, chunk.getBytes()));
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
        return Result.success(resumableUploadService.complete(sessionId));
    }

    @DeleteMapping("/documents/upload-sessions/{sessionId}")
    @SaCheckPermission("knowledge:document:upload")
    public Result<Void> cancelUpload(
            @PathVariable String sessionId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        resumableUploadService.cancel(sessionId);
        return Result.success();
    }

    @GetMapping("/documents/{id}")
    public Result<EnterpriseDocumentService.DocumentView> get(@PathVariable Long id,
                                                              @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                      defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.get(id));
    }

    @GetMapping("/documents/{id}/versions")
    public Result<List<EnterpriseDocumentService.VersionView>> versions(@PathVariable Long id,
                                                                         @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                                 defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.versions(id));
    }

    @PostMapping("/documents/{id}/submit")
    @SaCheckPermission("knowledge:document:upload")
    public Result<EnterpriseDocumentService.DocumentView> submit(
            @PathVariable Long id, @RequestParam(required = false) String comment,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.submit(id, comment));
    }

    @PostMapping("/documents/{id}/approve")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> approve(
            @PathVariable Long id, @RequestParam(required = false) String comment,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.approve(id, comment));
    }

    @PostMapping("/documents/{id}/reject")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> reject(
            @PathVariable Long id, @RequestParam(required = false) String comment,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.reject(id, comment));
    }

    @PostMapping("/documents/{id}/publish")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> publish(
            @PathVariable Long id, @RequestParam(required = false) String comment,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.publish(id, comment));
    }

    @PostMapping("/documents/{id}/archive")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> archive(
            @PathVariable Long id, @RequestParam(required = false) String comment,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.archive(id, comment));
    }

    @PostMapping("/documents/{id}/versions/{versionId}/rollback")
    @SaCheckPermission("knowledge:edit")
    public Result<EnterpriseDocumentService.DocumentView> rollback(
            @PathVariable Long id, @PathVariable Long versionId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(documentService.rollback(id, versionId));
    }

    @DeleteMapping("/documents/{id}")
    @SaCheckPermission("knowledge:document:delete")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        documentService.delete(id);
        return Result.success();
    }

    @GetMapping("/documents/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id,
                                          @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                  defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
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

    private void deleteQuietly(ObjectStorage.StoredObject stored) {
        if (stored == null) {
            return;
        }
        try {
            objectStorage.delete(stored.objectKey());
        } catch (IOException ignored) {
            // The original registration exception is more useful to the caller.
        }
    }

    private void validateExternalUrl(URI uri) throws IOException {
        if (uri.getScheme() == null || (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null) {
            throw new ServiceException("仅支持 http/https URL");
        }
        for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                    || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                    || address.isMulticastAddress()) {
                throw new ServiceException("不允许访问内网或本机地址");
            }
        }
    }

    private String fileName(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank() || path.endsWith("/")) return "web-page.html";
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.isBlank() ? "web-page.html" : name;
    }

    public record ImportUrlRequest(@NotBlank String url, String title) {
    }
}
