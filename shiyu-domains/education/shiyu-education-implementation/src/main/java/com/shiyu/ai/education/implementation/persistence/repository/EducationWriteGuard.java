package com.shiyu.ai.education.implementation.persistence.repository;

/** Ensures a tenant-scoped command did not silently affect zero rows. */
final class EducationWriteGuard {
    private EducationWriteGuard() {
    }

    static int require(int rows, String operation) {
        if (rows < 1) {
            throw new IllegalStateException(operation + " affected no rows");
        }
        return rows;
    }
}
