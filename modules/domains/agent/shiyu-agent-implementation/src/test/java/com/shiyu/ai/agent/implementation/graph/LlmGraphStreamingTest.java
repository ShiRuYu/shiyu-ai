package com.shiyu.ai.agent.implementation.graph;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.bsc.langgraph4j.streaming.StreamingOutput;
import reactor.core.publisher.Sinks;

class LlmGraphStreamingTest {
    @Test
    void modelFailureFailsGraphStream() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        Sinks.Many<ChatResponse> model = Sinks.many().unicast().onBackpressureBuffer();
        when(engine.stream(any())).thenReturn(model.asFlux());
        LlmCallNode node = LlmCallNode.builder().chatEngine(engine)
                .config(LlmCallConfig.builder().stream(true).build()).build();
        Graph graph = Graph.builder().startNode("llm").endNode("llm")
                .nodes(Map.of("llm", node)).build();
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        CompletableFuture<?> stream = CompletableFuture.runAsync(() -> {
            try {
                graph.executeStream(NodeActorContext.bind(actor, Map.of("query", "hello")))
                        .collectList().block(Duration.ofSeconds(5));
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        });
        verify(engine, timeout(2000)).stream(any());
        model.tryEmitError(new IllegalStateException("provider failed"));
        assertThrows(java.util.concurrent.ExecutionException.class,
                () -> stream.get(2, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void emitsFirstChunkBeforeModelCompletes() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        Sinks.Many<ChatResponse> model = Sinks.many().unicast().onBackpressureBuffer();
        when(engine.stream(any())).thenReturn(model.asFlux());
        LlmCallNode node = LlmCallNode.builder().chatEngine(engine)
                .config(LlmCallConfig.builder().stream(true).build()).build();
        Graph graph = Graph.builder().startNode("llm").endNode("llm")
                .nodes(Map.of("llm", node)).build();
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        CompletableFuture<Boolean> partial = CompletableFuture.supplyAsync(() -> {
            try {
                return graph.executeStream(NodeActorContext.bind(actor, Map.of("query", "hello")))
                        .any(output -> output instanceof StreamingOutput<?> streaming
                                && streaming.chunk().contains("first"))
                        .block(Duration.ofSeconds(5));
            } catch (Exception error) {
                throw new RuntimeException(error);
            }
        });
        try {
            verify(engine, timeout(2000)).stream(any());
            model.tryEmitNext(ChatResponse.builder().eventType("DELTA").content("first").build());
            assertTrue(partial.get(2, java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            model.tryEmitComplete();
        }
    }
}
