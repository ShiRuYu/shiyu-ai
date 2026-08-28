package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.model.StudentBO;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.port.repository.StudentRepository;
import com.shiyu.ai.education.request.StudentRequest;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final StudentRepository repository = mock(StudentRepository.class);
    private final StudentServiceImpl service = new StudentServiceImpl(repository);

    @Test
    void reliesOnTheTenantUserUniqueConstraintInsteadOfCheckThenInsert() {
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(StudentBO.class), eq(StudentResponse.class)))
                    .thenReturn(mock(StudentResponse.class));

            service.create(ACTOR, request());
        }

        verify(repository, never()).selectByUserId(any(), any());
        verify(repository).insert(eq(ACTOR.tenantId()), any(StudentBO.class));
    }

    @Test
    void duplicateStudentCreationFailsTheCommand() {
        doThrow(new DuplicateKeyException("tenant/user already exists"))
                .when(repository).insert(any(TenantId.class), any(StudentBO.class));

        assertThrows(DuplicateKeyException.class, () -> service.create(ACTOR, request()));
    }

    @Test
    void readsPagesAndUsesTheActorTenantForEveryLookup() {
        StudentBO student = new StudentBO();
        student.setId(12L);
        PageData<StudentBO> page = new PageData<>(java.util.List.of(student), 1L);
        when(repository.selectById(ACTOR.tenantId(), 12L)).thenReturn(student);
        when(repository.selectByUserId(ACTOR.tenantId(), 88L)).thenReturn(student);
        when(repository.selectPage(ACTOR.tenantId(), 2, 20)).thenReturn(page);

        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(student, StudentResponse.class))
                    .thenReturn(mock(StudentResponse.class));
            mapper.when(() -> MapstructUtils.convert(page.getItems(), StudentResponse.class))
                    .thenReturn(java.util.List.of(mock(StudentResponse.class)));

            service.getById(ACTOR, 12L);
            service.getByUserId(ACTOR, 88L);
            PageData<StudentResponse> result = service.page(ACTOR, 2, 20);
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getItems().size());
        }

        verify(repository).selectById(ACTOR.tenantId(), 12L);
        verify(repository).selectByUserId(ACTOR.tenantId(), 88L);
        verify(repository).selectPage(ACTOR.tenantId(), 2, 20);
    }

    @Test
    void updatesExistingStudentAndLeavesMissingStudentUntouched() {
        StudentBO existing = new StudentBO();
        existing.setId(12L);
        existing.setStatus(1);
        when(repository.selectById(ACTOR.tenantId(), 12L)).thenReturn(existing).thenReturn(null);
        StudentRequest request = request();
        request.setId(12L);
        request.setStatus(0);

        service.update(ACTOR, request);
        assertEquals("student", existing.getName());
        assertEquals(0, existing.getStatus());
        verify(repository).update(ACTOR.tenantId(), existing);

        service.update(ACTOR, request);
        verify(repository, org.mockito.Mockito.times(1)).update(ACTOR.tenantId(), existing);
    }

    @Test
    void deletesByActorTenant() {
        service.deleteById(ACTOR, 12L);
        verify(repository).deleteById(ACTOR.tenantId(), 12L);
    }

    private static StudentRequest request() {
        StudentRequest request = new StudentRequest();
        request.setUserId(88L);
        request.setName("student");
        request.setGrade(7);
        return request;
    }
}
