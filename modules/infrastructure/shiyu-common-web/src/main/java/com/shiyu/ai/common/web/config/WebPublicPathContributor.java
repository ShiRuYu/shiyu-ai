package com.shiyu.ai.common.web.config;

import java.util.Collection;

/**
 * WebPublicPathContributor 接口，定义基础设施模块的能力边界。
 */
public interface WebPublicPathContributor {

    /**
     * 执行 {@code publicPathPatterns} 定义的接口操作。
     *
     * @return 符合条件的结果集合。
     */
    Collection<String> publicPathPatterns();
}
