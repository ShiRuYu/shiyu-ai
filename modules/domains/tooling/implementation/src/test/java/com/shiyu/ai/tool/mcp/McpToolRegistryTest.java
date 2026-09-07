package com.shiyu.ai.tool.mcp;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpToolRegistryTest {

    @Test
    void indexesSearchesAndUnregistersTools() {
        McpToolRegistry registry = new McpToolRegistry();
        McpToolDescriptor search = tool("web.search", "Search the web", "search", List.of("web", "read"), "network");
        McpToolDescriptor write = tool("web.write", "Write a page", "write", List.of("web", "write"), "network");
        registry.registerAll(List.of(search, write));

        assertEquals(2, registry.size());
        assertEquals(1, registry.getToolsByTag("read").size());
        assertEquals(2, registry.getToolsByCategory("network").size());
        assertEquals(search, registry.getTool("web.search"));
        assertEquals(1, registry.searchTools("SEARCH").size());
        assertEquals(1, registry.searchTools("write").size());
        assertEquals(2, registry.searchTools(null).size());
        assertTrue(registry.getCategories().contains("network"));

        registry.unregister("web.search");
        assertEquals(1, registry.size());
        assertTrue(registry.getToolsByTag("read").isEmpty());
        registry.unregister("missing");
        assertEquals(1, registry.size());
    }

    private static McpToolDescriptor tool(String name, String description, String server,
                                           List<String> tags, String category) {
        return new McpToolDescriptor(name, description, server, Map.of(), tags, category, false);
    }
}
