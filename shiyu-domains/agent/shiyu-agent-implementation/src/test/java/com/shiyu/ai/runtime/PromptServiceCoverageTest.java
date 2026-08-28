package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptServiceCoverageTest {
    @Test
    void promptCommandsRequireTypedTenantIdentity() {
        PromptService service = new PromptService();
        assertEquals(List.of(), service.list(new TenantId(1), 2L));
        assertThrows(NullPointerException.class, () -> service.list(null, 2L));
    }

    @Test
    void extractsVariablesPreviewsMissingValuesAndPublishesScopedTemplates() {
        PromptService service = new PromptService();
        PromptTemplate draft = service.create(new TenantId(1L), 2L, "Greeting",
                "Hello {{ name }} / {{name}} {{user.id}}", null);
        assertEquals(List.of("name", "user.id"), draft.variables());
        assertEquals(1, service.list(new TenantId(1L), 2L).size());
        assertEquals(0, service.list(new TenantId(1L), 3L).size());
        assertEquals("Hello Ada/ Ada", service.preview(draft.template(), Map.of("name", "Ada")).content());
        assertEquals(List.of(), service.preview(null, null).variables());

        assertEquals("PUBLISHED", service.publish(draft.id(), new TenantId(1L), 2L).status());
        assertThrows(IllegalArgumentException.class, () -> service.publish(draft.id(), new TenantId(9L), 2L));
        PromptTemplate explicit = service.create(new TenantId(1L), 2L, "Explicit", "{{x}}", List.of("custom"));
        assertEquals(List.of("custom"), explicit.variables());
    }
}
