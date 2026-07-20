package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.DefaultNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.dal.agent.bo.AgentExecutionBO;
import com.shiyu.ai.dal.agent.repository.AgentExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class AgentRuntimeImplTest {

    @Mock private AgentCacheManager cacheManager;
    @Mock private AgentLoader agentLoader;
    @Mock private AgentExecutionRepository executionRepository;
    @Mock private AgentCheckpointRepository checkpointRepository;
    @Mock private EventPublisher eventPublisher;

    private AgentRuntimeImpl runtime;
    private AgentDefinition definition;
    private AgentVersion agentVersion;

    @BeforeEach
    void setUp() {
        runtime = new AgentRuntimeImpl(cacheManager, agentLoader, executionRepository, checkpointRepository, eventPublisher);

        // 构建一个最小可执行的 Agent
        BaseNode node = createNode("test", NodeType.DEFAULT);
        Graph graph = new Graph();
        graph.setName("testGraph");
        graph.addNode("test", node);
        graph.setStartNode("test").setEndNode("test");

        agentVersion = AgentVersion.builder()
                .versionNumber("v1.0.0")
                .description("test")
                .graph(graph)
                .createdAt(System.currentTimeMillis())
                .build();

        definition = AgentDefinition.builder()
                .agentId("test-agent")
                .name("Test Agent")
                .description("test")
                .currentVersion("v1.0.0")
                .createdAt(System.currentTimeMillis())
                .build();
        definition.addVersion(agentVersion);
    }

    private BaseNode createNode(String id, NodeType type) {
        NodeConfig config = NodeConfig.builder()
                .nodeId(id).nodeName(id).nodeType(type)
                .build();
        return DefaultNode.builder().config(config).build();
    }

    @Test
    void testExecuteSuccess() {
        when(cacheManager.get(null, "test-agent")).thenReturn(definition);
        when(executionRepository.selectByExecutionId(anyString())).thenReturn(null);

        Map<String, Object> input = new HashMap<>();
        input.put("msg", "hello");

        Execution result = runtime.execute("test-agent", input);

        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getOutput());
        assertNotNull(result.getDurationMs());
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void testExecuteWithVersion() {
        when(cacheManager.get(null, "test-agent")).thenReturn(definition);
        when(executionRepository.selectByExecutionId(anyString())).thenReturn(null);

        Map<String, Object> input = new HashMap<>();
        Execution result = runtime.execute("test-agent", "v1.0.0", input);

        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
    }

    @Test
    void testExecuteAgentNotFound() {
        when(cacheManager.get(null, "nonexistent")).thenReturn(null);
        when(cacheManager.getOrLoad(null, "nonexistent", agentLoader)).thenReturn(null);

        Map<String, Object> input = new HashMap<>();
        assertThrows(IllegalStateException.class, () -> runtime.execute("nonexistent", input));
    }

    @Test
    void testGetExecutionStatus() {
        when(executionRepository.selectByExecutionId("exec-1")).thenReturn(null);

        ExecutionStatus status = runtime.getStatus("exec-1");
        assertNull(status);
    }

    @Test
    void testGetExecutionNotFound() {
        assertNull(runtime.getExecution("nonexistent"));
    }

    @Test
    void testGetHistory() {
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setAgentId("test-agent");
        bo.setExecutionId("exec-1");
        bo.setInputData("{}");
        bo.setStatus("COMPLETED");
        when(executionRepository.selectByAgentId("test-agent", 20)).thenReturn(List.of(bo));

        List<Execution> history = runtime.getHistory("test-agent", 20);
        assertEquals(1, history.size());
    }

    @Test
    void testGetUserHistory() {
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setAgentId("test-agent");
        bo.setExecutionId("exec-1");
        bo.setInputData("{}");
        bo.setStatus("COMPLETED");
        when(executionRepository.selectBySessionId("100")).thenReturn(List.of(bo));

        List<Execution> history = runtime.getUserHistory(100L, 10);
        assertEquals(1, history.size());
    }

    @Test
    void testPauseWithoutActiveExecution() {
        assertThrows(IllegalStateException.class, () -> runtime.pause("nonexistent"));
    }

    @Test
    void testCancelWithoutActiveExecution() {
        when(executionRepository.selectByExecutionId("exec-1")).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> runtime.cancel("exec-1"));
    }

    @Test
    void testResumeWithoutHistory() {
        when(executionRepository.selectByExecutionId("exec-1")).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> runtime.resume("exec-1"));
    }

    @Test
    void testExecuteStream() {
        when(cacheManager.get(null, "test-agent")).thenReturn(definition);
        when(executionRepository.selectByExecutionId(anyString())).thenReturn(null);

        Map<String, Object> input = new HashMap<>();
        var flux = runtime.executeStream("test-agent", input);

        assertNotNull(flux);
        List<Map<String, Object>> results = flux.collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
