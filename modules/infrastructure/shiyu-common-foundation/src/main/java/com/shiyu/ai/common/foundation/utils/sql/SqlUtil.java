package com.shiyu.ai.common.foundation.utils.sql;

import com.shiyu.ai.common.foundation.exception.UtilException;
import com.shiyu.ai.common.foundation.utils.StringUtils;

/**
 * 提供 Sql 相关的通用辅助操作，供业务和基础设施复用。
 */
public class SqlUtil {

    /** 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序） */
    public static final String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /** 检查字符，防止注入绕过 */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new UtilException("参数不符合规范，不能进行查询");
        }
        return value;
    }

    /** 验证 order by 语法是否符合规范（白名单校验） */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }
}
