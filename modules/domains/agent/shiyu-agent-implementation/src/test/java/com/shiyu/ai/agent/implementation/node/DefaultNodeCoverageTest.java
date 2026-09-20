package com.shiyu.ai.agent.implementation.node;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.agent.contract.node.*;

import org.junit.jupiter.api.Test;

/**
 * 验证 Default Node Coverage 相关功能、边界条件、异常路径和协作行为。
 */
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
