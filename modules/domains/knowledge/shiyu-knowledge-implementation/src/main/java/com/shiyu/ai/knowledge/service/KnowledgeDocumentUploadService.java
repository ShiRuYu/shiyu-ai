package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.kernel.context.ActorContext;

public interface KnowledgeDocumentUploadService {

    EnterpriseDocumentService.UploadResult upload(ActorContext actor, Long spaceId, String title,
                                                   String originalName, String contentType,
                                                   byte[] content);

    EnterpriseDocumentService.UploadResult importUrl(ActorContext actor, Long spaceId,
                                                      String title, String url);
}
