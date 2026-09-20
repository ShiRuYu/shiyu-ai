package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 提供 知识 文档 Upload 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeDocumentUploadService {

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
    EnterpriseDocumentService.UploadResult upload(
            ActorContext actor,
            Long spaceId,
            String title,
            String originalName,
            String contentType,
            byte[] content);

    /**
     * 执行 知识 文档 Upload 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param title 用于完成本次业务处理的 title 参数。
     * @param url 用于完成本次业务处理的 url 参数。
     * @return 返回 知识 文档 Upload 相关操作生成的结果数据。
     */
    EnterpriseDocumentService.UploadResult importUrl(
            ActorContext actor, Long spaceId, String title, String url);
}
