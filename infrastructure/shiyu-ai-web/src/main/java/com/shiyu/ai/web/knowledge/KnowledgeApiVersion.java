package com.shiyu.ai.web.knowledge;

import com.shiyu.ai.common.core.exception.ServiceException;

/**
 * 知识库接口版本通过请求头控制，不通过 URL 暴露版本号。
 */
final class KnowledgeApiVersion {

    static final String HEADER = "version";
    static final String CURRENT = "1";

    private KnowledgeApiVersion() {
    }

    static void requireCurrent(String version) {
        if (version == null || version.isBlank()) {
            return;
        }
        if (!CURRENT.equals(version) && !("v" + CURRENT).equalsIgnoreCase(version)) {
            throw new ServiceException("不支持的知识库接口版本: " + version);
        }
    }
}
