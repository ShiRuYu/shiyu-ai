package com.shiyu.ai.knowledge.implementation.web.upload;

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

/** Registers a completed storage upload as a knowledge document. */
@Component
@RequiredArgsConstructor
public class KnowledgeDocumentUploadHandler implements ResumableUploadHandler {

    private final EnterpriseDocumentService documentService;
    private final KnowledgeSpaceService spaceService;

    @Override
    public void authorize(UploadActor actor, Long spaceId) {
        spaceService.requireAccess(
                spaceId, KnowledgeSpaceService.SpaceRole.EDITOR, toKnowledgeActor(actor));
    }

    @Override
    public String namespace(TenantId tenantId, Long spaceId) {
        return "knowledge/" + tenantId.value() + "/" + spaceId;
    }

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
