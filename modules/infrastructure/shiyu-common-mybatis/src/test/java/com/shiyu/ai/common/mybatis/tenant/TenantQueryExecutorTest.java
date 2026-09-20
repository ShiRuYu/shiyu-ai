package com.shiyu.ai.common.mybatis.tenant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mybatisflex.core.tenant.TenantManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 验证 租户 Query Executor 相关功能、边界条件、异常路径和协作行为。
 */
class TenantQueryExecutorTest {

    @AfterEach
    void restoreTenantFilter() {
        TenantManager.restoreTenantCondition();
    }

    @Test
    void restoresAnExistingIgnoreStateAfterNestedExecution() {
        assertFalse(TenantManager.isIgnoreTenantCondition());
        TenantQueryExecutor.readAcrossTenants(
                () -> {
                    assertTrue(TenantManager.isIgnoreTenantCondition());
                    TenantQueryExecutor.runAcrossTenants(
                            () -> assertTrue(TenantManager.isIgnoreTenantCondition()));
                    assertTrue(TenantManager.isIgnoreTenantCondition());
                    return null;
                });
        assertFalse(TenantManager.isIgnoreTenantCondition());

        TenantManager.ignoreTenantCondition();
        TenantQueryExecutor.runAcrossTenants(
                () -> assertTrue(TenantManager.isIgnoreTenantCondition()));
        assertTrue(TenantManager.isIgnoreTenantCondition());
    }

    @Test
    void restoresTheFilterAfterAnException() {
        assertThrows(
                IllegalStateException.class,
                () ->
                        TenantQueryExecutor.readAcrossTenants(
                                () -> {
                                    assertTrue(TenantManager.isIgnoreTenantCondition());
                                    throw new IllegalStateException("boom");
                                }));
        assertFalse(TenantManager.isIgnoreTenantCondition());
    }
}
