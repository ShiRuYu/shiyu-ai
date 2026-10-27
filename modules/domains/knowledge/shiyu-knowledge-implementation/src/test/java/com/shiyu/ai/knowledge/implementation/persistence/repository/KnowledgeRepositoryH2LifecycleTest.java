package com.shiyu.ai.knowledge.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.mybatisflex.core.datasource.FlexDataSource;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.mybatis.FlexSqlSessionFactoryBuilder;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.*;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.*;
import org.apache.ibatis.mapping.Environment;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** 用真实 H2 和生产映射器验证索引租约、入库领取、事务回滚和分块版本隔离。 */
@SuppressWarnings({"rawtypes", "unchecked"})
class KnowledgeRepositoryH2LifecycleTest {
    private static final TenantId TENANT = new TenantId(31);
    private JdbcTemplate jdbc;
    private KnowledgeEnterpriseRepositoryImpl enterprise;
    private KnowledgeChunkRepositoryImpl chunks;
    private MockedStatic<MapstructUtils> conversions;

    @BeforeEach
    void prepareDatabaseAndRepositories() {
        JdbcDataSource raw = new JdbcDataSource();
        raw.setURL("jdbc:h2:mem:knowledge_lifecycle_" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        FlexDataSource dataSource = new FlexDataSource("agent", raw);
        new ResourceDatabasePopulator(
                new ClassPathResource("db/baseline/h2/schema/knowledge/06_knowledge.sql"),
                new ClassPathResource("db/baseline/h2/schema/knowledge/09_vector.sql"))
                .execute(dataSource);
        jdbc = new JdbcTemplate(dataSource);
        FlexConfiguration configuration = new FlexConfiguration(new Environment(
                "agent", new SpringManagedTransactionFactory(), dataSource));
        var factory = new FlexSqlSessionFactoryBuilder().build(configuration);
        for (Class<?> type : List.of(KnowledgeSpaceMapper.class, KnowledgeSpaceMemberMapper.class,
                KnowledgeDocumentVersionMapper.class, KnowledgeDocumentMapper.class,
                KnowledgeReviewRecordMapper.class, KnowledgeIngestionJobMapper.class,
                KnowledgeAuditLogMapper.class, KnowledgeEvaluationCaseMapper.class,
                KnowledgeChunkMapper.class)) configuration.addMapper(type);
        SqlSessionTemplate session = new SqlSessionTemplate(factory);
        DataSourceTransactionManager transactions = new DataSourceTransactionManager(dataSource);
        enterprise = transactional(new KnowledgeEnterpriseRepositoryImpl(
                session.getMapper(KnowledgeSpaceMapper.class),
                session.getMapper(KnowledgeSpaceMemberMapper.class),
                session.getMapper(KnowledgeDocumentVersionMapper.class),
                session.getMapper(KnowledgeDocumentMapper.class),
                session.getMapper(KnowledgeReviewRecordMapper.class),
                session.getMapper(KnowledgeIngestionJobMapper.class),
                session.getMapper(KnowledgeAuditLogMapper.class),
                session.getMapper(KnowledgeEvaluationCaseMapper.class)), transactions);
        chunks = transactional(new KnowledgeChunkRepositoryImpl(
                session.getMapper(KnowledgeChunkMapper.class), dataSource), transactions);
        // 对象映射基础设施独立于本测试的 SQL、锁和事务契约，按同名属性转换真实数据对象。
        conversions = mockStatic(MapstructUtils.class);
        conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                .thenAnswer(call -> copy(call.getArgument(0), call.getArgument(1)));
        conversions.when(() -> MapstructUtils.convert(any(List.class), any(Class.class)))
                .thenAnswer(call -> ((List<?>) call.getArgument(0)).stream()
                        .map(value -> copy(value, call.getArgument(1))).toList());
        TenantScope.set(TENANT);
        jdbc.update("INSERT INTO knowledge_space(id,code,name,tenant_id,active_index_version)"
                + " VALUES(5,'space','知识',31,0),(6,'other','其他',32,0)");
        jdbc.update("INSERT INTO knowledge_document(id,title,content,tenant_id,space_id,current_version_id)"
                + " VALUES(10,'文档','',31,5,20),(11,'其他','',32,6,21)");
        jdbc.update("INSERT INTO knowledge_document_version(id,document_id,version_no,title,tenant_id,space_id)"
                + " VALUES(20,10,1,'',31,5),(21,11,1,'其他',32,6)");
    }

    @AfterEach
    void releaseContext() {
        TenantScope.clear();
        if (conversions != null) conversions.close();
    }

    @Test
    void rebuildLeaseRejectsForeignSpacesAndFencesPreviousOwners() {
        assertNull(enterprise.claimIndexRebuildLease(TENANT, null));
        assertNull(enterprise.claimIndexRebuildLease(TENANT, 6L));
        assertNull(enterprise.claimIndexRebuildLease(TENANT, 5L, null));
        long first = enterprise.claimIndexRebuildLease(TENANT, 5L);
        assertEquals(1, first);
        assertNull(enterprise.claimIndexRebuildLease(TENANT, 5L));
        assertTrue(enterprise.heartbeatIndexRebuildLease(TENANT, 5L, first, LocalDateTime.now()));
        assertFalse(enterprise.heartbeatIndexRebuildLease(TENANT, 5L, first + 1, LocalDateTime.now()));
        assertFalse(enterprise.activateIndexRebuild(TENANT, 5L, first + 1, 0L, 1L));
        assertTrue(enterprise.activateIndexRebuild(TENANT, 5L, first, 0L, 1L));
        assertTrue(enterprise.releaseIndexRebuildLease(TENANT, 5L, first, true));
        assertFalse(enterprise.releaseIndexRebuildLease(TENANT, 5L, first, true));
        long next = enterprise.claimIndexRebuildLease(TENANT, 5L);
        assertTrue(next > first);
        assertFalse(enterprise.activateIndexRebuild(TENANT, 5L, first, 1L, 99L));
        assertTrue(enterprise.releaseIndexRebuildLease(TENANT, 5L, next, false));
        assertEquals("PENDING", jdbc.queryForObject(
                "SELECT job_status FROM knowledge_ingestion_job WHERE job_type='INDEX_REBUILD_LEASE'", String.class));
    }

    @Test
    void ingestionClaimCanPublishOnlyCurrentDocumentVersionAndRollsBackPartialCompletion() {
        KnowledgeIngestionJobBO created = job("INGEST", 1);
        assertNull(enterprise.claimIngestionJob(TENANT, -1L));
        KnowledgeIngestionJobBO claimed = enterprise.claimIngestionJob(TENANT, created.getId());
        assertNotNull(claimed);
        assertEquals(1, claimed.getAttempts());
        assertNull(enterprise.claimIngestionJob(TENANT, claimed.getId()));
        assertFalse(enterprise.heartbeatIngestionJob(TENANT, claimed.getId(), 99L, LocalDateTime.now()));
        assertTrue(enterprise.heartbeatIngestionJob(TENANT, claimed.getId(), claimed.getLockVersion(), LocalDateTime.now()));
        claimed.setProgress(50);
        assertTrue(enterprise.updateClaimedIngestionJob(TENANT, claimed));
        KnowledgeDocumentVersionBO version = enterprise.findVersion(TENANT, 20L);
        version.setParseStatus("SUCCEEDED");
        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setId(10L); document.setContent("已解析"); document.setParseStatus("SUCCEEDED");
        jdbc.execute("ALTER TABLE knowledge_document_version ADD CONSTRAINT reject_published CHECK(parse_status<>'SUCCEEDED')");
        assertThrows(RuntimeException.class,
                () -> enterprise.completeClaimedIngestion(TENANT, claimed, version, document, "解析标题"));
        assertEquals("", jdbc.queryForObject("SELECT content FROM knowledge_document WHERE id=10", String.class));
        assertEquals("RUNNING", enterprise.findJob(TENANT, claimed.getId()).getJobStatus());
        jdbc.execute("ALTER TABLE knowledge_document_version DROP CONSTRAINT reject_published");
        assertTrue(enterprise.completeClaimedIngestion(TENANT, claimed, version, document, "解析标题"));
        assertEquals("已解析", jdbc.queryForObject("SELECT content FROM knowledge_document WHERE id=10", String.class));
        assertEquals("SUCCEEDED", enterprise.findJob(TENANT, claimed.getId()).getJobStatus());
        assertFalse(enterprise.completeClaimedIngestion(TENANT, claimed, version, document, "不应覆盖"));
        assertFalse(enterprise.updateClaimedIngestionJob(TENANT, claimed));
    }

    @Test
    void recoveryRetriesUntilExhaustionAndDoesNotOverwriteANewerDocument() {
        for (int attempts : List.of(1, 3)) {
            KnowledgeIngestionJobBO claimed = enterprise.claimIngestionJob(TENANT, job("INGEST", attempts).getId());
            claimed.setAttempts(attempts); claimed.setMaxAttempts(3);
            jdbc.update("UPDATE knowledge_ingestion_job SET heartbeat_time=?,attempts=? WHERE id=?",
                    LocalDateTime.now().minusHours(1), attempts, claimed.getId());
            assertTrue(enterprise.recoverStaleIngestionJob(TENANT, claimed, LocalDateTime.now().minusMinutes(10)));
            assertEquals(attempts < 3 ? "PENDING" : "FAILED", enterprise.findJob(TENANT, claimed.getId()).getJobStatus());
            assertFalse(enterprise.recoverStaleIngestionJob(TENANT, claimed, LocalDateTime.now()));
        }
        assertEquals("FAILED", jdbc.queryForObject("SELECT parse_status FROM knowledge_document WHERE id=10", String.class));
        KnowledgeIngestionJobBO stale = enterprise.claimIngestionJob(TENANT, job("INGEST", 1).getId());
        jdbc.update("UPDATE knowledge_document SET current_version_id=22,parse_status='PENDING' WHERE id=10");
        stale.setJobStatus("FAILED");
        assertFalse(enterprise.failClaimedIngestion(TENANT, stale, "FAILED"));
        assertFalse(enterprise.heartbeatIngestionJob(TENANT, stale.getId(), stale.getLockVersion(), LocalDateTime.now()));
        assertEquals("PENDING", jdbc.queryForObject("SELECT parse_status FROM knowledge_document WHERE id=10", String.class));
    }

    @Test
    void failedAndCancelledIngestionsCannotPublishOrRemainClaimable() {
        KnowledgeIngestionJobBO failed = enterprise.claimIngestionJob(TENANT, job("INGEST", 1).getId());
        failed.setJobStatus("FAILED"); failed.setStage("FAILED");
        assertTrue(enterprise.failClaimedIngestion(TENANT, failed, "FAILED"));
        assertFalse(enterprise.failClaimedIngestion(TENANT, failed, "FAILED"));
        assertEquals("FAILED", jdbc.queryForObject("SELECT parse_status FROM knowledge_document WHERE id=10", String.class));
        for (String stage : List.of("SUPERSEDED", "PENDING")) {
            KnowledgeIngestionJobBO pending = job("INGEST", 1); pending.setStage(stage);
            assertTrue(enterprise.cancelIngestionJob(TENANT, pending));
            assertFalse(enterprise.cancelIngestionJob(TENANT, pending));
            assertNull(enterprise.claimIngestionJob(TENANT, pending.getId()));
        }
    }

    @Test
    void rebuildJobsTrackAttemptsCancellationRecoveryAndActivationFencing() {
        assertTrue(enterprise.pollPendingIndexRebuildJobs(0).isEmpty());
        KnowledgeIngestionJobBO pending = job("INDEX_REBUILD", 1);
        assertFalse(enterprise.pollPendingIndexRebuildJobs(200).isEmpty());
        assertNull(enterprise.claimIngestionJob(TENANT, pending.getId()));
        KnowledgeIngestionJobBO claimed = enterprise.claimIndexRebuildJob(TENANT, pending.getId());
        assertNull(enterprise.claimIndexRebuildJob(TENANT, pending.getId()));
        assertTrue(enterprise.isIndexRebuildJobCurrent(TENANT, claimed.getId(), claimed.getLockVersion()));
        assertTrue(enterprise.beginIndexRebuildAttempt(TENANT, claimed.getId(), claimed.getLockVersion()));
        assertFalse(enterprise.beginIndexRebuildAttempt(TENANT, claimed.getId(), 99L));
        assertTrue(enterprise.heartbeatIndexRebuildJob(TENANT, claimed.getId(), claimed.getLockVersion(), LocalDateTime.now()));
        claimed.setProgress(30);
        assertTrue(enterprise.updateIndexRebuildJob(TENANT, claimed));
        long lease = enterprise.claimIndexRebuildLease(TENANT, 5L, claimed.getId());
        assertTrue(enterprise.cancelIndexRebuildJob(TENANT, claimed));
        assertFalse(enterprise.cancelIndexRebuildJob(TENANT, claimed));
        assertFalse(enterprise.activateIndexRebuild(TENANT, 5L, lease, 0L, 1L));
        assertFalse(enterprise.isIndexRebuildJobCurrent(TENANT, claimed.getId(), claimed.getLockVersion()));
        assertFalse(enterprise.updateIndexRebuildJob(TENANT, claimed));
        assertFalse(enterprise.activateIndexRebuild(TENANT, 5L, lease, 0L, 1L, claimed.getId(), claimed.getLockVersion()));
        KnowledgeIngestionJobBO retry = enterprise.claimIndexRebuildJob(TENANT, job("INDEX_REBUILD", 1).getId());
        jdbc.update("UPDATE knowledge_ingestion_job SET heartbeat_time=? WHERE id=?", LocalDateTime.now().minusHours(1), retry.getId());
        assertFalse(enterprise.findStaleIndexRebuildJobs(LocalDateTime.now().minusMinutes(5)).isEmpty());
        assertTrue(enterprise.recoverStaleIndexRebuildJob(TENANT, retry, LocalDateTime.now().minusMinutes(5)));
        assertFalse(enterprise.recoverStaleIndexRebuildJob(TENANT, retry, LocalDateTime.now()));
        assertEquals("PENDING", enterprise.findJob(TENANT, retry.getId()).getJobStatus());
        KnowledgeIngestionJobBO exhausted = enterprise.claimIndexRebuildJob(TENANT, job("INDEX_REBUILD", 1).getId());
        exhausted.setAttempts(3); exhausted.setMaxAttempts(3);
        jdbc.update("UPDATE knowledge_ingestion_job SET heartbeat_time=? WHERE id=?", LocalDateTime.now().minusHours(1), exhausted.getId());
        assertTrue(enterprise.recoverStaleIndexRebuildJob(TENANT, exhausted, LocalDateTime.now().minusMinutes(5)));
        assertEquals("FAILED", enterprise.findJob(TENANT, exhausted.getId()).getJobStatus());
    }

    @Test
    void chunksAreVersionAndTenantScopedAndReplacementIsTransactional() {
        KnowledgeChunkBO original = chunk(20L, "原文");
        assertTrue(chunks.replaceByDocumentVersion(TENANT, 10L, 20L, List.of(original)));
        assertEquals("原文", chunks.getById(TENANT, original.getId()).getContent());
        assertNull(chunks.getById(TENANT, -1L));
        assertEquals(1, chunks.findByIds(TENANT, 5L, List.of(original.getId())).size());
        assertTrue(chunks.findByIds(TENANT, 6L, List.of(original.getId())).isEmpty());
        assertTrue(chunks.findByIds(TENANT, 5L, List.of()).isEmpty());
        assertTrue(chunks.findByIds(TENANT, 5L, null).isEmpty());
        assertFalse(chunks.replaceByDocumentVersion(TENANT, 10L, 21L, List.of()));
        assertFalse(chunks.replaceByDocumentVersion(TENANT, 99L, 20L, List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersion(TENANT, 10L, 20L, List.of(chunk(21L,"错误"))));
        KnowledgeChunkBO invalid = chunk(20L, null);
        assertThrows(RuntimeException.class, () -> chunks.replaceByDocumentVersion(TENANT, 10L, 20L, List.of(invalid)));
        assertEquals("原文", chunks.findByDocumentVersion(TENANT, 10L, 20L).getFirst().getContent());
        assertTrue(chunks.copyByDocumentVersion(TENANT, 10L, 20L, 22L));
        assertEquals("原文", chunks.findByDocumentVersion(TENANT, 10L, 22L).getFirst().getContent());
        assertFalse(chunks.copyByDocumentVersion(TENANT, 10L, 99L, 23L));
        assertThrows(IllegalArgumentException.class, () -> chunks.copyByDocumentVersion(TENANT, 10L, 20L, 20L));
        KnowledgeIngestionJobBO claimed = enterprise.claimIngestionJob(TENANT, job("INGEST", 1).getId());
        assertTrue(chunks.replaceByDocumentVersionIfClaimed(TENANT, 10L, 20L, claimed.getId(), claimed.getLockVersion(), List.of(chunk(20L,"新内容"))));
        assertFalse(chunks.replaceByDocumentVersionIfClaimed(TENANT, 10L, 20L, claimed.getId(), 99L, List.of()));
        TenantScope.set(new TenantId(32));
        assertTrue(chunks.findByDocumentVersion(new TenantId(32), 10L, 20L).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> chunks.findByDocumentVersion(TENANT, 10L, 20L));
    }

    private KnowledgeIngestionJobBO job(String type, int attempts) {
        KnowledgeIngestionJobBO job = new KnowledgeIngestionJobBO();
        job.setJobKey(UUID.randomUUID().toString()); job.setJobType(type);
        job.setSpaceId(5L); job.setDocumentId(10L); job.setVersionId(20L);
        job.setJobStatus("PENDING"); job.setAttempts(attempts - 1); job.setMaxAttempts(3);
        job.setLockVersion(0L); job.setDelFlag(0);
        return enterprise.insertJob(TENANT, job);
    }

    @Test
    void incompleteDurableClaimsCannotMutateJobsOrDocumentContent() {
        KnowledgeIngestionJobBO pending = job("INGEST", 1);
        KnowledgeDocumentVersionBO version = enterprise.findVersion(TENANT, 20L);
        KnowledgeDocumentBO document = new KnowledgeDocumentBO(); document.setId(10L);
        for (KnowledgeIngestionJobBO invalid : java.util.Arrays.asList(null,
                new KnowledgeIngestionJobBO(), invalidClaim(pending.getId()))) {
            assertFalse(enterprise.updateClaimedIngestionJob(TENANT, invalid));
            assertFalse(enterprise.failClaimedIngestion(TENANT, invalid, "FAILED"));
            assertFalse(enterprise.completeClaimedIngestion(TENANT, invalid, version, document, null));
            assertFalse(enterprise.recoverStaleIngestionJob(TENANT, invalid, LocalDateTime.now()));
            assertFalse(enterprise.updateIndexRebuildJob(TENANT, invalid));
        }
        assertFalse(enterprise.cancelIngestionJob(TENANT, null));
        assertFalse(enterprise.cancelIngestionJob(TENANT, new KnowledgeIngestionJobBO()));
        assertFalse(enterprise.cancelIndexRebuildJob(TENANT, null));
        assertFalse(enterprise.cancelIndexRebuildJob(TENANT, new KnowledgeIngestionJobBO()));
        assertFalse(enterprise.recoverStaleIndexRebuildJob(TENANT, null, LocalDateTime.now()));
        assertFalse(enterprise.recoverStaleIndexRebuildJob(TENANT, new KnowledgeIngestionJobBO(), LocalDateTime.now()));
        assertFalse(enterprise.isIndexRebuildJobCurrent(TENANT, null, 1L));
        assertFalse(enterprise.isIndexRebuildJobCurrent(TENANT, pending.getId(), null));
        assertFalse(enterprise.heartbeatIngestionJob(TENANT, -1L, 1L, LocalDateTime.now()));
        KnowledgeIngestionJobBO claimed = enterprise.claimIngestionJob(TENANT, pending.getId());
        assertFalse(enterprise.completeClaimedIngestion(TENANT, claimed, null, document, null));
        assertFalse(enterprise.completeClaimedIngestion(TENANT, claimed, version, null, null));
        assertFalse(enterprise.heartbeatIndexRebuildJob(TENANT, pending.getId(), claimed.getLockVersion(), LocalDateTime.now()));
        assertNull(enterprise.claimIndexRebuildJob(TENANT, -1L));
        assertNull(enterprise.claimIndexRebuildJob(TENANT, pending.getId()));
        assertEquals("RUNNING", enterprise.findJob(TENANT, pending.getId()).getJobStatus());
        assertEquals("",jdbc.queryForObject("SELECT content FROM knowledge_document WHERE id=10",String.class));
    }

    @Test
    void malformedReplacementRequestsLeaveExistingChunksUntouched() {
        chunks.replaceByDocumentVersion(TENANT,10L,20L,List.of(chunk(20L,"保留")));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersion(TENANT,null,20L,List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersion(TENANT,10L,null,List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersion(TENANT,10L,20L,null));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersionIfClaimed(TENANT,null,20L,1L,1L,List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersionIfClaimed(TENANT,10L,null,1L,1L,List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersionIfClaimed(TENANT,10L,20L,null,1L,List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersionIfClaimed(TENANT,10L,20L,1L,null,List.of()));
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersionIfClaimed(TENANT,10L,20L,1L,1L,null));
        assertThrows(IllegalArgumentException.class, () -> chunks.copyByDocumentVersion(TENANT,null,20L,22L));
        assertThrows(IllegalArgumentException.class, () -> chunks.copyByDocumentVersion(TENANT,10L,null,22L));
        assertThrows(IllegalArgumentException.class, () -> chunks.copyByDocumentVersion(TENANT,10L,20L,null));
        KnowledgeChunkBO wrongDocument=chunk(20L,"错误"); wrongDocument.setDocumentId(11L);
        assertThrows(IllegalArgumentException.class, () -> chunks.replaceByDocumentVersion(TENANT,10L,20L,List.of(wrongDocument)));
        assertThrows(IllegalArgumentException.class, () -> chunks.findByIds(null,5L,List.of(1L)));
        assertEquals("保留",chunks.findByDocumentVersion(TENANT,10L,20L).getFirst().getContent());
    }

    @Test
    void rebuildActivationCannotPublishAfterItsLeaseWasReplaced() {
        KnowledgeIngestionJobBO pending=job("INDEX_REBUILD",1);
        KnowledgeIngestionJobBO claimed=enterprise.claimIndexRebuildJob(TENANT,pending.getId());
        enterprise.beginIndexRebuildAttempt(TENANT,claimed.getId(),claimed.getLockVersion());
        long lease=enterprise.claimIndexRebuildLease(TENANT,5L,claimed.getId());
        assertThrows(IllegalStateException.class, () -> enterprise.activateIndexRebuild(TENANT,5L,lease+1,0L,1L,claimed.getId(),claimed.getLockVersion()));
        assertEquals("LEASED_BUILD",enterprise.findJob(TENANT,claimed.getId()).getStage());
        assertTrue(enterprise.activateIndexRebuild(TENANT,5L,lease,0L,1L,claimed.getId(),claimed.getLockVersion()));
        assertFalse(enterprise.cancelIndexRebuildJob(TENANT,claimed));
        assertFalse(enterprise.activateIndexRebuild(TENANT,5L,lease,0L,2L,claimed.getId(),claimed.getLockVersion()));
        assertEquals(1L,jdbc.queryForObject("SELECT active_index_version FROM knowledge_space WHERE id=5",Long.class));
    }

    private static KnowledgeIngestionJobBO invalidClaim(Long id) {
        KnowledgeIngestionJobBO claim=new KnowledgeIngestionJobBO(); claim.setId(id); return claim;
    }

    @Test
    void optionalFiltersAndLegacyPendingClaimsPreserveTenantBoundaries() {
        assertEquals(2,enterprise.pageSpaces(1,10,"","").getTotal());
        assertEquals(1,enterprise.pageSpacesByTenant(TENANT,1,10,"","").getTotal());
        assertEquals(1,enterprise.pageSpacesByTenant(TENANT,1,10,null,null).getTotal());
        assertEquals(1,enterprise.pageSpacesByTenant(TENANT,1,10,"知识","GENERAL").getTotal());
        assertEquals(0,enterprise.pageSpacesByTenant(TENANT,1,10,"其他","GENERAL").getTotal());
        assertFalse(enterprise.hasMember(TENANT,5L,"USER",7L,List.of("OWNER")));
        assertThrows(IllegalArgumentException.class, () -> enterprise.updateVersion(TENANT,new KnowledgeDocumentVersionBO()));
        assertThrows(IllegalArgumentException.class, () -> enterprise.updateJob(TENANT,new KnowledgeIngestionJobBO()));
        KnowledgeIngestionJobBO legacy=job("INGEST",1);
        legacy.setLockVersion(null);
        assertTrue(enterprise.cancelIngestionJob(TENANT,legacy));
        KnowledgeIngestionJobBO pending=job("INDEX_REBUILD",1);
        pending.setLockVersion(null);
        assertTrue(enterprise.cancelIndexRebuildJob(TENANT,pending));
        assertEquals(2,enterprise.pageJobsByTenant(TENANT,1,10,null,"").getTotal());
        assertEquals(0,enterprise.pageJobsByTenant(TENANT,1,10,null,"RUNNING").getTotal());
        KnowledgeIngestionJobBO recovery=job("INDEX_REBUILD",1);
        jdbc.update("UPDATE knowledge_ingestion_job SET job_status='RUNNING',heartbeat_time=? WHERE id=?",
                LocalDateTime.now().minusHours(1),recovery.getId());
        recovery.setLockVersion(null); recovery.setAttempts(null); recovery.setMaxAttempts(null);
        recovery.setSpaceId(null);
        assertTrue(enterprise.recoverStaleIndexRebuildJob(TENANT,recovery,LocalDateTime.now().minusMinutes(5)));
        assertEquals("PENDING",enterprise.findJob(TENANT,recovery.getId()).getJobStatus());
    }

    private static KnowledgeChunkBO chunk(Long version, String content) {
        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setDocumentId(10L); chunk.setVersionId(version); chunk.setSpaceId(5L);
        chunk.setContent(content); chunk.setChunkIndex(0); chunk.setDelFlag(0);
        return chunk;
    }

    private static Object copy(Object source, Class<?> type) {
        if (source == null) return null;
        Object result = BeanUtils.instantiateClass(type);
        BeanUtils.copyProperties(source, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T transactional(T target, DataSourceTransactionManager manager) {
        ProxyFactory proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        proxy.addAdvice(interceptor);
        return (T) proxy.getProxy();
    }
}
