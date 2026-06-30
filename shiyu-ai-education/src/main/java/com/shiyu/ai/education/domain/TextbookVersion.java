package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

    private final String code;
    private final String name;
}
