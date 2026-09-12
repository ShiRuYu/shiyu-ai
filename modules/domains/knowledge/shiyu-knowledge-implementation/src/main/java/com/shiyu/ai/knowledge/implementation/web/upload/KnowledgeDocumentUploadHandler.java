package com.shiyu.ai.knowledge.implementation.web.upload;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.StoredFileRequest;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.UploadResult;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 处理知识文档的分片上传、合并和安全校验。
 */
@Component
@RequiredArgsConstructor
public class KnowledgeDocumentUploadHandler implements ResumableUploadHandler {

    /**
     * documentService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EnterpriseDocumentService documentService;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;

    /**
     * {@code authorize} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     */
    @Override
    public void authorize(UploadActor actor, Long spaceId) {
        spaceService.requireAccess(
                spaceId, KnowledgeSpaceService.SpaceRole.EDITOR, toKnowledgeActor(actor));
    }

    /**
     * {@code namespace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String namespace(TenantId tenantId, Long spaceId) {
        return "knowledge/" + tenantId.value() + "/" + spaceId;
    }

    /**
     * {@code register} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public RegistrationResult register(UploadActor actor, UploadRegistration request) {
        authorize(actor, request.spaceId());
        if (!actor.tenantId().equals(request.tenantId())) {
            throw new IllegalArgumentException(
                    "upload registration tenant does not match actor tenant");
        }
        EnterpriseDocumentService.UploadResult result =
                documentService.registerStoredFile(
                        toKnowledgeActor(actor),
                        new EnterpriseDocumentService.StoredFileRequest(
                                request.spaceId(),
                                request.title(),
                                request.originalName(),
                                request.objectKey(),
                                request.storageProvider(),
                                request.contentType(),
                                request.size(),
                                request.checksum()));
        return new RegistrationResult(result, result.duplicate());
    }

    private ActorContext toKnowledgeActor(UploadActor actor) {
        RoleId roleId = actor.roleId();
        return new ActorContext(actor.tenantId(), actor.userId(), roleId, actor.platformAdmin());
    }
}
