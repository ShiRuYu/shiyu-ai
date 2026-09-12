package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * KnowledgeDocumentUploadService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeDocumentUploadService {

    /**
     * 执行 {@code upload} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param title 方法参数。
     * @param originalName 方法参数。
     * @param contentType 方法参数。
     * @param content 方法参数。
     *
     * @return 操作结果。
     */
    EnterpriseDocumentService.UploadResult upload(
            ActorContext actor,
            Long spaceId,
            String title,
            String originalName,
            String contentType,
            byte[] content);

    /**
     * 执行 {@code importUrl} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param title 方法参数。
     * @param url 方法参数。
     *
     * @return 操作结果。
     */
    EnterpriseDocumentService.UploadResult importUrl(
            ActorContext actor, Long spaceId, String title, String url);
}
