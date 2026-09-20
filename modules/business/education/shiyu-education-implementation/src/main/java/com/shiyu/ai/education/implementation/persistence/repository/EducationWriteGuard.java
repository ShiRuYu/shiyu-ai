package com.shiyu.ai.education.implementation.persistence.repository;

/**
 * 实现 教育 Write Guard 相关的业务处理、协作逻辑或基础设施能力。
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
