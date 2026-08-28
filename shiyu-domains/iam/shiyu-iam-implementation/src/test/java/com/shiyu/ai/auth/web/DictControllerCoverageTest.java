package com.shiyu.ai.auth.web;

import com.shiyu.ai.auth.request.DictPageRequest;
import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.auth.service.DictService;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DictControllerCoverageTest {
    private final DictService service = mock(DictService.class);
    private final DictController controller = new DictController(service);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsDictionaryQueriesMutationsAndDeletes() {
        DictPageRequest page = new DictPageRequest();
        DictRequest request = new DictRequest();
        when(service.pageView(any(), any(), any())).thenReturn(Pair.of(0L, List.of()));
        when(service.byTypeView(any(), eq("theme"))).thenReturn(List.of());
        assertTrue(controller.getDictList(page).isSuccess());
        assertTrue(controller.getDictByType("theme").isSuccess());
        assertTrue(controller.createDict(request).isSuccess());
        assertTrue(controller.updateDict(1L, request).isSuccess());
        controller.deleteDict(1L);
        controller.deleteDicts(List.of(1L, 2L));
        verify(service).deleteByIds(any(), eq(List.of(1L, 2L)));
    }
}
