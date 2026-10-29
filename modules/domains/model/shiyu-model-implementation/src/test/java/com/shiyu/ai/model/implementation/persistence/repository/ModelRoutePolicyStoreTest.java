package com.shiyu.ai.model.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelRoutePolicy;
import com.shiyu.ai.model.implementation.infrastructure.gateway.service.ModelRouter;
import com.shiyu.ai.kernel.context.TenantId;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import java.util.List;

/** 验证路由策略在仓储重建后保留，并按租户隔离。 */
class ModelRoutePolicyStoreTest {
    @Test
    void updatesStoredPolicyWithoutDuplicatingItAndRejectsMissingTenantOrRoute() {
        JdbcDataSource database = new JdbcDataSource();
        database.setURL("jdbc:h2:mem:route_updates;DB_CLOSE_DELAY=-1");
        ModelRoutePolicyStore store = new ModelRoutePolicyStore(database);
        store.run(null);
        ModelRouter router = new ModelRouter(store);
        TenantId tenant = new TenantId(7);
        router.savePolicy(new ModelRoutePolicy("update", 7, "初始策略",
                List.of("default:configured"), 30000, true, 16000));
        ModelRoutePolicy updated = new ModelRoutePolicy("update", 7, "更新策略",
                List.of("default:configured"), 10000, false, 8000);
        router.savePolicy(updated);

        assertEquals(List.of(updated), router.policies(tenant));
        assertEquals(updated, router.requirePolicy("update", tenant));
        assertTrue(router.policies(new TenantId(8)).isEmpty());
        assertTrue(router.policies(null).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> router.requirePolicy("update", null));
        assertThrows(IllegalArgumentException.class, () -> router.requirePolicy("missing", tenant));
    }

    @Test
    void propagatesDatabaseWriteFailureInsteadOfKeepingAnInMemoryPolicy() {
        JdbcDataSource database = new JdbcDataSource();
        database.setURL("jdbc:h2:mem:route_missing_table;DB_CLOSE_DELAY=-1");
        ModelRouter router = new ModelRouter(new ModelRoutePolicyStore(database));
        ModelRoutePolicy policy = new ModelRoutePolicy("missing-table", 7, "策略",
                List.of("default:configured"), 30000, true, 16000);
        assertThrows(org.springframework.dao.DataAccessException.class, () -> router.savePolicy(policy));
    }

    @Test
    void survivesRestartAndIsolatesTenants() throws Exception {
        JdbcDataSource database = new JdbcDataSource();
        database.setURL("jdbc:h2:mem:routes;DB_CLOSE_DELAY=-1");
        ModelRoutePolicyStore first = new ModelRoutePolicyStore(database);
        first.run(null);
        ModelRoutePolicy policy = new ModelRoutePolicy("route", 7, "聊天", List.of("default:configured"), 30000, true, 16000);
        new com.shiyu.ai.model.implementation.infrastructure.gateway.service.ModelRouter(first).savePolicy(policy);
        ModelRoutePolicyStore restarted = new ModelRoutePolicyStore(database);
        restarted.run(null);
        assertEquals(List.of(policy), restarted.list(7));
        var router = new com.shiyu.ai.model.implementation.infrastructure.gateway.service.ModelRouter(restarted);
        assertEquals(policy, router.requirePolicy("route", new com.shiyu.ai.kernel.context.TenantId(7)));
        assertThrows(IllegalArgumentException.class, () -> router.requirePolicy("route", new com.shiyu.ai.kernel.context.TenantId(8)));
        assertTrue(restarted.list(8).isEmpty());
    }
}
