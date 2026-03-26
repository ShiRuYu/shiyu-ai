package com.shiyu.ai.agent.node;

import com.shiyu.ai.agent.config.NodeConfig;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BaseNode 测试类
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class BaseNodeTest {
    
    /**
     * 测试用的具体节点实现
     */
    private static class TestNode extends BaseNode {
        @Override
        protected NodeOutput doExecute(NodeInput input) {
            NodeOutput output = new NodeOutput();
            output.addData("executed", true);
            output.addData("input", input.getParameter("input", null));
            return output;
        }
    }
    
    private TestNode testNode;
    
    @BeforeEach
    void setUp() {
        testNode = new TestNode();
        
        NodeConfig config = NodeConfig.builder()
                .nodeId("test_node")
                .nodeName("测试节点")
                .nodeType("TEST")
                .enabled(true)
                .timeout(5000L)
                .build();
        
        testNode.setConfig(config);
    }
    
    @Test
    void testApply_Success() {
        // 准备输入数据
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("input", "test_value");
        
        AgentState inputState = AgentState.create(inputData);
        
        // 执行
        AgentState result = testNode.apply(inputState);
        
        // 验证
        assertNotNull(result);
        assertTrue((Boolean) result.get("executed", false));
    }
    
    @Test
    void testBeforeExecute() {
        AgentState state = AgentState.create();
        
        AgentState result = testNode.beforeExecute(state);
        
        assertNotNull(result);
    }
    
    @Test
    void testProcessParameters() {
        AgentState state = AgentState.create(Map.of("test_param", "test_value"));
        
        NodeConfig config = NodeConfig.builder()
                .inputParams(Map.of("param1", "test_param"))
                .build();
        testNode.setConfig(config);
        
        NodeInput input = testNode.processParameters(state);
        
        assertEquals("test_value", input.getParameter("test_param", null));
    }
    
    @Test
    void testAfterExecute() {
        AgentState state = AgentState.create();
        NodeOutput output = NodeOutput.fromMap(Map.of("key1", "value1"));
        
        testNode.afterExecute(state, output);
        
        // 验证方法执行不抛出异常即可
        assertNotNull(state);
    }
    
    @Test
    void testHandleException_Throw() {
        NodeConfig config = NodeConfig.builder()
                .errorStrategy("THROW")
                .build();
        testNode.setConfig(config);
        
        Exception exception = new RuntimeException("Test exception");
        
        assertThrows(RuntimeException.class, () -> {
            testNode.handleException(AgentState.create(), exception);
        });
    }
    
    @Test
    void testHandleException_Ignore() {
        NodeConfig config = NodeConfig.builder()
                .errorStrategy("IGNORE")
                .build();
        testNode.setConfig(config);
        
        AgentState state = AgentState.create();
        Exception exception = new RuntimeException("Test exception");
        
        AgentState result = testNode.handleException(state, exception);
        
        assertNotNull(result);
    }
    
    @Test
    void testAsyncExecution() throws Exception {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("input", "async_test");
        
        AgentState inputState = AgentState.create(inputData);
        
        var future = testNode.applyAsync(inputState);
        
        AgentState result = future.get();
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("executed", false));
    }
}
