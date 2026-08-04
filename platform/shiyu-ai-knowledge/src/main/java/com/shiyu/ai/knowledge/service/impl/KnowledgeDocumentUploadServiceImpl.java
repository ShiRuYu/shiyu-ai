package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.storage.ContentSecurityScanner;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentUploadService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class KnowledgeDocumentUploadServiceImpl implements KnowledgeDocumentUploadService {

    private static final long MAX_IMPORT_BYTES = 200L * 1024 * 1024;

    private final ObjectStorage objectStorage;
    private final ContentSecurityScanner securityScanner;
    private final EnterpriseDocumentService documentService;
    private final KnowledgeSpaceService spaceService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public EnterpriseDocumentService.UploadResult upload(Long spaceId, String title,
                                                           String originalName, String contentType,
                                                           byte[] content) {
        requireEditor(spaceId);
        securityScanner.validate(originalName, contentType, content);
        String checksum = sha256(content);
        ObjectStorage.StoredObject stored = null;
        try {
            stored = objectStorage.put(namespace(spaceId), originalName, contentType, content.length,
                    new ByteArrayInputStream(content));
            EnterpriseDocumentService.UploadResult result = documentService.registerStoredFile(
                    new EnterpriseDocumentService.StoredFileRequest(spaceId,
                            title == null || title.isBlank() ? originalName : title.trim(),
                            originalName, stored.objectKey(), stored.provider(), stored.contentType(),
                            stored.size(), checksum));
            if (result.duplicate()) {
                objectStorage.delete(stored.objectKey());
            }
            return result;
        } catch (IOException exception) {
            deleteQuietly(stored);
            throw new ServiceException("文件存储失败: " + exception.getMessage());
        } catch (RuntimeException exception) {
            deleteQuietly(stored);
            throw exception;
        }
    }

    @Override
    public EnterpriseDocumentService.UploadResult importUrl(Long spaceId, String title, String url) {
        requireEditor(spaceId);
        URI uri;
        try {
            uri = URI.create(url == null ? "" : url.trim());
            validateExternalUrl(uri);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "text/plain,text/html,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,*/*")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
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
            return upload(spaceId, title, originalName, contentType, content);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("URL 格式不正确");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ServiceException("网页内容获取被中断");
        } catch (IOException exception) {
            throw new ServiceException("网页内容获取失败: " + exception.getMessage());
        }
    }

    private void requireEditor(Long spaceId) {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            throw new ServiceException("当前租户上下文不存在");
        }
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR);
    }

    private String namespace(Long spaceId) {
        return "knowledge/" + UserContextHolder.getCurrentTenantId() + "/" + spaceId;
    }

    private void deleteQuietly(ObjectStorage.StoredObject stored) {
        if (stored == null) return;
        try {
            objectStorage.delete(stored.objectKey());
        } catch (IOException ignored) {
            // The registration error is more useful to the caller.
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

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
