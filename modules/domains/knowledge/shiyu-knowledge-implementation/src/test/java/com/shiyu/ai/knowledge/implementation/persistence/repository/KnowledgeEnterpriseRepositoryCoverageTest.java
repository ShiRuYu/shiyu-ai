package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.paginate.Page;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.*;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.*;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class KnowledgeEnterpriseRepositoryCoverageTest {
    private static final TenantId TENANT = new TenantId(31L);

    @Test
    void exercisesTenantScopedSpaceVersionJobAuditAndEvaluationPersistence() {
        KnowledgeSpaceMapper spaces = mock(KnowledgeSpaceMapper.class, mapperAnswer(KnowledgeSpaceDO.class));
        KnowledgeSpaceMemberMapper members = mock(KnowledgeSpaceMemberMapper.class, mapperAnswer(KnowledgeSpaceMemberDO.class));
        KnowledgeDocumentVersionMapper versions = mock(KnowledgeDocumentVersionMapper.class, mapperAnswer(KnowledgeDocumentVersionDO.class));
        KnowledgeReviewRecordMapper reviews = mock(KnowledgeReviewRecordMapper.class, mapperAnswer(KnowledgeReviewRecordDO.class));
        KnowledgeIngestionJobMapper jobs = mock(KnowledgeIngestionJobMapper.class, mapperAnswer(KnowledgeIngestionJobDO.class));
        KnowledgeAuditLogMapper audits = mock(KnowledgeAuditLogMapper.class, mapperAnswer(KnowledgeAuditLogDO.class));
        KnowledgeEvaluationCaseMapper evaluations = mock(KnowledgeEvaluationCaseMapper.class, mapperAnswer(KnowledgeEvaluationCaseDO.class));
        when(versions.selectOneByQuery(any())).thenAnswer(invocation -> {
            KnowledgeDocumentVersionDO value = new KnowledgeDocumentVersionDO();
            value.setId(1L);
            value.setVersionNo(1);
            return value;
        });
        KnowledgeEnterpriseRepositoryImpl repository = new KnowledgeEnterpriseRepositoryImpl(
                spaces, members, versions, reviews, jobs, audits, evaluations);

        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), any(Class.class)))
                    .thenAnswer(invocation -> convertList((List<?>) invocation.getArgument(0), invocation.getArgument(1)));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), any(Class.class)))
                    .thenAnswer(invocation -> convertValue(invocation.getArgument(0), invocation.getArgument(1)));

            assertNotNull(repository.findSpace(TENANT, 1L));
            assertNull(repository.findSpaceByTenant(TENANT, null));
            assertNotNull(repository.findSpaceByTenant(TENANT, 1L));
            assertEquals(1, repository.findActiveSpacesByTenant(TENANT).size());
            assertEquals(1, repository.findAllActiveSpaces().size());
            assertNotNull(repository.findSpaceByTenantAndCode(TENANT, "space"));
            assertEquals(1, repository.pageSpaces(1, 10, null).getTotal());
            assertEquals(1, repository.pageSpaces(1, 10, "math", "school").getTotal());
            assertEquals(1, repository.pageSpacesByTenant(TENANT, 1, 10, "math", "school").getTotal());

            KnowledgeSpaceBO space = new KnowledgeSpaceBO();
            assertSame(space, repository.insertSpace(TENANT, space));
            repository.updateSpace(TENANT, space);
            repository.deleteSpace(TENANT, 1L);
            assertEquals(1, repository.findMembers(TENANT, 1L).size());
            repository.replaceMembers(TENANT, 1L, List.of(new KnowledgeSpaceMemberBO()));
            repository.replaceMembers(TENANT, 1L, List.of());
            assertTrue(repository.hasMember(TENANT, 1L, "USER", 7L, List.of("OWNER")));
            assertFalse(repository.hasMember(TENANT, 1L, "USER", null, List.of("OWNER")));

            assertNotNull(repository.findVersion(TENANT, 1L));
            assertEquals(1, repository.findVersions(TENANT, 1L).size());
            assertEquals(2, repository.nextVersionNo(TENANT, 1L));
            when(versions.selectOneByQuery(any())).thenReturn(null);
            assertEquals(1, repository.nextVersionNo(TENANT, 1L));
            KnowledgeDocumentVersionBO version = new KnowledgeDocumentVersionBO();
            assertSame(version, repository.insertVersion(TENANT, version));
            repository.updateVersion(TENANT, version);
            assertThrows(IllegalArgumentException.class, () -> repository.insertVersion(null, version));
            assertThrows(IllegalArgumentException.class, () -> repository.insertVersion(TENANT, null));
            assertThrows(IllegalArgumentException.class, () -> repository.updateVersion(TENANT, null));

            repository.insertReview(TENANT, new KnowledgeReviewRecordBO());
            assertThrows(IllegalArgumentException.class, () -> repository.insertReview(TENANT, null));
            assertNotNull(repository.findJob(TENANT, 1L));
            assertNotNull(repository.findJobByKey(TENANT, "job"));
            KnowledgeIngestionJobBO job = new KnowledgeIngestionJobBO();
            assertSame(job, repository.insertJob(TENANT, job));
            repository.updateJob(TENANT, job);
            assertThrows(IllegalArgumentException.class, () -> repository.insertJob(TENANT, null));
            assertThrows(IllegalArgumentException.class, () -> repository.updateJob(TENANT, null));
            assertEquals(1, repository.pageJobsByTenant(TENANT, 1, 10, null, null).getTotal());
            assertEquals(1, repository.pageJobsByTenant(TENANT, 1, 10, 2L, "PENDING").getTotal());
            assertEquals(1, repository.pollPendingJobs(5).size());
            assertEquals(1, repository.findStaleJobs(java.time.LocalDateTime.now()).size());

            repository.insertAudit(TENANT, new KnowledgeAuditLogBO());
            assertThrows(IllegalArgumentException.class, () -> repository.insertAudit(TENANT, null));
            assertEquals(1, repository.pageAudit(TENANT, 1, 10, null).getTotal());
            assertEquals(1, repository.pageAudit(TENANT, 1, 10, 2L).getTotal());
            KnowledgeEvaluationCaseBO evaluation = new KnowledgeEvaluationCaseBO();
            assertSame(evaluation, repository.insertEvaluation(TENANT, evaluation));
            assertEquals(1, repository.pageEvaluations(TENANT, 1, 10, 2L).getTotal());
            assertNotNull(repository.findEvaluation(TENANT, 1L));
            repository.deleteEvaluation(TENANT, 1L);
        }
        assertThrows(IllegalArgumentException.class, () -> repository.findSpace(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> repository.findMembers(null, 1L));
    }

    @Test
    void commandWritesFailWhenNoTenantOwnedRowIsAffected() {
        KnowledgeSpaceMapper spaces = mock(KnowledgeSpaceMapper.class);
        KnowledgeSpaceMemberMapper members = mock(KnowledgeSpaceMemberMapper.class);
        KnowledgeDocumentVersionMapper versions = mock(KnowledgeDocumentVersionMapper.class);
        KnowledgeReviewRecordMapper reviews = mock(KnowledgeReviewRecordMapper.class);
        KnowledgeIngestionJobMapper jobs = mock(KnowledgeIngestionJobMapper.class);
        KnowledgeAuditLogMapper audits = mock(KnowledgeAuditLogMapper.class);
        KnowledgeEvaluationCaseMapper evaluations = mock(KnowledgeEvaluationCaseMapper.class);
        KnowledgeEnterpriseRepositoryImpl repository = new KnowledgeEnterpriseRepositoryImpl(
                spaces, members, versions, reviews, jobs, audits, evaluations);

        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(404L);
        when(spaces.updateByQuery(any(), any())).thenReturn(0);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(KnowledgeSpaceDO.class)))
                    .thenReturn(new KnowledgeSpaceDO());
            assertThrows(IllegalStateException.class, () -> repository.updateSpace(TENANT, space));
        }

        when(spaces.deleteByQuery(any())).thenReturn(0);
        assertThrows(IllegalStateException.class, () -> repository.deleteSpace(TENANT, 404L));
    }

    private static Answer<Object> mapperAnswer(Class<?> dataType) {
        return invocation -> {
            String name = invocation.getMethod().getName();
            if (name.startsWith("selectCount")) return 1L;
            if (name.startsWith("selectList")) return List.of(newValue(dataType));
            if (name.startsWith("selectOne")) return newValue(dataType);
            if (name.equals("paginate")) {
                Page<Object> page = Page.of(1, 10, 1);
                page.setRecords(List.of(newValue(dataType)));
                return page;
            }
            if (name.startsWith("insert") || name.startsWith("update") || name.startsWith("delete")) return 1;
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        };
    }

    private static Object newValue(Class<?> type) throws Exception {
        Object value = type.getDeclaredConstructor().newInstance();
        try {
            Method setter = type.getMethod("setId", Long.class);
            setter.invoke(value, 1L);
        } catch (NoSuchMethodException ignored) {
            // Some persistence values intentionally have no generated id.
        }
        return value;
    }

    private static Object convertValue(Object source, Class<?> type) throws Exception {
        if (source == null) return null;
        return newValue(type);
    }

    private static List<?> convertList(List<?> source, Class<?> type) throws Exception {
        if (source == null) return List.of();
        List<Object> result = new ArrayList<>();
        for (Object ignored : source) result.add(newValue(type));
        return result;
    }
}
