package com.shiyu.ai.common.storage;

public interface ContentSecurityScanner {

    void validate(String fileName, String contentType, byte[] content);
}
