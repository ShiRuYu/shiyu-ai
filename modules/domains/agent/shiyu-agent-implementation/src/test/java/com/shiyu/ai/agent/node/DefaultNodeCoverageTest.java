package com.shiyu.ai.agent.node;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultNodeCoverageTest {
    @Test
    void buildsWithAndWithoutConfigAndExecutes() {
        var empty = DefaultNode.builder().build();
        assertEquals(NodeType.DEFAULT, empty.getConfig().getNodeType());
        assertTrue(empty.doExecute(new NodeInput()).isSuccess());
        var config = new NodeConfig();
        config.setNodeName("custom");
        var configured = DefaultNode.builder().config(config).build();
        assertSame(config, configured.getConfig());
        assertEquals(NodeType.DEFAULT, configured.getConfig().getNodeType());
        assertTrue(configured.getRequiredInputs().isEmpty());
        assertTrue(configured.doExecute(null).isSuccess());
    }
}
