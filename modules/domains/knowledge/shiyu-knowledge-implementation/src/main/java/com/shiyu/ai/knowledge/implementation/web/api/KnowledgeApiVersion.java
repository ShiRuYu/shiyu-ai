package com.shiyu.ai.knowledge.implementation.web.api;

import com.shiyu.ai.common.foundation.exception.ServiceException;

/**
 * 承载 知识 API Version 所属 Web 能力的请求适配和边界处理。
 */
public final class KnowledgeApiVersion {

    /**
     * HEADER 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String HEADER = "version";
    /**
     * CURRENT 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String CURRENT = "1";

    private KnowledgeApiVersion() {}

    /**
     * 获取并校验 知识 API Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param version 用于完成本次业务处理的 version 参数。
     */
    public static void requireCurrent(String version) {
        if (version == null || version.isBlank()) {
            return;
        }
        if (!CURRENT.equals(version) && !("v" + CURRENT).equalsIgnoreCase(version)) {
            throw new ServiceException("不支持的知识库接口版本: " + version);
        }
    }
}
