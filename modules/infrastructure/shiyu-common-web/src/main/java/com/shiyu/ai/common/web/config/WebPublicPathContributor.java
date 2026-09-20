package com.shiyu.ai.common.web.config;

import java.util.Collection;

/**
 * 向 Web Public Path 所属的应用或基础设施注册必要的扩展能力。
 */
public interface WebPublicPathContributor {

    /**
     * 执行 Web Public Path 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    Collection<String> publicPathPatterns();
}
