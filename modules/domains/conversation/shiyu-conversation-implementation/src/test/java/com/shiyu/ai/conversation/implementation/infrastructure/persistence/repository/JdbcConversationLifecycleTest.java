package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.contract.runtime.AiRunRepository;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.conversation.contract.model.*;
import com.shiyu.ai.conversation.implementation.application.ConversationService;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 验证真实数据库中的生成终态提交、领取互斥、租户隔离和事务回滚。 */
class JdbcConversationLifecycleTest {
    private static final TenantId TENANT = new TenantId(7);
    private static final Instant NOW = Instant.parse("2026-10-27T00:00:00Z");
    private JdbcTemplate jdbc;
    private JdbcConversationRepository conversations;
    private JdbcGenerationRepository generations;
    private AiRunRepository runtime;
    private ConversationService service;
    private JdbcIdempotencyRepository idempotency;

    @BeforeEach
    void setup() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:lifecycle" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(
                new ClassPathResource("db/baseline/h2/schema/conversation/11_conversation.sql"),
                new ClassPathResource("db/updates/conversation/20261024_generation_postprocessing.sql"))
                .execute(dataSource);
        jdbc = new JdbcTemplate(dataSource);
        runtime = mock(AiRunRepository.class);
        JdbcConversationRepository target = new JdbcConversationRepository(dataSource, runtime);
        ProxyFactory proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(dataSource),
                new AnnotationTransactionAttributeSource()));
        conversations = (JdbcConversationRepository) proxy.getProxy();
        generations = new JdbcGenerationRepository(dataSource, runtime);
        idempotency = new JdbcIdempotencyRepository(dataSource);
        ProxyFactory serviceProxy = new ProxyFactory(new ConversationService(conversations, generations, idempotency));
        serviceProxy.setProxyTargetClass(true);
        serviceProxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(dataSource),
                new AnnotationTransactionAttributeSource()));
        service = (ConversationService) serviceProxy.getProxy();
        TenantScope.set(TENANT);
        conversations.insertConversation(conversation(0));
        conversations.insertMessage(message("input", MessageRole.USER, "c", null));
        generations.insert(run(GenerationStatus.RUNNING, 0, "runtime"));
    }

    @AfterEach
    void clearTenant() {
        TenantScope.clear();
    }

    @Test
    void completionCommitsMessageConversationRuntimeAndPostProcessingTogether() {
        GenerationRun completed = run(GenerationStatus.COMPLETED, 1, null);
        assertEquals(1, conversations.completeGeneration(completed, 0,
                message("assistant", MessageRole.ASSISTANT, "c", "input"), conversation(1), 0,
                List.of(AiRunEventType.MODEL_COMPLETED)));
        assertEquals("assistant", conversations.findMessage("assistant", TENANT, 8).orElseThrow().id());
        assertEquals(1, conversations.findConversation("c", TENANT, 8).orElseThrow().version());
        assertEquals(GenerationStatus.COMPLETED, generations.find("generation", TENANT, 8).orElseThrow().status());
        assertEquals(0, count("CHAT_GENERATION_ACTIVE"));
        verify(runtime).finishGenerationRun(eq("runtime"), eq(TENANT), eq(8L),
                eq(AiRunStatus.COMPLETED), isNull(), eq(List.of(AiRunEventType.MODEL_COMPLETED)),
                eq("turn-generation"), eq("model-generation"));
        GenerationPostProcessing claimed = conversations.claimGenerationPostProcessingNext().orElseThrow();
        assertEquals(GenerationPostProcessing.QuotaAction.SETTLE, claimed.quotaAction());
        assertEquals(GenerationPostProcessing.UsageAction.COMPLETED, claimed.usageAction());
        assertFalse(claimed.quotaDone());
        assertFalse(claimed.usageDone());
        assertEquals("runtime", claimed.run().runtimeRunId());
        assertEquals(8, claimed.ownerUserId());
    }

    @Test
    void conversationVersionConflictRollsBackCompletedGenerationAndAssistantMessage() {
        assertThrows(IllegalStateException.class, () -> conversations.completeGeneration(
                run(GenerationStatus.COMPLETED, 1, "runtime"), 0,
                message("assistant", MessageRole.ASSISTANT, "c", "input"), conversation(1), 99, List.of()));
        assertEquals(GenerationStatus.RUNNING, generations.find("generation", TENANT, 8).orElseThrow().status());
        assertTrue(conversations.findMessage("assistant", TENANT, 8).isEmpty());
        assertEquals(1, count("CHAT_GENERATION_ACTIVE"));
        assertEquals(0, count("CHAT_GENERATION_POSTPROCESSING"));
        verifyNoInteractions(runtime);
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrong-conversation", "wrong-role", "wrong-message"})
    void rejectsAssistantThatDoesNotBelongToCompletedGeneration(String mismatch) {
        ConversationMessage assistant = message(mismatch.equals("wrong-message") ? "other" : "assistant",
                mismatch.equals("wrong-role") ? MessageRole.USER : MessageRole.ASSISTANT,
                mismatch.equals("wrong-conversation") ? "other" : "c", "input");
        assertThrows(IllegalArgumentException.class, () -> conversations.completeGeneration(
                run(GenerationStatus.COMPLETED, 1, "runtime"), 0, assistant, conversation(1), 0, List.of()));
        assertEquals(GenerationStatus.RUNNING, generations.find("generation", TENANT, 8).orElseThrow().status());
        assertEquals(1, count("CHAT_MESSAGE"));
    }

    @Test
    void staleCompletionAndTenantMismatchCannotChangePersistedState() {
        assertEquals(0, conversations.completeGeneration(run(GenerationStatus.COMPLETED, 1, "runtime"), 99,
                message("assistant", MessageRole.ASSISTANT, "c", "input"), conversation(1), 0, List.of()));
        TenantScope.set(new TenantId(9));
        assertThrows(IllegalArgumentException.class, () -> conversations.completeGeneration(
                run(GenerationStatus.COMPLETED, 1, "runtime"), 0,
                message("assistant", MessageRole.ASSISTANT, "c", "input"), conversation(1), 0, List.of()));
        assertEquals(0, count("CHAT_GENERATION_POSTPROCESSING"));
        assertEquals(1, count("CHAT_MESSAGE"));
    }

    @ParameterizedTest
    @EnumSource(value = GenerationStatus.class, names = {"FAILED", "CANCELLED"})
    void terminalTransitionCreatesCorrectDurableSideEffects(GenerationStatus status) {
        assertEquals(1, conversations.transitionRunningGeneration(run(status, 1, null), 0, 8, List.of()));
        assertEquals(0, count("CHAT_GENERATION_ACTIVE"));
        var claimed = conversations.claimGenerationPostProcessing("generation").orElseThrow();
        assertEquals(GenerationPostProcessing.QuotaAction.RELEASE, claimed.quotaAction());
        assertEquals(status == GenerationStatus.FAILED ? GenerationPostProcessing.UsageAction.FAILED
                : GenerationPostProcessing.UsageAction.NONE, claimed.usageAction());
        assertEquals(status == GenerationStatus.CANCELLED, claimed.usageDone());
        verify(runtime).finishGenerationRun(eq("runtime"), eq(TENANT), eq(8L),
                eq(status == GenerationStatus.FAILED ? AiRunStatus.FAILED : AiRunStatus.CANCELLED),
                isNull(), eq(List.of()), eq("turn-generation"), eq("model-generation"));
        assertEquals(0, conversations.transitionRunningGeneration(run(status, 2, "runtime"), 0, 8, List.of()));
    }

    @Test
    void transitionRejectsNonTerminalStateAndMissingTenant() {
        assertThrows(IllegalArgumentException.class, () -> conversations.transitionRunningGeneration(
                run(GenerationStatus.RUNNING, 1, "runtime"), 0, 8, List.of()));
        TenantScope.clear();
        assertThrows(IllegalStateException.class, () -> conversations.transitionRunningGeneration(
                run(GenerationStatus.FAILED, 1, "runtime"), 0, 8, List.of()));
        assertEquals(1, count("CHAT_GENERATION_ACTIVE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void transitionWithoutRuntimeLinkStillPersistsPostProcessing(String link) {
        jdbc.update("UPDATE CHAT_GENERATION_RUN SET RUNTIME_RUN_ID=NULL");
        assertEquals(1, conversations.transitionRunningGeneration(run(GenerationStatus.FAILED, 1, link), 0, 8, List.of()));
        assertEquals(1, count("CHAT_GENERATION_POSTPROCESSING"));
        verifyNoInteractions(runtime);
    }

    @Test
    void claimsAreExclusiveAndWrongTokensCannotAdvanceOrReleaseThem() {
        conversations.transitionRunningGeneration(run(GenerationStatus.FAILED, 1, "runtime"), 0, 8, List.of());
        var first = conversations.claimGenerationPostProcessing("generation").orElseThrow();
        assertTrue(conversations.claimGenerationPostProcessing("generation").isEmpty());
        assertTrue(conversations.claimGenerationPostProcessing("missing").isEmpty());
        assertTrue(conversations.claimGenerationPostProcessingNext().isEmpty());
        assertEquals(0, conversations.completeGenerationPostProcessing("generation", GenerationPostProcessing.Step.QUOTA, "wrong"));
        assertEquals(0, conversations.releaseGenerationPostProcessing("generation", "wrong", 0));
        assertEquals(1, conversations.completeGenerationPostProcessing("generation", GenerationPostProcessing.Step.QUOTA, first.claimToken()));
        assertEquals(1, count("CHAT_GENERATION_POSTPROCESSING"));
        assertEquals(1, conversations.completeGenerationPostProcessing("generation", GenerationPostProcessing.Step.USAGE, first.claimToken()));
        assertEquals(0, count("CHAT_GENERATION_POSTPROCESSING"));
    }

    @Test
    void expiredClaimIsFencedAndReleasePreservesCompletedStepsForRetry() {
        conversations.transitionRunningGeneration(run(GenerationStatus.FAILED, 1, "runtime"), 0, 8, List.of());
        var first = conversations.claimGenerationPostProcessingNext().orElseThrow();
        conversations.completeGenerationPostProcessing("generation", GenerationPostProcessing.Step.QUOTA, first.claimToken());
        jdbc.update("UPDATE CHAT_GENERATION_POSTPROCESSING SET CLAIMED_UNTIL=DATEADD('SECOND',-1,CURRENT_TIMESTAMP)");
        var renewed = conversations.claimGenerationPostProcessingNext().orElseThrow();
        assertNotEquals(first.claimToken(), renewed.claimToken());
        assertTrue(renewed.quotaDone());
        assertEquals(0, conversations.releaseGenerationPostProcessing("generation", first.claimToken(), 0));
        assertEquals(1, conversations.releaseGenerationPostProcessing("generation", renewed.claimToken(), 0));
        assertTrue(conversations.claimGenerationPostProcessingNext().isEmpty());
        jdbc.update("UPDATE CHAT_GENERATION_POSTPROCESSING SET NEXT_ATTEMPT_AT=DATEADD('SECOND',-1,CURRENT_TIMESTAMP)");
        var retry = conversations.claimGenerationPostProcessingNext().orElseThrow();
        assertEquals(1, retry.attempts());
        assertTrue(retry.quotaDone());
        assertFalse(retry.usageDone());
    }

    @ParameterizedTest
    @EnumSource(GenerationStatus.class)
    void schedulerUpdateWithoutTenantScopeUsesVersionAndCleansOnlyTerminalClaims(GenerationStatus status) {
        TenantScope.clear();
        assertEquals(0, generations.update(run(status, 1, null), 99));
        assertEquals(1, generations.update(run(status, 1, null), 0));
        assertEquals(status.name(), jdbc.queryForObject("SELECT STATUS FROM CHAT_GENERATION_RUN", String.class));
        boolean terminal = status == GenerationStatus.COMPLETED || status == GenerationStatus.FAILED
                || status == GenerationStatus.CANCELLED;
        assertEquals(terminal ? 0 : 1, count("CHAT_GENERATION_ACTIVE"));
    }

    @ParameterizedTest
    @EnumSource(GenerationStatus.class)
    void tenantScopedUpdateUsesVersionAndCleansOnlyTerminalClaims(GenerationStatus status) {
        assertEquals(0, generations.update(run(status, 1, null), 99));
        assertEquals(1, generations.update(run(status, 1, null), 0));
        boolean terminal = status == GenerationStatus.COMPLETED || status == GenerationStatus.FAILED
                || status == GenerationStatus.CANCELLED;
        assertEquals(terminal ? 0 : 1, count("CHAT_GENERATION_ACTIVE"));
    }

    @Test
    void createdGenerationRollbackDeletesOnlyAnUnstartedClaim() {
        assertEquals(0, generations.deleteCreated("generation", TENANT, 8));
        jdbc.update("UPDATE CHAT_GENERATION_RUN SET STATUS='CREATED'");
        assertEquals(1, generations.deleteCreated("generation", TENANT, 8));
        assertEquals(0, count("CHAT_GENERATION_ACTIVE"));
        assertEquals(0, count("CHAT_GENERATION_RUN"));
        assertEquals(0, generations.deleteCreated("generation", TENANT, 8));
    }

    @Test
    void completionWithoutRuntimeLinkStillCommitsItsDurableWork() {
        jdbc.update("UPDATE CHAT_GENERATION_RUN SET RUNTIME_RUN_ID=NULL");
        assertEquals(1, conversations.completeGeneration(run(GenerationStatus.COMPLETED, 1, null), 0,
                message("assistant", MessageRole.ASSISTANT, "c", "input"), conversation(1), 0, List.of()));
        assertEquals(1, count("CHAT_GENERATION_POSTPROCESSING"));
        verifyNoInteractions(runtime);
    }

    @Test
    void idempotentConversationCreationPersistsSystemPromptAndRejectsDuplicateKey() {
        var created = service.createIdempotent(TENANT, 8, "chat", "  Named  ", "OPENAI", "model",
                "System instructions", "create", "key", "fingerprint", "created");
        assertEquals("Named", created.title());
        assertEquals("System instructions", conversations.listMessages("created", TENANT, 8, 10).getFirst().textContent());
        assertEquals("fingerprint", idempotency.findFingerprint(TENANT, 8, "create", "key").orElseThrow());
        assertThrows(IllegalStateException.class, () -> service.createIdempotent(TENANT, 8, "chat", "Other",
                "OPENAI", "model", null, "create", "key", "fingerprint", "duplicate"));
        assertTrue(conversations.findConversation("duplicate", TENANT, 8).isEmpty());
        var branch = service.branch(created, created.activeLeafMessageId());
        assertEquals(created.id(), branch.parentConversationId());
        assertEquals(created.activeLeafMessageId(), branch.branchFromMessageId());
        assertEquals(1, conversations.listBranches(created.id(), TENANT, 8).size());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "message-key"})
    void messageGenerationPersistsInputAndMovesLeafAsOneTransaction(String key) {
        clearRunningGeneration();
        var run = service.createMessageGeneration(conversation(0), "Followup", "OPENAI", "model",
                idempotency, "generate", key, "fingerprint", "new-run", "new-input", "speaker");
        assertEquals("new-input", run.inputMessageId());
        assertEquals("speaker", run.speakerId());
        assertEquals(GenerationStatus.CREATED, run.status());
        assertEquals("new-input", conversations.findConversation("c", TENANT, 8).orElseThrow().activeLeafMessageId());
        assertEquals("Followup", conversations.findMessage("new-input", TENANT, 8).orElseThrow().textContent());
        if (key != null && !key.isBlank()) {
            assertEquals("new-run", idempotency.find(TENANT, 8, "generate", key).orElseThrow());
            assertThrows(IllegalStateException.class, () -> service.createMessageGeneration(conversation(0),
                    "Duplicate", "OPENAI", "model", idempotency, "generate", key, "fingerprint", "dup", "dup-input", null));
            assertTrue(conversations.findMessage("dup-input", TENANT, 8).isEmpty());
        }
    }

    @Test
    void generationRetryMovesLeafBackToTheRequestedBranchAndClaimsIdempotency() {
        clearRunningGeneration();
        var input = conversations.findMessage("input", TENANT, 8).orElseThrow();
        service.appendUserMessage(conversation(0), "Other branch");
        var latest = conversations.findConversation("c", TENANT, 8).orElseThrow();
        var run = service.createGenerationIdempotent(latest, input, "OPENAI", "model", "retry", "key",
                "fingerprint", "retry-run", true);
        assertEquals("input", conversations.findConversation("c", TENANT, 8).orElseThrow().activeLeafMessageId());
        assertEquals("retry-run", idempotency.find(TENANT, 8, "retry", "key").orElseThrow());
        assertEquals("input", run.inputMessageId());
    }

    @ParameterizedTest
    @CsvSource({"true,true", "false,true", "true,false", "false,false"})
    void rejectedGenerationRollbackRestoresOnlyRequestedState(boolean deleteInput, boolean restoreLeaf) {
        clearRunningGeneration();
        var run = service.createMessageGeneration(conversation(0), "Rejected", "OPENAI", "model",
                idempotency, "generate", "key", "fingerprint", "rejected", "rejected-input", null);
        service.rollbackRejectedGeneration(run, TENANT, 8, deleteInput, restoreLeaf, "input", "generate", "key");
        assertTrue(generations.find("rejected", TENANT, 8).isEmpty());
        assertTrue(idempotency.find(TENANT, 8, "generate", "key").isEmpty());
        assertEquals(restoreLeaf ? "input" : "rejected-input",
                conversations.findConversation("c", TENANT, 8).orElseThrow().activeLeafMessageId());
        assertEquals(!(deleteInput && restoreLeaf), conversations.findMessage("rejected-input", TENANT, 8).isPresent());
        service.rollbackRejectedGeneration(run, TENANT, 8, deleteInput, restoreLeaf, "input", "generate", "key");
        assertEquals(1, count("CHAT_CONVERSATION"));
    }

    @Test
    void rejectedGenerationRollbackKeepsNewerBranchMessagesAndLeaf() {
        clearRunningGeneration();
        var run = service.createMessageGeneration(conversation(0), "Rejected", "OPENAI", "model",
                idempotency, "generate", "key", "fingerprint", "rejected", "rejected-input", null);
        var after = conversations.findConversation("c", TENANT, 8).orElseThrow();
        var newer = service.appendUserMessage(after, "Later user turn");
        service.rollbackRejectedGeneration(run, TENANT, 8, true, true, "input", "generate", "key");
        assertEquals(newer.id(), conversations.findConversation("c", TENANT, 8).orElseThrow().activeLeafMessageId());
        assertTrue(conversations.findMessage("rejected-input", TENANT, 8).isPresent());
        assertTrue(generations.find("rejected", TENANT, 8).isEmpty());
    }

    @Test
    void failedRetryOnNonActiveLeafRollsBackItsIdempotencyClaim() {
        clearRunningGeneration();
        var input = conversations.findMessage("input", TENANT, 8).orElseThrow();
        service.appendUserMessage(conversation(0), "Newer leaf");
        var latest = conversations.findConversation("c", TENANT, 8).orElseThrow();
        assertThrows(IllegalStateException.class, () -> service.createGenerationIdempotent(latest, input,
                "OPENAI", "model", "retry", "failed-key", "fingerprint", "rejected", false));
        assertTrue(idempotency.find(TENANT, 8, "retry", "failed-key").isEmpty());
        assertTrue(generations.find("rejected", TENANT, 8).isEmpty());
    }

    @Test
    void retryOnActiveLeafDoesNotIncrementConversationVersion() {
        clearRunningGeneration();
        var input = conversations.findMessage("input", TENANT, 8).orElseThrow();
        var run = service.createGenerationIdempotent(conversation(0), input, "OPENAI", "model",
                "retry", "key", "fingerprint", "active-retry", false);
        assertEquals("input", run.inputMessageId());
        assertEquals(0, conversations.findConversation("c", TENANT, 8).orElseThrow().version());
        assertThrows(IllegalStateException.class, () -> service.createGenerationIdempotent(conversation(0), input,
                "OPENAI", "model", "retry", "key", "fingerprint", "duplicate", false));
    }

    @Test
    void compatibilityServiceWithoutIdempotencyCannotCreateIdempotentResources() {
        var legacy = new ConversationService(conversations, generations);
        assertThrows(IllegalStateException.class, () -> legacy.createIdempotent(TENANT, 8, "chat", null,
                null, null, null, "create", "key", "fingerprint", "invalid"));
        assertThrows(IllegalStateException.class, () -> legacy.createGenerationIdempotent(conversation(0),
                conversations.findMessage("input", TENANT, 8).orElseThrow(), "OPENAI", "model", "retry", "key",
                "fingerprint", "invalid", false));
        assertTrue(conversations.findConversation("invalid", TENANT, 8).isEmpty());
        assertTrue(generations.find("invalid", TENANT, 8).isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rollbackWithoutIdempotencyKeyDoesNotRequireAClaim(String key) {
        clearRunningGeneration();
        var run = service.createMessageGeneration(conversation(0), "Input", "OPENAI", "model", idempotency,
                "generate", key, "fingerprint", "rejected", "rejected-input", null);
        service.rollbackRejectedGeneration(run, TENANT, 8, true, true, "input", "generate", key);
        assertEquals(0, count("CHAT_GENERATION_RUN"));
        assertEquals(0, count("CHAT_IDEMPOTENCY_KEY"));
    }

    @Test
    void rollbackAfterConversationDisappearsStillReleasesItsIdempotencyClaim() {
        clearRunningGeneration();
        var run = service.createMessageGeneration(conversation(0), "Input", "OPENAI", "model", idempotency,
                "generate", "key", "fingerprint", "rejected", "rejected-input", null);
        jdbc.update("DELETE FROM CHAT_CONVERSATION");
        service.rollbackRejectedGeneration(run, TENANT, 8, true, true, "input", "generate", "key");
        assertEquals(0, count("CHAT_GENERATION_RUN"));
        assertTrue(idempotency.find(TENANT, 8, "generate", "key").isEmpty());
    }

    @Test
    void idempotentMessagePersistsOnceAndDuplicateDoesNotMoveLeaf() {
        var saved = service.appendMessageIdempotent(conversation(0), "input", MessageRole.USER,
                "Idempotent input", "input", idempotency, "message", "key", "fingerprint", "idempotent-input");
        assertEquals("idempotent-input", saved.id());
        assertEquals("input", saved.sourceMessageId());
        assertEquals("idempotent-input", conversations.findConversation("c", TENANT, 8).orElseThrow().activeLeafMessageId());
        assertThrows(IllegalStateException.class, () -> service.appendMessageIdempotent(conversation(0),
                "input", MessageRole.USER, "Duplicate", null, idempotency, "message", "key", "fingerprint", "duplicate"));
        assertTrue(conversations.findMessage("duplicate", TENANT, 8).isEmpty());
    }

    @Test
    void generationInputRequiresBothConversationAndMessage() {
        var input = conversations.findMessage("input", TENANT, 8).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> service.createGeneration(null, input, "OPENAI", "model"));
        assertThrows(IllegalArgumentException.class, () -> service.createGeneration(conversation(0), null, "OPENAI", "model"));
        assertEquals(1, count("CHAT_GENERATION_RUN"));
    }

    @Test
    void missingAndBlankSystemPromptCreateNoMessagesAndUseDefaultTitle() {
        var empty = service.create(TENANT, 8, "chat", null, "OPENAI", "model", " ");
        var absent = service.create(TENANT, 8, "chat", "Named", "OPENAI", "model", null);
        assertEquals("New conversation", empty.title());
        assertTrue(conversations.listMessages(empty.id(), TENANT, 8, 10).isEmpty());
        assertTrue(conversations.listMessages(absent.id(), TENANT, 8, 10).isEmpty());
    }

    @Test
    void retryWithMoveLeafEnabledOnTheSameLeafDoesNotRewriteConversation() {
        clearRunningGeneration();
        var input = conversations.findMessage("input", TENANT, 8).orElseThrow();
        service.createGenerationIdempotent(conversation(0), input, "OPENAI", "model", "retry", "key",
                "fingerprint", "same-leaf", true);
        assertEquals(0, conversations.findConversation("c", TENANT, 8).orElseThrow().version());
    }

    @Test
    void compatibilityRollbackWithoutIdempotencyRepositoryStillDeletesCreatedRun() {
        clearRunningGeneration();
        var run = service.createMessageGeneration(conversation(0), "Input", "OPENAI", "model", idempotency,
                "generate", null, "fingerprint", "rejected", "rejected-input", null);
        new ConversationService(conversations, generations).rollbackRejectedGeneration(run, TENANT, 8,
                false, false, "input", "generate", "unused-key");
        assertEquals(0, count("CHAT_GENERATION_RUN"));
    }

    @Test
    void runtimeTerminalDoesNothingWithoutALinkAndDelegatesValidLink() {
        assertNull(generations.finishRuntimeTerminal(null, TENANT, 8, AiRunStatus.FAILED, "error", List.of(), "turn", "step"));
        assertNull(generations.finishRuntimeTerminal(" ", TENANT, 8, AiRunStatus.FAILED, "error", List.of(), "turn", "step"));
        verifyNoInteractions(runtime);
        generations.finishRuntimeTerminal("runtime", TENANT, 8, AiRunStatus.FAILED, "error", List.of(), "turn", "step");
        verify(runtime).finishGenerationRun("runtime", TENANT, 8, AiRunStatus.FAILED, "error", List.of(), "turn", "step");
    }

    private void clearRunningGeneration() {
        jdbc.update("DELETE FROM CHAT_GENERATION_ACTIVE");
        jdbc.update("DELETE FROM CHAT_GENERATION_RUN");
    }

    private Conversation conversation(long version) {
        return new Conversation("c", 7, 8, "chat", "Conversation", ConversationStatus.ACTIVE,
                null, null, version == 0 ? "input" : "assistant", "", "OPENAI", "model", version, NOW, NOW);
    }

    private GenerationRun run(GenerationStatus status, long version, String runtimeId) {
        return new GenerationRun("generation", "c", "input", status == GenerationStatus.COMPLETED ? "assistant" : null,
                null, "OPENAI", "model", status, 12, 8, 20, null, 2, false, version, NOW, NOW, runtimeId);
    }

    private ConversationMessage message(String id, MessageRole role, String conversationId, String parentId) {
        return new ConversationMessage(id, conversationId, parentId, null, role,
                List.of(ContentPart.text("text")), Map.of(), MessageStatus.COMPLETED, 0, "generation", NOW, NOW);
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
