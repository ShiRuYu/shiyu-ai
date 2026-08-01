package com.shiyu.ai.knowledge.service;

public interface KnowledgeDocumentUploadService {

    EnterpriseDocumentService.UploadResult upload(Long spaceId, String title,
                                                   String originalName, String contentType,
                                                   byte[] content);

    EnterpriseDocumentService.UploadResult importUrl(Long spaceId, String title, String url);
}
