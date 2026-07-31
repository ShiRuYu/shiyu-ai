package com.shiyu.ai.knowledge.storage;

public interface ContentSecurityScanner {

    void validate(String fileName, String contentType, byte[] content);
}
