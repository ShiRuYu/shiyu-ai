package com.shiyu.ai.education.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code TextbookVersion} 表示教育模块中的一组受控业务状态或分类。
 */
@Getter
@AllArgsConstructor
public enum TextbookVersion {
    PEP("PEP", "人教版"),
    BNUP("BNUP", "北师大版"),
    JSEP("JSEP", "苏教版"),
    SHEP("SHEP", "沪教版"),
    JKP("JKP", "教科版"),
    XJP("XJP", "湘教版"),
    FLTRP("FLTRP", "外研版");

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
}
