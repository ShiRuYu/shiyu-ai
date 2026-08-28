package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.runtime.AiRun;
import com.shiyu.ai.runtime.AiRunSource;
import com.shiyu.ai.runtime.AiRunStatus;
import com.shiyu.ai.runtime.AiRuntimeService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentRuntimeImplRuntimeAuditTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(9L), new UserId(7L), false);

    @Test
    void admitsAndFinishesDurableRuntimeRunForAppExecution() {
        AgentExecutionRepository executions = mock(AgentExecutionRepository.class);
        when(executions.selectByExecutionId(any(TenantId.class), any(String.class))).thenReturn(null);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AiRun run = run();
        when(runtime.startRun(any(), any(AiRunSource.class), any(String.class), any(), any())).thenReturn(run);
        AgentRuntimeImpl service = newRuntime(executions, runtime);

        assertEquals(com.shiyu.ai.agent.execution.ExecutionStatus.COMPLETED,
                service.execute(ACTOR, "agent-1", Map.of("__appId", "app-1", "message", "hello")).getStatus());
        verify(runtime).append(run, com.shiyu.ai.runtime.AiRunEventType.MODEL_STARTED, "{}", true);
        verify(runtime).finish("run-1", new TenantId(9L), 7L, AiRunStatus.COMPLETED, null);
    }

    @Test
    void failsClosedWhenAppRuntimeAdmissionFails() {
        AgentExecutionRepository executions = mock(AgentExecutionRepository.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        doThrow(new IllegalStateException("runtime unavailable"))
                .when(runtime).startRun(any(), any(AiRunSource.class), any(String.class), any(), any());
        AgentRuntimeImpl service = newRuntime(executions, runtime);

        assertThrows(IllegalStateException.class,
                () -> service.execute(ACTOR, "agent-1", Map.of("__appId", "app-1")));
    }

    @Test
    void keepsLegacyAgentExecutionTraceOptionalButDoesNotHideFinishFailure() {
        AgentExecutionRepository executions = mock(AgentExecutionRepository.class);
        when(executions.selectByExecutionId(any(TenantId.class), any(String.class))).thenReturn(null);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        doThrow(new IllegalStateException("runtime unavailable"))
                .when(runtime).startRun(any(), any(AiRunSource.class), any(String.class), any(), any());
        AgentRuntimeImpl service = newRuntime(executions, runtime);

        assertEquals(com.shiyu.ai.agent.execution.ExecutionStatus.COMPLETED,
                service.execute(ACTOR, "agent-1", Map.of("message", "legacy" )).getStatus());

        AiRun run = run();
        when(runtime.startRun(any(), any(AiRunSource.class), any(String.class), any(), any())).thenReturn(run);
        doThrow(new IllegalStateException("finish unavailable"))
                .when(runtime).finish(anyString(), any(TenantId.class), anyLong(), any(AiRunStatus.class), nullable(String.class));
        assertEquals(com.shiyu.ai.agent.execution.ExecutionStatus.COMPLETED,
                service.execute(ACTOR, "agent-1", Map.of("__appId", "app-2")).getStatus());
    }

    private AgentRuntimeImpl newRuntime(AgentExecutionRepository executions, AiRuntimeService runtime) {
        AgentCacheManager cache = new AgentCacheManager(mock(AgentAdminRepository.class), mock(AgentLoader.class));
        BaseNode node = new BaseNode(NodeConfig.builder().nodeId("node").nodeName("node")
                .nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override protected NodeOutput doExecute(NodeInput input) {
                NodeOutput output = new NodeOutput(); output.setSuccess(true); output.addData("ok", true); return output;
            }
        };
        Graph graph = Graph.builder().name("audit").startNode("node").endNode("node")
                .nodes(Map.of("node", node)).edges(Map.of()).conditionalEdges(Map.of()).build();
        AgentVersion version = AgentVersion.builder().versionNumber("v1").graph(graph).build();
        AgentDefinition definition = AgentDefinition.builder().agentId("agent-1").currentVersion("v1").build();
        definition.addVersion(version);
        cache.putSystem(definition);
        return new AgentRuntimeImpl(cache, mock(AgentLoader.class), executions,
                mock(AgentCheckpointRepository.class), mock(EventPublisher.class), runtime);
    }

    private AiRun run() {
        return new AiRun("run-1", new TenantId(9L), new UserId(7L), "app-1", null, AiRunSource.AGENT, "agent-1",
                null, null, null, null, "execution", "model", "prompt", AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 1L);
    }
}
