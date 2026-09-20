package com.shiyu.ai.knowledge.implementation.web.upload;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.StoredFileRequest;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.UploadResult;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.port.ResumableUploadHandler;
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
 * 处理 知识 文档 Upload 相关事件或请求，并推进后续业务流程。
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
     * 执行 知识 文档 Upload 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     */
    @Override
    public void authorize(UploadActor actor, Long spaceId) {
        spaceService.requireAccess(
                spaceId, KnowledgeSpaceService.SpaceRole.EDITOR, toKnowledgeActor(actor));
    }

    /**
     * 执行 知识 文档 Upload 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 文档 Upload 相关操作生成的结果数据。
     */
    @Override
    public String namespace(TenantId tenantId, Long spaceId) {
        return "knowledge/" + tenantId.value() + "/" + spaceId;
    }

    /**
     * 创建或保存 知识 文档 Upload 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 文档 Upload 相关操作生成的结果数据。
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
