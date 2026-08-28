package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.record.domain.model.ProfileBO;
import com.shiyu.ai.record.port.repository.ProfileRepository;
import com.shiyu.ai.record.request.ProfileRequest;
import com.shiyu.ai.record.vo.ProfileVO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class ProfileServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(4), new UserId(9), false);
    private final ProfileRepository repository = mock(ProfileRepository.class);
    private final ProfileServiceImpl service = new ProfileServiceImpl(repository);

    @Test
    void supportsTenantScopedProfileLifecycle() {
        ProfileBO profile = new ProfileBO(); ProfileRequest request = new ProfileRequest(); request.setName("student"); request.setAvatar("a"); ProfileVO view = mock(ProfileVO.class);
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, "owner")).thenReturn(Pair.of(1L, List.of(profile)));
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(profile).thenReturn(null);
        when(repository.insert(eq(ACTOR.tenantId()), any(ProfileBO.class))).thenReturn(profile);
        when(repository.update(ACTOR.tenantId(), profile)).thenReturn(true); when(repository.deleteById(ACTOR.tenantId(), 1L)).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(ProfileVO.class))).thenReturn(List.of(view));
            mapper.when(() -> MapstructUtils.convert(any(ProfileBO.class), eq(ProfileVO.class))).thenReturn(view);
            assertEquals(1, service.pageView(ACTOR, 0, 0, "owner").getRight().size()); assertSame(view, service.create(ACTOR, request));
        }
        assertTrue(service.update(ACTOR, 1L, request)); assertFalse(service.update(ACTOR, 1L, request)); assertTrue(service.delete(ACTOR, 1L));
    }

    @Test
    void coversExplicitPaginationAndBlankOwnerFilter() {
        when(repository.selectPage(ACTOR.tenantId(), 2, 3, null)).thenReturn(Pair.of(0L, List.of()));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(ProfileVO.class))).thenReturn(List.of());
            assertEquals(0L, service.pageView(ACTOR, 2, 3, null).getLeft());
        }
    }
}
