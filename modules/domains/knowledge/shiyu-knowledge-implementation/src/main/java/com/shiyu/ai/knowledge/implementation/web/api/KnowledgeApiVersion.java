package com.shiyu.ai.knowledge.implementation.web.api;

import com.shiyu.ai.common.core.exception.ServiceException;

/** 知识库接口版本通过请求头控制，不通过 URL 暴露版本号。 */
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
     * {@code requireCurrent} 执行当前类型定义的业务操作。
     *
     * @param version 参数值，用于执行当前操作。
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
