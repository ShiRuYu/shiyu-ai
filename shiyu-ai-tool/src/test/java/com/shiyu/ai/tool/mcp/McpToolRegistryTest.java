package com.shiyu.ai.tool.mcp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * McpToolRegistry 单元测试
 */
@Tag("dev")
class McpToolRegistryTest {

    private McpToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new McpToolRegistry();
    }

    private McpToolDescriptor tool(String name, String category, List<String> tags) {
        return new McpToolDescriptor(name, "desc-" + name, "server", Map.of(), tags, category, false);
    }

    @Test
    void testRegisterAndGet() {
        registry.register(tool("t1", "cat1", List.of("tag1")));
        assertNotNull(registry.getTool("t1"));
        assertEquals("desc-t1", registry.getTool("t1").getDescription());
    }

    @Test
    void testRegisterAll() {
        registry.registerAll(List.of(
                tool("a", "cat1", List.of()),
                tool("b", "cat2", List.of())
        ));
        assertEquals(2, registry.size());
    }

    @Test
    void testUnregister() {
        registry.register(tool("t1", "cat1", List.of()));
        registry.unregister("t1");
        assertNull(registry.getTool("t1"));
        assertEquals(0, registry.size());
    }

    @Test
    void testListTools() {
        registry.register(tool("a", "cat1", List.of()));
        registry.register(tool("b", "cat2", List.of()));
        assertEquals(2, registry.listTools().size());
    }

    @Test
    void testGetToolsByCategory() {
        registry.register(tool("a", "math", List.of()));
        registry.register(tool("b", "math", List.of()));
        registry.register(tool("c", "science", List.of()));

        List<McpToolDescriptor> mathTools = registry.getToolsByCategory("math");
        assertEquals(2, mathTools.size());
    }

    @Test
    void testGetToolsByTag() {
        registry.register(tool("a", "cat1", List.of("urgent")));
        registry.register(tool("b", "cat1", List.of("urgent", "important")));
        registry.register(tool("c", "cat1", List.of("normal")));

        List<McpToolDescriptor> urgentTools = registry.getToolsByTag("urgent");
        assertEquals(2, urgentTools.size());
    }

    @Test
    void testSearchTools() {
        registry.register(tool("weather-api", "utility", List.of("weather")));
        registry.register(tool("calculator", "math", List.of("calc")));

        List<McpToolDescriptor> results = registry.searchTools("weather");
        assertEquals(1, results.size());
        assertEquals("weather-api", results.get(0).getName());
    }

    @Test
    void testSearchToolsEmptyKeyword() {
        registry.register(tool("a", "cat", List.of()));
        registry.register(tool("b", "cat", List.of()));
        assertEquals(2, registry.searchTools("").size());
        assertEquals(2, registry.searchTools(null).size());
    }

    @Test
    void testGetCategories() {
        registry.register(tool("a", "math", List.of()));
        registry.register(tool("b", "science", List.of()));

        Set<String> categories = registry.getCategories();
        assertTrue(categories.contains("math"));
        assertTrue(categories.contains("science"));
    }

    @Test
    void testSize() {
        assertEquals(0, registry.size());
        registry.register(tool("a", "cat", List.of()));
        assertEquals(1, registry.size());
    }

    @Test
    void testUnregisterRemovesIndexes() {
        registry.register(tool("t1", "cat1", List.of("tag1")));
        registry.unregister("t1");

        assertTrue(registry.getToolsByCategory("cat1").isEmpty());
        assertTrue(registry.getToolsByTag("tag1").isEmpty());
    }
}
