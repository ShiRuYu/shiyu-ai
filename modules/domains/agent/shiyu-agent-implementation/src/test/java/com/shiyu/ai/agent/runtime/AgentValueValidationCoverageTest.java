package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.evaluation.*;
import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Covers the fail-closed value-object validation used at domain boundaries. */
class AgentValueValidationCoverageTest {
    @Test
    void validatesAppAndVersionIdentityAndAppliesDefaults() {
        assertThrows(IllegalArgumentException.class, () -> app(null, 1, 2, "name"));
        assertThrows(IllegalArgumentException.class, () -> app(" ", 1, 2, "name"));
        assertThrows(IllegalArgumentException.class, () -> app("a", 0, 2, "name"));
        assertThrows(IllegalArgumentException.class, () -> app("a", 1, 0, "name"));
        assertThrows(IllegalArgumentException.class, () -> app("a", 1, 2, null));
        assertThrows(IllegalArgumentException.class, () -> app("a", 1, 2, " "));
        AiApp defaults = new AiApp("a", new TenantId(1), new UserId(2), "name", null, " ", null, null, null);
        assertEquals("ACTIVE", defaults.status());
        assertEquals(defaults.createdAt(), defaults.updatedAt());

        assertThrows(IllegalArgumentException.class, () -> version(null, "app", 1, "1"));
        assertThrows(IllegalArgumentException.class, () -> version(" ", "app", 1, "1"));
        assertThrows(IllegalArgumentException.class, () -> version("v", null, 1, "1"));
        assertThrows(IllegalArgumentException.class, () -> version("v", " ", 1, "1"));
        assertThrows(IllegalArgumentException.class, () -> version("v", "app", 0, "1"));
        assertThrows(IllegalArgumentException.class, () -> version("v", "app", 1, null));
        assertThrows(IllegalArgumentException.class, () -> version("v", "app", 1, " "));
        AiAppVersion draft = new AiAppVersion("v", "app", new TenantId(1), "1", null, " ", null, null);
        assertEquals("{}", draft.configJson());
        assertEquals("DRAFT", draft.status());
        assertFalse(draft.published());
        assertTrue(new AiAppVersion("p", "app", new TenantId(1), "1", "{}", "PUBLISHED", Instant.now(), null).published());
    }

    @Test
    void validatesApprovalAndEvaluationValueObjects() {
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class, () -> approval(null, "run", 1, 2, "tool"));
        assertThrows(IllegalArgumentException.class, () -> approval(" ", "run", 1, 2, "tool"));
        assertThrows(IllegalArgumentException.class, () -> approval("a", null, 1, 2, "tool"));
        assertThrows(IllegalArgumentException.class, () -> approval("a", " ", 1, 2, "tool"));
        assertThrows(IllegalArgumentException.class, () -> approval("a", "run", 0, 2, "tool"));
        assertThrows(IllegalArgumentException.class, () -> approval("a", "run", 1, 0, "tool"));
        assertThrows(IllegalArgumentException.class, () -> approval("a", "run", 1, 2, null));
        assertThrows(IllegalArgumentException.class, () -> approval("a", "run", 1, 2, " "));
        ToolApproval normalized = new ToolApproval("a", "run", 1, 2, "tool", null, null, null, null, null);
        assertEquals("{}", normalized.argumentsRedacted());
        assertEquals(ToolApprovalStatus.PENDING, normalized.status());
        assertThrows(IllegalArgumentException.class, () -> new ToolApproval("a", "run", 1, 2, "tool", "{}", ToolApprovalStatus.PENDING, now, null, now.minusSeconds(1)));

        assertThrows(IllegalArgumentException.class, () -> new EvalDataset(null, 1, 2, "name", null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalDataset(" ", 1, 2, "name", null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalDataset("d", 0, 2, "name", null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalDataset("d", 1, 0, "name", null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalDataset("d", 1, 2, null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalDataset("d", 1, 2, " ", null, now));
        assertNotNull(new EvalDataset("d", 1, 2, "name", null, null).createdAt());

        assertThrows(IllegalArgumentException.class, () -> new EvalCase(null, "d", 1, "input", null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase(" ", "d", 1, "input", null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase("c", null, 1, "input", null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase("c", " ", 1, "input", null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase("c", "d", 0, "input", null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase("c", "d", 1, null, null, null, now));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase("c", "d", 1, " ", null, null, now));
        assertEquals("", new EvalCase("c", "d", 1, "input", null, null, now).expected());

        assertThrows(IllegalArgumentException.class, () -> new EvalRun(null, "d", 1, 2, null, null, null, 0, null, now, null));
        assertThrows(IllegalArgumentException.class, () -> new EvalRun(" ", "d", 1, 2, null, null, null, 0, null, now, null));
        assertThrows(IllegalArgumentException.class, () -> new EvalRun("r", null, 1, 2, null, null, null, 0, null, now, null));
        assertThrows(IllegalArgumentException.class, () -> new EvalRun("r", " ", 1, 2, null, null, null, 0, null, now, null));
        assertThrows(IllegalArgumentException.class, () -> new EvalRun("r", "d", 0, 2, null, null, null, 0, null, now, null));
        assertThrows(IllegalArgumentException.class, () -> new EvalRun("r", "d", 1, 0, null, null, null, 0, null, now, null));
        EvalRun run = new EvalRun("r", "d", 1, 2, null, null, null, 0, null, null, null);
        assertEquals(EvalMetric.EXACT_MATCH, run.metric());
        assertEquals("CREATED", run.status());
        assertNotNull(run.results());
        assertNotNull(run.createdAt());
    }

    private static AiApp app(String id, long tenant, long owner, String name) {
        return new AiApp(id, new TenantId(tenant), new UserId(owner), name, null, "ACTIVE", null, Instant.now(), Instant.now());
    }

    private static AiAppVersion version(String id, String appId, long tenant, String number) {
        return new AiAppVersion(id, appId, new TenantId(tenant), number, "{}", "DRAFT", Instant.now(), null);
    }

    private static ToolApproval approval(String id, String run, long tenant, long owner, String tool) {
        return new ToolApproval(id, run, tenant, owner, tool, "{}", ToolApprovalStatus.PENDING, Instant.now(), null);
    }
}
