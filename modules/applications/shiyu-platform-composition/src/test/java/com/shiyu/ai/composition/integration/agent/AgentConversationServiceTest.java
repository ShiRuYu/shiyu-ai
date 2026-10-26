package com.shiyu.ai.composition.integration.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.conversation.implementation.application.ConversationService;
import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.conversation.implementation.domain.model.MessageRole;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/** 验证应用执行保留消息，并在早期提供运行标识。 */
class AgentConversationServiceTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(7), new UserId(9), false);

    @Test
    void approvedRunAppendsOneOwnedFinalReplyAndRetryIsIdempotent() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRun run = mock(AiRun.class);
        Execution execution = mock(Execution.class);
        Conversation conversation = mock(Conversation.class);
        ConversationMessage pending = mock(ConversationMessage.class);
        ConversationMessage completed = mock(ConversationMessage.class);
        when(run.id()).thenReturn("run-1");
        when(run.appId()).thenReturn("app-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(run.tenantId()).thenReturn(ACTOR.tenantId());
        when(run.ownerUserId()).thenReturn(ACTOR.userId());
        when(run.executionId()).thenReturn("exec-1");
        when(execution.getExecutionId()).thenReturn("exec-1");
        when(execution.getStatus()).thenReturn(ExecutionStatus.COMPLETED);
        when(execution.getOutput()).thenReturn(Map.of("content", "approved answer"));
        when(pending.id()).thenReturn("pending-1");
        when(pending.role()).thenReturn(MessageRole.ASSISTANT);
        when(pending.toolCall()).thenReturn(Map.of("status", "PAUSED",
                "runtimeRunId", "run-1", "executionId", "exec-1"));
        when(completed.role()).thenReturn(MessageRole.ASSISTANT);
        when(completed.toolCall()).thenReturn(Map.of("status", "COMPLETED",
                "runtimeRunId", "run-1", "executionId", "exec-1"));
        when(conversation.activeLeafMessageId()).thenReturn("pending-1");
        doReturn(List.of(pending)).doReturn(List.of(pending, completed))
                .when(repository).listMessages("conversation-1", ACTOR.tenantId(), 9L, 1000);
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(conversation));

        AgentConversationService service = new AgentConversationService(repository,
                conversations, mock(AiRuntimeService.class), mock(AgentRuntime.class));
        service.completeApprovedRun(ACTOR, run, execution);
        service.completeApprovedRun(ACTOR, run, execution);

        verify(conversations, times(1)).appendMessage(eq(conversation), eq("pending-1"),
                eq(MessageRole.ASSISTANT), eq("approved answer"), isNull(),
                argThat(trace -> "COMPLETED".equals(trace.get("status"))));
    }

    @Test
    void persistsAnOwnedAgentExchangeAndReturnsMessageAndRunIds() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        Conversation conversation = mock(Conversation.class);
        ConversationMessage input = mock(ConversationMessage.class);
        ConversationMessage assistant = mock(ConversationMessage.class);
        when(conversation.sceneType()).thenReturn("agent");
        when(conversation.activeLeafMessageId()).thenReturn("user-1");
        when(input.id()).thenReturn("user-1");
        when(assistant.id()).thenReturn("assistant-1");
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(conversation));
        when(conversations.appendUserMessage(conversation, "hello")).thenReturn(input);
        when(conversations.appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), anyString(), isNull(), anyMap()))
                .thenReturn(assistant);
        when(runtime.requirePublishedVersion("app-1", ACTOR.tenantId(), 9L))
                .thenReturn(new AiAppVersion("version-1", "app-1", ACTOR.tenantId(),
                        "1", "{\"agentId\":\"agent-1\",\"platform\":\"DEEPSEEK\","
                                + "\"model\":\"deepseek-flash\"}", "PUBLISHED",
                        Instant.now(), Instant.now()));
        when(agents.executeStream(eq(ACTOR), eq("agent-1"), isNull(), anyMap()))
                .thenReturn(Flux.just(
                        Map.of("status", "STARTED", "executionId", "exec-1",
                                "runtimeRunId", "run-1"),
                        Map.of("status", "DELTA", "executionId", "exec-1",
                                "content", "ok"),
                        Map.of("status", "COMPLETED", "executionId", "exec-1",
                                "output", Map.of("content", "ok",
                                        "__knowledgeAccessContext", "private"))));

        var events = new AgentConversationService(repository, conversations, runtime, agents)
                .execute(ACTOR, "conversation-1", "app-1", null, "hello", null)
                .collectList().block();

        assertNotNull(events);
        assertEquals(List.of("STARTED", "DELTA", "COMPLETED"),
                events.stream().map(event -> event.get("status")).toList());
        assertEquals("user-1", events.getFirst().get("inputMessageId"));
        assertEquals("run-1", events.getFirst().get("runtimeRunId"));
        assertEquals("assistant-1", events.getLast().get("assistantMessageId"));
        assertEquals("ok", events.get(1).get("content"));
        assertEquals(Map.of("content", "ok"), events.getLast().get("output"));
        verify(conversations).appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("ok"), isNull(), anyMap());
        verify(agents).executeStream(eq(ACTOR), eq("agent-1"), isNull(),
                argThat(graphInput -> "hello".equals(graphInput.get("query"))
                        && "DEEPSEEK".equals(graphInput.get("platform"))
                        && "deepseek-flash".equals(graphInput.get("model"))
                        && "STREAM".equals(graphInput.get("chatType"))));
    }

    @Test
    void persistsFirstReplyEvenWhenAnotherInputAdvancedTheConversation() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        Conversation initial = mock(Conversation.class);
        Conversation advanced = mock(Conversation.class);
        ConversationMessage input = mock(ConversationMessage.class);
        ConversationMessage assistant = mock(ConversationMessage.class);
        when(initial.sceneType()).thenReturn("agent");
        when(initial.activeLeafMessageId()).thenReturn("user-1");
        when(advanced.activeLeafMessageId()).thenReturn("user-2");
        when(input.id()).thenReturn("user-1");
        when(assistant.id()).thenReturn("assistant-1");
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(initial))
                .thenReturn(Optional.of(initial))
                .thenReturn(Optional.of(advanced));
        when(conversations.appendUserMessage(initial, "hello")).thenReturn(input);
        when(conversations.appendMessage(eq(initial), eq("user-1"),
                eq(MessageRole.ASSISTANT), anyString(), isNull(), anyMap()))
                .thenThrow(new IllegalStateException(
                        "conversation was modified; retry against the latest active leaf"));
        when(conversations.appendMessagePreservingLeaf(eq(advanced), eq("user-1"),
                eq(MessageRole.ASSISTANT), anyString(), isNull(), anyMap()))
                .thenReturn(assistant);
        when(runtime.requirePublishedVersion("app-1", ACTOR.tenantId(), 9L))
                .thenReturn(new AiAppVersion("version-1", "app-1", ACTOR.tenantId(),
                        "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED",
                        Instant.now(), Instant.now()));
        when(agents.executeStream(eq(ACTOR), eq("agent-1"), isNull(), anyMap()))
                .thenReturn(Flux.just(
                        Map.of("status", "STARTED", "executionId", "exec-1"),
                        Map.of("status", "COMPLETED", "output", "ok")));

        var events = new AgentConversationService(repository, conversations, runtime, agents)
                .execute(ACTOR, "conversation-1", "app-1", null, "hello", null)
                .collectList().block();

        assertNotNull(events);
        assertEquals("assistant-1", events.getLast().get("assistantMessageId"));
        verify(conversations).appendMessage(eq(initial), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("ok"), isNull(), anyMap());
        verify(conversations).appendMessagePreservingLeaf(eq(advanced), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("ok"), isNull(), anyMap());
    }

    @Test
    void approvalPausePersistsAnOwnedPendingReplyWithoutInventingFailure() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        Conversation conversation = mock(Conversation.class);
        ConversationMessage input = mock(ConversationMessage.class);
        ConversationMessage assistant = mock(ConversationMessage.class);
        when(conversation.sceneType()).thenReturn("agent");
        when(conversation.activeLeafMessageId()).thenReturn("user-1");
        when(input.id()).thenReturn("user-1");
        when(assistant.id()).thenReturn("assistant-pending");
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(conversation));
        when(conversations.appendUserMessage(conversation, "hello")).thenReturn(input);
        when(conversations.appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), anyString(), isNull(), anyMap()))
                .thenReturn(assistant);
        when(runtime.requirePublishedVersion("app-1", ACTOR.tenantId(), 9L))
                .thenReturn(new AiAppVersion("version-1", "app-1", ACTOR.tenantId(),
                        "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED",
                        Instant.now(), Instant.now()));
        when(agents.executeStream(eq(ACTOR), eq("agent-1"), isNull(), anyMap()))
                .thenReturn(Flux.just(
                        Map.of("status", "STARTED", "executionId", "exec-1",
                                "runtimeRunId", "run-1"),
                        Map.of("status", "PAUSED", "executionId", "exec-1",
                                "approvalId", "approval-1")));

        var events = new AgentConversationService(repository, conversations, runtime, agents)
                .execute(ACTOR, "conversation-1", "app-1", null, "hello", null)
                .collectList().block();

        assertEquals(List.of("STARTED", "PAUSED"),
                events.stream().map(event -> event.get("status")).toList());
        assertEquals("approval-1", events.getLast().get("approvalId"));
        assertEquals("assistant-pending", events.getLast().get("assistantMessageId"));
        verify(conversations).appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT),
                eq("工具调用等待审批，请前往工具审批页面处理。"), isNull(),
                argThat(trace -> "PAUSED".equals(trace.get("status"))));
    }

    @Test
    void recordsFailureWhenAgentStreamEndsWithoutATerminalEvent() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        Conversation conversation = mock(Conversation.class);
        ConversationMessage input = mock(ConversationMessage.class);
        ConversationMessage assistant = mock(ConversationMessage.class);
        when(conversation.sceneType()).thenReturn("agent");
        when(conversation.activeLeafMessageId()).thenReturn("user-1");
        when(input.id()).thenReturn("user-1");
        when(assistant.id()).thenReturn("assistant-1");
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(conversation));
        when(conversations.appendUserMessage(conversation, "hello")).thenReturn(input);
        when(conversations.appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("Agent 执行失败"), isNull(), anyMap()))
                .thenReturn(assistant);
        when(runtime.requirePublishedVersion("app-1", ACTOR.tenantId(), 9L))
                .thenReturn(new AiAppVersion("version-1", "app-1", ACTOR.tenantId(),
                        "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED",
                        Instant.now(), Instant.now()));
        when(agents.executeStream(eq(ACTOR), eq("agent-1"), isNull(), anyMap()))
                .thenReturn(Flux.just(Map.of("status", "STARTED", "executionId", "exec-1")));

        var events = new AgentConversationService(repository, conversations, runtime, agents)
                .execute(ACTOR, "conversation-1", "app-1", null, "hello", null)
                .collectList().block();

        assertNotNull(events);
        assertEquals(List.of("STARTED", "FAILED"),
                events.stream().map(event -> event.get("status")).toList());
        assertEquals("assistant-1", events.getLast().get("assistantMessageId"));
        verify(conversations).appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("Agent 执行失败"), isNull(),
                argThat(trace -> "FAILED".equals(trace.get("status"))));
    }

    @Test
    void preservesCompletedOutcomeWhenClientDisconnectsDuringFinalization() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        Conversation conversation = mock(Conversation.class);
        ConversationMessage input = mock(ConversationMessage.class);
        ConversationMessage assistant = mock(ConversationMessage.class);
        Execution completed = mock(Execution.class);
        when(conversation.sceneType()).thenReturn("agent");
        when(conversation.activeLeafMessageId()).thenReturn("user-1");
        when(input.id()).thenReturn("user-1");
        when(assistant.id()).thenReturn("assistant-1");
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(conversation));
        when(conversations.appendUserMessage(conversation, "hello")).thenReturn(input);
        when(conversations.appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("finished answer"), isNull(), anyMap()))
                .thenReturn(assistant);
        when(runtime.requirePublishedVersion("app-1", ACTOR.tenantId(), 9L))
                .thenReturn(new AiAppVersion("version-1", "app-1", ACTOR.tenantId(),
                        "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED",
                        Instant.now(), Instant.now()));
        when(agents.executeStream(eq(ACTOR), eq("agent-1"), isNull(), anyMap()))
                .thenReturn(Flux.<Map<String, Object>>just(Map.of("status", "STARTED",
                        "executionId", "exec-1", "runtimeRunId", "run-1"))
                        .concatWith(Flux.never()));
        when(agents.getExecution(ACTOR, "exec-1")).thenReturn(completed);
        when(completed.getStatus()).thenReturn(ExecutionStatus.COMPLETED);
        when(completed.getOutput()).thenReturn(Map.of("content", "finished answer"));

        var events = new AgentConversationService(repository, conversations, runtime, agents)
                .execute(ACTOR, "conversation-1", "app-1", null, "hello", null)
                .take(1).collectList().block();

        assertNotNull(events);
        assertEquals("STARTED", events.getFirst().get("status"));
        verify(conversations).appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("finished answer"), isNull(),
                argThat(trace -> "COMPLETED".equals(trace.get("status"))
                        && "run-1".equals(trace.get("runtimeRunId"))));
    }

    @Test
    void recordsCancellationWhenClientDisconnectsAfterStart() {
        ConversationRepository repository = mock(ConversationRepository.class);
        ConversationService conversations = mock(ConversationService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        Conversation conversation = mock(Conversation.class);
        ConversationMessage input = mock(ConversationMessage.class);
        ConversationMessage assistant = mock(ConversationMessage.class);
        when(conversation.sceneType()).thenReturn("agent");
        when(conversation.activeLeafMessageId()).thenReturn("user-1");
        when(input.id()).thenReturn("user-1");
        when(assistant.id()).thenReturn("assistant-1");
        when(repository.findConversation("conversation-1", ACTOR.tenantId(), 9L))
                .thenReturn(Optional.of(conversation));
        when(conversations.appendUserMessage(conversation, "hello")).thenReturn(input);
        when(conversations.appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("已停止"), isNull(), anyMap()))
                .thenReturn(assistant);
        when(runtime.requirePublishedVersion("app-1", ACTOR.tenantId(), 9L))
                .thenReturn(new AiAppVersion("version-1", "app-1", ACTOR.tenantId(),
                        "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED",
                        Instant.now(), Instant.now()));
        when(agents.executeStream(eq(ACTOR), eq("agent-1"), isNull(), anyMap()))
                .thenReturn(Flux.<Map<String, Object>>just(Map.of("status", "STARTED", "executionId", "exec-1",
                        "runtimeRunId", "run-1")).concatWith(Flux.never()));

        var events = new AgentConversationService(repository, conversations, runtime, agents)
                .execute(ACTOR, "conversation-1", "app-1", null, "hello", null)
                .take(1).collectList().block();

        assertNotNull(events);
        assertEquals("STARTED", events.getFirst().get("status"));
        verify(conversations).appendMessage(eq(conversation), eq("user-1"),
                eq(MessageRole.ASSISTANT), eq("已停止"), isNull(),
                argThat(trace -> "CANCELLED".equals(trace.get("status"))
                        && "run-1".equals(trace.get("runtimeRunId"))));
    }
}
