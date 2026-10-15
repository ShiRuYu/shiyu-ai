package com.shiyu.ai.agent.implementation.persistence.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunSource;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/** 使用实际的 H2 运行时表结构验证全局审批过期扫描。 */
class JdbcToolApprovalExpiryQueryTest {
    @Test
    void persistsManualNotExecutedResolutionInH2() {
        var database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("db/baseline/h2/schema/agent/16_ai_runtime.sql").build();
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            JdbcToolApprovalRepository approvals = new JdbcToolApprovalRepository(jdbc);
            TenantId tenant = new TenantId(7);
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            ToolApproval claimed = new ToolApproval(
                    "approval-1", "run-1", tenant.value(), 9, "CALCULATOR", "{}",
                    ToolApprovalStatus.CLAIMED, now, now, now.plusSeconds(300));
            TenantScope.withTenant(tenant, () -> {
                approvals.insert(claimed);
                ToolApproval resolved = new ToolApprovalService(approvals)
                        .markClaimedNotExecuted(claimed.id(), tenant, 9);
                assertEquals(ToolApprovalStatus.NOT_EXECUTED, resolved.status());
                assertEquals(ToolApprovalStatus.NOT_EXECUTED,
                        approvals.find(claimed.id(), tenant, 9).orElseThrow().status());
                return null;
            });
        } finally {
            database.shutdown();
        }
    }

    @Test
    void selectsOnlyOverdueApprovalsOnOwnedRunningRuns() {
        var database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("db/baseline/h2/schema/agent/16_ai_runtime.sql").build();
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            JdbcAiRuntimeRepository runs = new JdbcAiRuntimeRepository(jdbc);
            JdbcToolApprovalRepository approvals = new JdbcToolApprovalRepository(jdbc);
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            TenantId tenant = new TenantId(7);
            TenantScope.withTenant(tenant, () -> {
                runs.insert(run("active", tenant, 9, AiRunStatus.RUNNING, now));
                runs.insert(run("finished", tenant, 9, AiRunStatus.CANCELLED, now));
                approvals.insert(approval("due", "active", 7, 9, now.minusSeconds(1)));
                approvals.insert(approval("later", "active", 7, 9, now.plusSeconds(1)));
                approvals.insert(approval("ended", "finished", 7, 9, now.minusSeconds(1)));
                approvals.insert(approval("foreign", "active", 7, 10, now.minusSeconds(1)));
                return null;
            });
            assertEquals("due", approvals.findOverdueRunning(now, 100).getFirst().id());
            assertEquals(1, approvals.findOverdueRunning(now, 100).size());
        } finally {
            database.shutdown();
        }
    }

    private static AiRun run(String id, TenantId tenant, long user, AiRunStatus status,
            Instant now) {
        return new AiRun(id, tenant, new UserId(user), "app", "version",
                AiRunSource.CONVERSATION, "source", null, "trace", "conversation",
                "generation", "execution", "model", "hash", status,
                0, 0, false, "cost", now, null, null, 0, 0);
    }

    private static ToolApproval approval(String id, String run, long tenant, long user,
            Instant expires) {
        return new ToolApproval(id, run, tenant, user, "CALCULATOR", "{}",
                ToolApprovalStatus.PENDING, expires.minusSeconds(300), null, expires);
    }
}
