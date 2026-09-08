package com.shiyu.ai.agent.web;

import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.service.IntentDefService;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IntentDefControllerCoverageTest {
    @Test
    void coversIntentCrudAndMissingDetail() {
        var service = mock(IntentDefService.class);
        var controller = new IntentDefController(service);
        var actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        var request = new IntentDefRequest();
        when(service.pageView(actor, 1, 10, "a", "n", "c", "cat")).thenReturn(Pair.of(1L, List.of(new IntentDefVO())));
        when(service.detailView(actor, 1L)).thenReturn(new IntentDefVO()).thenReturn(null);
        when(service.create(actor, request)).thenReturn(new IntentDefVO());
        when(service.update(actor, 1L, request)).thenReturn(new IntentDefVO());
        when(service.listAllOptions(actor)).thenReturn(List.of());
        try (var mocked = mockStatic(ActorContextHttpAdapter.class)) {
            mocked.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.page("a", "n", "c", "cat", 1, 10).isSuccess());
            assertTrue(controller.detail(1L).isSuccess());
            assertFalse(controller.detail(1L).isSuccess());
            assertTrue(controller.create(request).isSuccess());
            assertTrue(controller.update(1L, request).isSuccess());
            assertTrue(controller.delete(1L).isSuccess());
            assertTrue(controller.deleteBatch(List.of(1L)).isSuccess());
            assertTrue(controller.options().isSuccess());
        }
        verify(service).deleteById(actor, 1L);
        verify(service).deleteByIds(actor, List.of(1L));
    }
}
