package com.shiyu.ai.education.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.mybatisflex.core.paginate.Page;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptDO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptQuestionDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamAttemptMapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamAttemptQuestionMapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamQuestionMapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamSectionMapper;
import com.shiyu.ai.kernel.context.TenantId;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

/** 验证逾期处理任务能够发现各租户的逾期作答记录。 */
class ExamTakingRepositoryImplTest {
    @Test
    void pagesStudentHistoryInSqlAndFiltersDeletedOrCrossTenantExams() {
        ExamAttemptMapper attempts = mock(ExamAttemptMapper.class);
        ExamAttemptDO row = new ExamAttemptDO();
        row.setId(91L);
        row.setTenantId(9L);
        row.setExamId(5L);
        row.setStudentId(4L);
        row.setStatus("SUBMITTED");
        Page<ExamAttemptDO> mapperPage = new Page<>(List.of(row), 2, 10, 17);
        when(attempts.paginate(org.mockito.ArgumentMatchers.eq(2),
                org.mockito.ArgumentMatchers.eq(10), any(QueryWrapper.class)))
                .thenReturn(mapperPage);
        var repository = new ExamTakingRepositoryImpl(
                mock(ExamSectionMapper.class), mock(ExamQuestionMapper.class), attempts,
                mock(ExamAttemptQuestionMapper.class));

        var page = repository.findHistoryPageByStudent(new TenantId(9L), 4L, 2, 10);

        assertEquals(17L, page.getTotal());
        assertEquals(List.of(91L), page.getItems().stream()
                .map(ExamTakingRepository.Attempt::id).toList());
        ArgumentCaptor<QueryWrapper> queries = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(attempts).paginate(org.mockito.ArgumentMatchers.eq(2),
                org.mockito.ArgumentMatchers.eq(10), queries.capture());
        String sql = queries.getValue().toSQL().toLowerCase();
        assertTrue(sql.contains("inner join"));
        assertTrue(sql.contains("edu_exam"));
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("student_id"));
        assertTrue(sql.contains("del_flag"));
        assertTrue(sql.contains("started_at"));
    }

    @Test
    void findsExpiredStudentAttemptsInTenantScopedCursorBatches() {
        ExamAttemptMapper attempts = mock(ExamAttemptMapper.class);
        ExamAttemptDO row = new ExamAttemptDO();
        row.setId(91L);
        row.setTenantId(9L);
        row.setExamId(5L);
        row.setStudentId(4L);
        row.setStatus("STARTED");
        LocalDateTime now = LocalDateTime.now();
        row.setDeadlineAt(now.minusMinutes(1));
        when(attempts.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(row));
        var repository = new ExamTakingRepositoryImpl(
                mock(ExamSectionMapper.class), mock(ExamQuestionMapper.class), attempts,
                mock(ExamAttemptQuestionMapper.class));

        var expired = repository.findExpiredAttemptsByStudent(
                new TenantId(9L), 4L, now, 90L, 150);

        assertEquals(List.of(91L), expired.stream()
                .map(ExamTakingRepository.Attempt::id).toList());
        ArgumentCaptor<QueryWrapper> queries = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(attempts).selectListByQuery(queries.capture());
        String sql = queries.getValue().toSQL().toLowerCase();
        assertTrue(sql.contains("tenant_id"));
        assertTrue(sql.contains("student_id"));
        assertTrue(sql.contains("status"));
        assertTrue(sql.contains("deadline_at"));
        assertTrue(sql.contains(" > 90"), sql);
        assertTrue(sql.contains("limit 100"));
    }

    @Test
    void findsDueAttemptsAcrossTenantsWithoutLeavingFilteringDisabled() {
        ExamAttemptMapper attempts = mock(ExamAttemptMapper.class);
        ExamAttemptDO row = new ExamAttemptDO();
        row.setId(91L);
        row.setTenantId(9L);
        row.setStatus("STARTED");
        row.setDeadlineAt(LocalDateTime.now().minusMinutes(1));
        when(attempts.selectListByQuery(any(QueryWrapper.class))).thenAnswer(invocation -> {
            assertTrue(TenantManager.isIgnoreTenantCondition());
            return List.of(row);
        });
        var repository = new ExamTakingRepositoryImpl(
                mock(ExamSectionMapper.class), mock(ExamQuestionMapper.class), attempts,
                mock(ExamAttemptQuestionMapper.class));

        var due = repository.findDueAttempts(LocalDateTime.now(), null, 100);
        repository.findDueAttempts(LocalDateTime.now(), 90L, 100);

        assertEquals(List.of(new ExamTakingRepository.DueAttempt(new TenantId(9L), 91L)), due);
        ArgumentCaptor<QueryWrapper> queries = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(attempts, times(2)).selectListByQuery(queries.capture());
        assertTrue(queries.getAllValues().get(1).toSQL().toLowerCase().contains("id >"));
        assertFalse(TenantManager.isIgnoreTenantCondition());
    }

    @Test
    void attemptSnapshotAndAnswerWritesCarryTheCurrentTenant() {
        ExamAttemptMapper attempts = mock(ExamAttemptMapper.class);
        ExamAttemptQuestionMapper snapshots = mock(ExamAttemptQuestionMapper.class);
        when(attempts.insert(any(ExamAttemptDO.class))).thenAnswer(invocation -> {
            ExamAttemptDO row = invocation.getArgument(0);
            row.setId(91L);
            return 1;
        });
        when(snapshots.insert(any(ExamAttemptQuestionDO.class))).thenReturn(1);
        when(snapshots.updateByQuery(any(ExamAttemptQuestionDO.class),
                any(QueryWrapper.class))).thenReturn(1);
        var repository = new ExamTakingRepositoryImpl(
                mock(ExamSectionMapper.class), mock(ExamQuestionMapper.class), attempts,
                snapshots);
        TenantId tenant = new TenantId(9L);
        LocalDateTime now = LocalDateTime.now();

        var inserted = repository.insertAttempt(tenant, new ExamTakingRepository.Attempt(
                null, 5L, 4L, "STARTED", now, now.plusMinutes(30),
                null, null, BigDecimal.TEN));
        repository.insertAttemptQuestion(tenant, new ExamTakingRepository.AttemptQuestion(
                null, 91L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                "[\"A\",\"B\"]", "B", "答案解析", BigDecimal.TEN, null, null));
        repository.saveDraftAnswer(tenant, 101L, "A");
        repository.saveAnswer(tenant, 101L, "B", BigDecimal.TEN);

        assertEquals(91L, inserted.id());
        assertEquals(5L, inserted.examId());
        ArgumentCaptor<ExamAttemptDO> attemptRow = ArgumentCaptor.forClass(ExamAttemptDO.class);
        verify(attempts).insert(attemptRow.capture());
        assertEquals(9L, attemptRow.getValue().getTenantId());
        assertEquals(4L, attemptRow.getValue().getStudentId());
        ArgumentCaptor<ExamAttemptQuestionDO> questionRows =
                ArgumentCaptor.forClass(ExamAttemptQuestionDO.class);
        verify(snapshots).insert(questionRows.capture());
        assertEquals(9L, questionRows.getValue().getTenantId());
        assertEquals("B", questionRows.getValue().getCorrectAnswer());
        ArgumentCaptor<QueryWrapper> answerScopes = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(snapshots, times(2)).updateByQuery(any(ExamAttemptQuestionDO.class),
                answerScopes.capture());
        for (QueryWrapper scope : answerScopes.getAllValues()) {
            String sql = scope.toSQL().toLowerCase();
            assertTrue(sql.contains("tenant_id"));
            assertTrue(sql.contains("id"));
        }
    }

    @Test
    void attemptClaimsAndFinalizationRequirePriorStateInTenantScopedSql() {
        ExamAttemptMapper attempts = mock(ExamAttemptMapper.class);
        when(attempts.updateByQuery(any(ExamAttemptDO.class), any(QueryWrapper.class)))
                .thenReturn(1);
        var repository = new ExamTakingRepositoryImpl(
                mock(ExamSectionMapper.class), mock(ExamQuestionMapper.class), attempts,
                mock(ExamAttemptQuestionMapper.class));
        TenantId tenant = new TenantId(9L);
        LocalDateTime now = LocalDateTime.now();

        assertEquals(1, repository.claimAttempt(tenant, 91L, "FINALIZING", now, true));
        assertEquals(1, repository.claimAttempt(tenant, 92L, "FINALIZING", now, false));
        assertEquals(1, repository.finishAttempt(tenant, 91L, BigDecimal.TEN,
                now, "SUBMITTED"));

        ArgumentCaptor<QueryWrapper> scopes = ArgumentCaptor.forClass(QueryWrapper.class);
        ArgumentCaptor<ExamAttemptDO> updates = ArgumentCaptor.forClass(ExamAttemptDO.class);
        verify(attempts, times(3)).updateByQuery(updates.capture(), scopes.capture());
        String before = scopes.getAllValues().get(0).toSQL().toLowerCase();
        String expired = scopes.getAllValues().get(1).toSQL().toLowerCase();
        String finished = scopes.getAllValues().get(2).toSQL().toLowerCase();
        assertTrue(before.contains("tenant_id") && before.contains("status")
                && before.contains("deadline_at >"));
        assertTrue(expired.contains("tenant_id") && expired.contains("status")
                && expired.contains("deadline_at <="));
        assertTrue(finished.contains("tenant_id") && finished.contains("status"));
        assertEquals("FINALIZING", updates.getAllValues().get(0).getStatus());
        assertEquals("SUBMITTED", updates.getAllValues().get(2).getStatus());
        assertEquals(BigDecimal.TEN, updates.getAllValues().get(2).getScore());
    }
}
