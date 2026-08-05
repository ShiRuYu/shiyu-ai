package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.port.repository.AgentExecutionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentRuntimeImplControlTest {

    private Disposable disposable;

    @AfterEach
    void cleanup() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Test
    void pauseThenResumeLetsStreamCompleteAndPersistsSuccess() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

        disposable = runtime.executeStream("agent-1", Map.of("message", "hello")).subscribe(
                events::add, error -> terminal.countDown(), terminal::countDown);

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();

        runtime.pause(executionId);
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(executionId));

        runtime.resume(executionId);
        assertEquals(ExecutionStatus.RUNNING, runtime.getStatus(executionId));

        node.release.countDown();
        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should complete after resume");
        assertTrue(events.stream().anyMatch(event -> "COMPLETED".equals(event.get("status"))),
                "stream should emit a COMPLETED terminal event");
        verify(repository, timeout(5000).atLeastOnce()).update(argThat(bo ->
                executionId.equals(bo.getExecutionId()) && Integer.valueOf(1).equals(bo.getStatus())));
    }

    @Test
    void cancelDuringExecutionEmitsCancelledTerminalAndPersistsCancelled() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

        disposable = runtime.executeStream("agent-1", Map.of("message", "hello")).subscribe(
                events::add, error -> terminal.countDown(), terminal::countDown);

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();

        runtime.cancel(executionId);
        assertEquals(ExecutionStatus.CANCELLED, runtime.getStatus(executionId));

        node.release.countDown();
        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should terminate after cancel");
        assertTrue(events.stream().anyMatch(event -> "CANCELLED".equals(event.get("status"))),
                "stream should emit a CANCELLED terminal event");
        verify(repository, timeout(5000).atLeastOnce()).update(argThat(bo ->
                executionId.equals(bo.getExecutionId()) && Integer.valueOf(4).equals(bo.getStatus())));
    }

    private static AgentRuntimeImpl newRuntime(AgentExecutionRepository repository, BlockingNode node) {
        AgentAdminRepository adminRepository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cacheManager = new AgentCacheManager(adminRepository, loader);

        Graph graph = Graph.builder()
                .name("control-test")
                .startNode("block")
                .endNode("block")
                .nodes(Map.of("block", node))
                .edges(Map.of())
                .conditionalEdges(Map.of())
                .build();
        AgentVersion version = AgentVersion.builder()
                .versionNumber("v1")
                .description("runtime control test")
                .graph(graph)
                .build();
        AgentDefinition definition = AgentDefinition.builder()
                .agentId("agent-1")
                .name("control-test")
                .currentVersion("v1")
                .build();
        definition.addVersion(version);
        cacheManager.put(definition);

        AgentCheckpointRepository checkpointRepository = mock(AgentCheckpointRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        return new AgentRuntimeImpl(cacheManager, loader, repository, checkpointRepository, publisher);
    }

    private static final class ExecutionRecorder {

        private final List<AgentExecutionBO> stored = new CopyOnWriteArrayList<>();
        private final CountDownLatch inserted = new CountDownLatch(1);

        private ExecutionRecorder(AgentExecutionRepository repository) {
            doAnswer(invocation -> {
                stored.add(invocation.getArgument(0));
                inserted.countDown();
                return null;
            }).when(repository).insert(any(AgentExecutionBO.class));
            when(repository.selectByExecutionId(anyString())).thenAnswer(invocation -> {
                String executionId = invocation.getArgument(0);
                return stored.stream()
                        .filter(bo -> executionId.equals(bo.getExecutionId()))
                        .findFirst()
                        .orElse(null);
            });
        }

        private String awaitExecutionId() throws Exception {
            assertTrue(inserted.await(5, TimeUnit.SECONDS), "execution should be persisted on start");
            return stored.getFirst().getExecutionId();
        }
    }

    private static final class BlockingNode extends BaseNode {

        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        private BlockingNode() {
            super(NodeConfig.builder()
                    .nodeId("block")
                    .nodeName("block")
                    .nodeType(NodeType.DEFAULT)
                    .timeout(0L)
                    .build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) throws Exception {
            entered.countDown();
            release.await();
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("blocked node released");
            return output;
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() {
            return List.of();
        }
    }
}
