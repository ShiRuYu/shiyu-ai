package com.shiyu.ai.education.implementation.web.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.StudentService;
import com.shiyu.ai.education.implementation.web.dto.StudentResponse;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** 验证学生自助服务正确识别当前已认证用户。 */
class CurrentStudentControllerTest {

    @Test
    void resolvesCurrentStudentFromAuthenticatedActor() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(37), false);
        StudentService service = mock(StudentService.class);
        StudentResponse student = mock(StudentResponse.class);
        when(service.getByUserId(actor, 37L)).thenReturn(student);

        try (MockedStatic<ActorContextHttpAdapter> context =
                mockStatic(ActorContextHttpAdapter.class)) {
            context.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertSame(student, new StudentController(service).getMe().getData());
        }

        verify(service).getByUserId(actor, 37L);
    }
}
