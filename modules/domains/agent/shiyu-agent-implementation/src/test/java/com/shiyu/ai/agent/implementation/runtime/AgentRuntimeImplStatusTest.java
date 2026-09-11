package com.shiyu.ai.agent.implementation.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;

import org.junit.jupiter.api.Test;

class AgentRuntimeImplStatusTest {

    @Test
    void mapsRuntimeStatusesToStablePersistenceCodes() {
        assertEquals(0, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.PENDING));
        assertEquals(0, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.RUNNING));
        assertEquals(1, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.COMPLETED));
        assertEquals(2, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.FAILED));
        assertEquals(3, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.PAUSED));
        assertEquals(4, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.CANCELLED));
    }

    @Test
    void restoresRuntimeStatusesFromPersistenceCodes() {
        assertEquals(ExecutionStatus.RUNNING, AgentRuntimeImpl.fromStoredStatus(0));
        assertEquals(ExecutionStatus.COMPLETED, AgentRuntimeImpl.fromStoredStatus(1));
        assertEquals(ExecutionStatus.FAILED, AgentRuntimeImpl.fromStoredStatus(2));
        assertEquals(ExecutionStatus.PAUSED, AgentRuntimeImpl.fromStoredStatus(3));
        assertEquals(ExecutionStatus.CANCELLED, AgentRuntimeImpl.fromStoredStatus(4));
        assertNull(AgentRuntimeImpl.fromStoredStatus(99));
        assertNull(AgentRuntimeImpl.fromStoredStatus(null));
    }
}
