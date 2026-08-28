package com.shiyu.ai.agent.checkpoint;

import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DbCheckpointStoreCoverageTest {
    @Test
    void savesLoadsListsAndDeletesTenantScopedCheckpoints() {
        var repository = mock(AgentCheckpointRepository.class);
        var store = new DbCheckpointStore(repository);
        var tenant = new TenantId(3L);
        var checkpoint = new Checkpoint(tenant, "exec", "node", Map.of("answer", 42));
        store.save(tenant, checkpoint);
        verify(repository).insert(eq(tenant), any(AgentCheckpointBO.class));

        var row = new AgentCheckpointBO();
        row.setTenantId(3L); row.setCheckpointId("cp"); row.setExecutionId("exec");
        row.setNodeId("node"); row.setStateData("{\"answer\":42}");
        when(repository.selectByCheckpointId(tenant, "cp")).thenReturn(row);
        when(repository.selectLatestByExecutionId(tenant, "exec")).thenReturn(row);
        when(repository.listByExecutionId(tenant, "exec")).thenReturn(List.of(row));
        assertNotNull(store.load(tenant, "cp"));
        assertNotNull(store.loadByExecutionId(tenant, "exec"));
        assertEquals(1, store.listByExecutionId(tenant, "exec").size());
        when(repository.selectByCheckpointId(tenant, "missing")).thenReturn(null);
        when(repository.selectLatestByExecutionId(tenant, "missing")).thenReturn(null);
        assertNull(store.load(tenant, "missing"));
        assertNull(store.loadByExecutionId(tenant, "missing"));
        store.delete(tenant, "cp");
        store.deleteByExecutionId(tenant, "exec");
        verify(repository).deleteByCheckpointId(tenant, "cp");
        verify(repository).deleteByExecutionId(tenant, "exec");
    }

    @Test
    void rejectsNullOrForeignTenantOnSave() {
        var repository = mock(AgentCheckpointRepository.class);
        var store = new DbCheckpointStore(repository);
        var tenant = new TenantId(3L);
        var foreign = new Checkpoint(new TenantId(4L), "exec", "node", Map.of());
        assertThrows(IllegalArgumentException.class, () -> store.save(tenant, foreign));
        assertThrows(IllegalArgumentException.class, () -> store.save(tenant, null));
    }
}
