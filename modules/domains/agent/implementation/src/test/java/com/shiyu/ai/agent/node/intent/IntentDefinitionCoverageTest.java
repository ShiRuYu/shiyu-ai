package com.shiyu.ai.agent.node.intent;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntentDefinitionCoverageTest {
    @Test
    void buildsDefinitionsAndNormalizesMutableCollections() {
        IntentDefinition definition = IntentDefinition.builder().code("WEATHER").build();
        assertEquals("WEATHER", definition.getCode());
        definition.setExamples(null);
        definition.addExample("today").addExample("tomorrow");
        definition.setSlots(Map.of("city", "location"));
        definition.addSlot("date", "date");
        definition.setParameterMapping(Map.of("city", "location"));
        definition.addParameterMapping("date", "queryDate");
        definition.setSlotDefaults(Map.of("unit", "celsius"));
        definition.addSlotDefault("lang", "zh");
        definition.setParameters(Map.of("source", "test"));
        definition.addParameter("limit", 5);
        definition.buildWithDefaults();
        assertEquals(0.75, definition.getConfidenceThreshold());
        definition.buildWithDefaults();
        assertEquals(IntentType.WEATHER.getCode(), IntentDefinition.fromIntentType(IntentType.WEATHER).getCode());
    }

    @Test
    void resolvesIntentTypesAndFactoryRoutingWithFallbacks() {
        assertEquals(IntentType.UNKNOWN, IntentType.fromCode(null));
        assertEquals(IntentType.UNKNOWN, IntentType.fromCode(" "));
        assertEquals(IntentType.WEATHER, IntentType.fromCode("WEATHER"));
        assertEquals(IntentType.UNKNOWN, IntentType.fromCode("missing"));
        assertEquals(IntentType.QUESTION, IntentType.fromName("问答"));
        assertEquals(IntentType.UNKNOWN, IntentType.fromName("missing"));

        IntentDefBO fallback = definition("fallback", null, null, "fallback-node");
        IntentDefBO custom = definition("custom", "agent-1", "TASK", "task-node");
        IntentDefBO disabled = definition("disabled", "agent-1", "TASK", "");
        IntentDefinitionFactory.reloadFromDb(List.of(fallback, custom, disabled));
        assertEquals(2, IntentDefinitionFactory.getByCategory("agent-1", "TASK").size());
        assertEquals(2, IntentDefinitionFactory.getByCategory("TASK").size());
        assertEquals(2, IntentDefinitionFactory.getAll("agent-1").size());
        assertEquals(1, IntentDefinitionFactory.getAll("unknown-agent").size());
        assertNotNull(IntentDefinitionFactory.getFirst("agent-1", "TASK"));
        assertTrue(IntentDefinitionFactory.getAgentIds().contains("default"));
        assertTrue(IntentDefinitionFactory.getCategories().contains("TASK"));
        var predicates = IntentDefinitionFactory.buildRoutingPredicates("agent-1", "TASK");
        assertEquals(1, predicates.size());
        assertTrue(predicates.keySet().iterator().next().test(Map.of("intentCode", "custom")));
        IntentDefinitionFactory.register("agent-1", "TASK", IntentDefinition.builder().code("other").targetNode("node").build());
        assertEquals(3, IntentDefinitionFactory.getByCategory("agent-1", "TASK").size());
        IntentDefinitionFactory.reloadFromDb(null);
        assertTrue(IntentDefinitionFactory.getAll("default").isEmpty());
    }

    private static IntentDefBO definition(String code, String agentId, String category, String targetNode) {
        IntentDefBO value = new IntentDefBO();
        value.setCode(code); value.setAgentId(agentId); value.setCategory(category);
        value.setTargetNode(targetNode); value.setExamples(List.of("example"));
        value.setEnabled(true); value.setRequireSlotFilling(false); value.setPriority(50);
        return value;
    }
}
