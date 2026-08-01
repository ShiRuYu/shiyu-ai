package com.shiyu.ai.knowledge.upload;

import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.storage.ResumableUploadHandler;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Registers a completed storage upload as a knowledge document. */
@Component
@RequiredArgsConstructor
public class KnowledgeDocumentUploadHandler implements ResumableUploadHandler {

    private final EnterpriseDocumentService documentService;
    private final KnowledgeSpaceService spaceService;

    @Override
    public void authorize(Long tenantId, Long spaceId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !currentTenantId.equals(tenantId)) {
            throw new ServiceException("当前租户上下文不存在或已失效");
        }
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR);
    }

    @Override
    public String namespace(Long tenantId, Long spaceId) {
        return "knowledge/" + tenantId + "/" + spaceId;
    }

    @Override
    public RegistrationResult register(UploadRegistration request) {
        authorize(request.tenantId(), request.spaceId());
        EnterpriseDocumentService.UploadResult result = documentService.registerStoredFile(
                new EnterpriseDocumentService.StoredFileRequest(request.spaceId(), request.title(),
                        request.originalName(), request.objectKey(), request.storageProvider(),
                        request.contentType(), request.size(), request.checksum()));
        return new RegistrationResult(result, result.duplicate());
    }
}
