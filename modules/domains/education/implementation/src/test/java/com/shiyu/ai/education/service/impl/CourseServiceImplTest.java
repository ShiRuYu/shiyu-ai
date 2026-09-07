package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.CourseBO;
import com.shiyu.ai.education.dto.CourseResponse;
import com.shiyu.ai.education.port.repository.CourseRepository;
import com.shiyu.ai.education.request.CourseRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class CourseServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final CourseRepository repository = mock(CourseRepository.class);
    private final CourseServiceImpl service = new CourseServiceImpl(repository);

    @Test
    void readsCourseViewsAndProgressWithinTenant() {
        CourseBO course = new CourseBO();
        PageData<CourseBO> page = new PageData<>(List.of(course), 1L);
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(course);
        when(repository.selectBySubjectCode(ACTOR.tenantId(), "math")).thenReturn(List.of(course));
        when(repository.selectByGrade(ACTOR.tenantId(), 7)).thenReturn(List.of(course));
        when(repository.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(page);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            CourseResponse response = mock(CourseResponse.class);
            mapper.when(() -> MapstructUtils.convert(any(CourseBO.class), eq(CourseResponse.class))).thenReturn(response);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(CourseResponse.class))).thenReturn(List.of(response));
            assertEquals(response, service.getById(ACTOR, 1L));
            assertEquals(1, service.listBySubjectCode(ACTOR, "math").size());
            assertEquals(1, service.listByGrade(ACTOR, 7).size());
            assertEquals(1, service.page(ACTOR, 1, 10).getItems().size());
        }
        assertEquals(1L, service.getProgress(ACTOR, 1L, 2L).courseId());
    }

    @Test
    void createsUpdatesAndDeletesCoursesWithDefaults() {
        CourseRequest request = new CourseRequest();
        request.setName("Math");
        request.setSubjectCode("math");
        request.setGrade(7);
        when(repository.insert(eq(ACTOR.tenantId()), any(CourseBO.class))).thenReturn(1);
        CourseResponse response = mock(CourseResponse.class);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(CourseBO.class), eq(CourseResponse.class))).thenReturn(response);
            assertEquals(response, service.create(ACTOR, request));
        }
        CourseBO existing = new CourseBO();
        existing.setId(1L);
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(existing).thenReturn(null);
        request.setId(1L);
        service.update(ACTOR, request);
        assertEquals("Math", existing.getName());
        verify(repository).update(ACTOR.tenantId(), existing);
        service.update(ACTOR, request);
        service.deleteById(ACTOR, 1L);
        verify(repository).deleteById(ACTOR.tenantId(), 1L);
        assertThrows(NullPointerException.class, () -> service.getById(null, 1L));
    }
}
