package com.shiyu.ai.knowledge.implementation.application.service;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.StoredFileRequest;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.UploadResult;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentUploadService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * 提供 知识 文档 Upload 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentUploadServiceImpl implements KnowledgeDocumentUploadService {

    /**
     * MAX_IMPORT_BYTES 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long MAX_IMPORT_BYTES = 200L * 1024 * 1024;

    /**
     * objectStorage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ObjectStorage objectStorage;
    /**
     * securityScanner 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ContentSecurityScanner securityScanner;
    /**
     * documentService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EnterpriseDocumentService documentService;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

    /**
     * 执行 知识 文档 Upload 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param title 用于完成本次业务处理的 title 参数。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 知识 文档 Upload 相关操作生成的结果数据。
     */
    @Override
    public EnterpriseDocumentService.UploadResult upload(
            ActorContext actor,
            Long spaceId,
            String title,
            String originalName,
            String contentType,
            byte[] content) {
        requireEditor(actor, spaceId);
        securityScanner.validate(originalName, contentType, content);
        String checksum = sha256(content);
        ObjectStorage.StoredObject stored = null;
        try {
            stored =
                    objectStorage.put(
                            namespace(actor, spaceId),
                            originalName,
                            contentType,
                            content.length,
                            new ByteArrayInputStream(content));
            EnterpriseDocumentService.UploadResult result =
                    documentService.registerStoredFile(
                            actor,
                            new EnterpriseDocumentService.StoredFileRequest(
                                    spaceId,
                                    title == null || title.isBlank() ? originalName : title.trim(),
                                    originalName,
                                    stored.objectKey(),
                                    stored.provider(),
                                    stored.contentType(),
                                    stored.size(),
                                    checksum));
            if (result.duplicate()) {
                objectStorage.delete(stored.objectKey());
            }
            return result;
        } catch (IOException exception) {
            deleteQuietly(stored);
            throw new ServiceException("文件存储失败");
        } catch (RuntimeException exception) {
            deleteQuietly(stored);
            throw exception;
        }
    }

    /**
     * 执行 知识 文档 Upload 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param title 用于完成本次业务处理的 title 参数。
     * @param url 用于完成本次业务处理的 url 参数。
     * @return 返回 知识 文档 Upload 相关操作生成的结果数据。
     */
    @Override
    public EnterpriseDocumentService.UploadResult importUrl(
            ActorContext actor, Long spaceId, String title, String url) {
        requireEditor(actor, spaceId);
        URI uri;
        try {
            uri = URI.create(url == null ? "" : url.trim());
            validateExternalUrl(uri);
            HttpRequest request =
                    HttpRequest.newBuilder(uri)
                            .timeout(Duration.ofSeconds(60))
                            .header(
                                    "Accept",
                                    "text/plain,text/html,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,*/*")
                            .GET()
                            .build();
            HttpResponse<byte[]> response =
                    httpClient.send(request, ignored -> new LimitedBodySubscriber(MAX_IMPORT_BYTES));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException("网页内容获取失败，HTTP 状态码: " + response.statusCode());
            }
            byte[] content = response.body();
            if (content.length == 0 || content.length > MAX_IMPORT_BYTES) {
                throw new ServiceException("网页内容为空或超过 200 MB 限制");
            }
            String originalName = fileName(uri);
            String contentType =
                    response.headers()
                            .firstValue("Content-Type")
                            .map(value -> value.split(";", 2)[0].trim())
                            .orElse("text/html");
            return upload(actor, spaceId, title, originalName, contentType, content);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("URL 格式不正确");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ServiceException("网页内容获取被中断");
        } catch (IOException exception) {
            throw new ServiceException("网页内容获取失败");
        }
    }

    /** 在响应接收过程中限制累计字节，超限时取消下载而不是继续缓冲整个响应。 */
    static final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate =
                HttpResponse.BodySubscribers.ofByteArray();
        private final long limit;
        private long received;
        private Flow.Subscription subscription;
        private boolean done;

        LimitedBodySubscriber(long limit) {
            this.limit = limit;
        }

        /** {@inheritDoc} */
        @Override
        public CompletionStage<byte[]> getBody() {
            return delegate.getBody();
        }

        /** {@inheritDoc} */
        @Override
        public void onSubscribe(Flow.Subscription value) {
            subscription = value;
            delegate.onSubscribe(value);
        }

        /** {@inheritDoc} */
        @Override
        public void onNext(List<ByteBuffer> buffers) {
            if (done) return;
            for (ByteBuffer buffer : buffers) {
                if (buffer.remaining() > limit - received) {
                    done = true;
                    subscription.cancel();
                    delegate.onError(new IOException("网页内容超过下载大小限制"));
                    return;
                }
                received += buffer.remaining();
            }
            delegate.onNext(buffers);
        }

        /** {@inheritDoc} */
        @Override
        public void onError(Throwable error) {
            if (done) return;
            done = true;
            delegate.onError(error);
        }

        /** {@inheritDoc} */
        @Override
        public void onComplete() {
            if (done) return;
            done = true;
            delegate.onComplete();
        }
    }

    private void requireEditor(ActorContext actor, Long spaceId) {
        if (actor == null) {
            throw new ServiceException("当前租户上下文不存在");
        }
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR, actor);
    }

    private String namespace(ActorContext actor, Long spaceId) {
        return "knowledge/" + actor.tenantId().value() + "/" + spaceId;
    }

    private void deleteQuietly(ObjectStorage.StoredObject stored) {
        if (stored == null) return;
        try {
            objectStorage.delete(stored.objectKey());
        } catch (IOException ignored) {
        }
    }

    private void validateExternalUrl(URI uri) throws IOException {
        if (uri.getScheme() == null
                || (!"http".equalsIgnoreCase(uri.getScheme())
                        && !"https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new ServiceException("仅支持 http/https URL");
        }
        for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
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
