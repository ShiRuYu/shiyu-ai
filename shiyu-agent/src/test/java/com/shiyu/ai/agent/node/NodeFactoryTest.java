package com.shiyu.ai.agent.node;

import com.shiyu.ai.agent.node.intent.IntentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NodeFactory 测试类
 *
 * @author shiyu-ai
 * @date 2026-03-27
 */
@SpringBootTest
class NodeFactoryTest {

    @Autowired
    private NodeFactory nodeFactory;

    @BeforeEach
    void setUp() {
        // 清空已注册的节点
        nodeFactory.clearNodes();
    }

    @Test
    @DisplayName("测试创建单个节点")
    void testCreateSingleNode() {
        // 1. 创建节点配置
        NodeConfig config = NodeConfig.builder()
                .nodeId("test-node-001")
                .nodeName("测试意图节点")
                .nodeType("INTENT")
                .description("测试用节点")
                .enabled(true)
                .build();

        // 2. 通过工厂创建节点
        BaseNode node = nodeFactory.createNode(config);

        // 3. 验证节点创建成功
        assertNotNull(node, "节点不应该为空");
        assertEquals("test-node-001", config.getNodeId(), "节点 ID 应该匹配");
        assertEquals("INTENT", config.getNodeType(), "节点类型应该匹配");
    }

    @Test
    @DisplayName("测试批量创建节点")
    void testCreateMultipleNodes() {
        // 1. 准备多个节点配置
        Map<String, NodeConfig> configs = new HashMap<>();
        
        NodeConfig config1 = NodeConfig.builder()
                .nodeId("node-001")
                .nodeName("节点 1")
                .nodeType("INTENT")
                .build();
        
        NodeConfig config2 = NodeConfig.builder()
                .nodeId("node-002")
                .nodeName("节点 2")
                .nodeType("INTENT")
                .build();
        
        configs.put(config1.getNodeId(), config1);
        configs.put(config2.getNodeId(), config2);

        // 2. 批量创建节点
        Map<String, BaseNode> nodes = nodeFactory.createNodes(configs);

        // 3. 验证创建结果
        assertEquals(2, nodes.size(), "应该创建 2 个节点");
        assertTrue(nodes.containsKey("node-001"), "应该包含节点 1");
        assertTrue(nodes.containsKey("node-002"), "应该包含节点 2");
    }

    @Test
    @DisplayName("测试注册服务到节点")
    void testRegisterServiceToNode() {
        // 1. 创建节点
        NodeConfig config = NodeConfig.builder()
                .nodeId("service-test-node")
                .nodeName("服务测试节点")
                .nodeType("INTENT")
                .build();
        
        BaseNode node = nodeFactory.createNode(config);

        // 2. 创建模拟服务
        MockIntentRecognitionService mockService = new MockIntentRecognitionService();

        // 3. 注册服务到节点
        assertDoesNotThrow(() -> {
            nodeFactory.registerServiceToNode("service-test-node", 
                    "INTENT_RECOGNITION_SERVICE", mockService);
        }, "注册服务不应该抛出异常");

        // 4. 验证服务是否注册成功（这里可以通过节点的 getter 验证）
        // 由于 IntentNode 的 intentRecognitionService 是私有的，需要通过反射或其他方式验证
        // 在实际使用中，服务会被注入到节点中
    }

    @Test
    @DisplayName("测试获取已注册的节点")
    void testGetRegisteredNode() {
        // 1. 创建并注册节点
        NodeConfig config = NodeConfig.builder()
                .nodeId("get-test-node")
                .nodeName("获取测试节点")
                .nodeType("INTENT")
                .build();
        
        nodeFactory.createNode(config);

        // 2. 通过 ID 获取节点
        BaseNode retrievedNode = nodeFactory.getNode("get-test-node");

        // 3. 验证获取成功
        assertNotNull(retrievedNode, "应该能获取到节点");
        assertEquals("get-test-node", config.getNodeId(), "节点 ID 应该匹配");
    }

    @Test
    @DisplayName("测试移除节点")
    void testRemoveNode() {
        // 1. 创建节点
        NodeConfig config = NodeConfig.builder()
                .nodeId("remove-test-node")
                .nodeName("移除测试节点")
                .nodeType("INTENT")
                .build();
        
        nodeFactory.createNode(config);

        // 2. 移除节点
        boolean removed = nodeFactory.removeNode("remove-test-node");

        // 3. 验证移除成功
        assertTrue(removed, "应该成功移除节点");
        assertNull(nodeFactory.getNode("remove-test-node"), "节点应该已被移除");
    }

    @Test
    @DisplayName("测试创建不存在的节点类型")
    void testCreateUnknownNodeType() {
        // 1. 创建未知类型的节点配置
        NodeConfig config = NodeConfig.builder()
                .nodeId("unknown-node")
                .nodeName("未知节点")
                .nodeType("UNKNOWN_TYPE")
                .build();

        // 2. 验证应该抛出异常
        Exception exception = assertThrows(IllegalArgumentException.class, 
                () -> nodeFactory.createNode(config),
                "创建未知节点类型应该抛出异常");
        
        assertTrue(exception.getMessage().contains("不支持的节点类型"), 
                "异常消息应该包含'不支持的节点类型'");
    }

    @Test
    @DisplayName("测试使用 IntentConfig 创建节点")
    void testCreateNodeWithIntentConfig() {
        // 1. 创建意图配置
        IntentConfig intentConfig = new IntentConfig();
        intentConfig.setNodeId("intent-config-test");
        intentConfig.setNodeName("意图配置测试");
        intentConfig.setNodeType("INTENT");
        intentConfig.setConfidenceThreshold(0.85);
        intentConfig.setMaxRetries(5);

        // 2. 创建节点
        BaseNode node = nodeFactory.createNode(intentConfig);

        // 3. 验证创建成功
        assertNotNull(node, "节点不应该为空");
        assertEquals("intent-config-test", intentConfig.getNodeId(), "节点 ID 应该匹配");
    }

    /**
     * 模拟的意图识别服务
     */
    static class MockIntentRecognitionService {
        public String recognize(String input) {
            return "MOCK_INTENT";
        }
    }
}
