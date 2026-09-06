package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class KnowledgeAuditServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(6), false);
    private final KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
    private final KnowledgeAuditServiceImpl service = new KnowledgeAuditServiceImpl(repository);

    @Test
    void recordsTenantAuditAndPagesMappedResults() {
        service.record(ACTOR, 4L, "SPACE", 4L, "CREATE", java.util.Map.of("ok", true));
        verify(repository).insertAudit(eq(ACTOR.tenantId()), any(KnowledgeAuditLogBO.class));
        KnowledgeAuditLogBO audit = new KnowledgeAuditLogBO(); audit.setId(1L); audit.setAction("CREATE");
        when(repository.pageAudit(ACTOR.tenantId(), 1, 10, 4L)).thenReturn(new PageData<>(List.of(audit), 1));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), any(Class.class))).thenReturn(List.of());
            assertEquals(1, service.page(ACTOR, 1, 10, 4L).getTotal());
        }
    }

    @Test
    void rejectsIncompleteActor() {
        assertThrows(ServiceException.class, () -> service.record(null, 1L, "SPACE", 1L, "READ", null));
        assertThrows(ServiceException.class, () -> service.page(null, 1, 10, null));
    }
}
