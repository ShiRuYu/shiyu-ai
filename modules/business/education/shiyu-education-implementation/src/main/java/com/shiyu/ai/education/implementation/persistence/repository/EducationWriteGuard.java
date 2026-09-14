package com.shiyu.ai.education.implementation.persistence.repository;

/**
 * 校验教育模块写操作的租户范围和数据权限。
 */
final class EducationWriteGuard {
    private EducationWriteGuard() {}

    static int require(int rows, String operation) {
        if (rows < 1) {
            throw new IllegalStateException(operation + " affected no rows");
        }
        return rows;
    }
}
