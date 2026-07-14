package com.shiyu.ai.tool;

import com.shiyu.ai.tool.ToolService;
import com.shiyu.ai.tool.mcp.McpToolDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ToolServiceImpl 单元测试
 */
@Tag("dev")
class ToolServiceImplTest {

    private ToolServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ToolServiceImpl();
        service.init();
    }

    /** 从富化结果中提取内部 result 映射 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractResult(Object enriched) {
        return (Map<String, Object>) ((Map<String, Object>) enriched).get("result");
    }

    @Test
    void testInitRegistersBuiltinTools() {
        List<ToolServiceImpl.ToolDefinition> tools = service.listTools();
        assertTrue(tools.size() >= 5);
    }

    @Test
    void testExecuteWeather() {
        ToolService.ToolExecutionResult result = service.execute("WEATHER", Map.of("location", "北京"));
        assertTrue(result.success());
        assertNotNull(result.result());
    }

    @Test
    void testExecuteCalculator() {
        ToolService.ToolExecutionResult result = service.execute("CALCULATOR", Map.of("expression", "1+2*3"));
        assertTrue(result.success());
        Map<String, Object> data = extractResult(result.result());
        assertEquals(7.0, (double) data.get("result"), 0.001);
    }

    @Test
    void testExecuteCalculatorWithBrackets() {
        ToolService.ToolExecutionResult result = service.execute("CALCULATOR", Map.of("expression", "(1+2)*3"));
        assertTrue(result.success());
        Map<String, Object> data = extractResult(result.result());
        assertEquals(9.0, (double) data.get("result"), 0.001);
    }

    @Test
    void testExecuteDatetime() {
        ToolService.ToolExecutionResult result = service.execute("DATETIME", Map.of());
        assertTrue(result.success());
    }

    @Test
    void testExecuteRandom() {
        ToolService.ToolExecutionResult result = service.execute("RANDOM", Map.of("min", 1, "max", 10));
        assertTrue(result.success());
    }

    @Test
    void testExecuteTextStats() {
        ToolService.ToolExecutionResult result = service.execute("TEXT_STATS", Map.of("text", "Hello World\n第二行"));
        assertTrue(result.success());
        Map<String, Object> data = extractResult(result.result());
        assertTrue(((Number) data.get("char_count")).intValue() > 0);
    }

    @Test
    void testExecuteUnknownTool() {
        ToolService.ToolExecutionResult result = service.execute("UNKNOWN_TOOL", Map.of());
        assertFalse(result.success());
        assertNotNull(result.errorMessage());
    }

    @Test
    void testExecuteEmptyToolName() {
        ToolService.ToolExecutionResult result = service.execute("", Map.of());
        assertFalse(result.success());
    }

    @Test
    void testExecuteNullToolName() {
        ToolService.ToolExecutionResult result = service.execute(null, Map.of());
        assertFalse(result.success());
    }

    @Test
    void testExecuteMissingRequiredParam() {
        ToolService.ToolExecutionResult result = service.execute("WEATHER", Map.of());
        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("缺少必填参数"));
    }

    @Test
    void testRegisterAndExecuteCustomTool() {
        service.registerTool(
                "echo",
                "回显输入",
                Map.of("msg", new McpToolDescriptor.ParameterInfo("string", "消息", true, null)),
                params -> Map.of("echo", params.get("msg"))
        );

        ToolService.ToolExecutionResult result = service.execute("echo", Map.of("msg", "hello"));
        assertTrue(result.success());
        Map<String, Object> data = extractResult(result.result());
        assertEquals("hello", data.get("echo"));
    }

    @Test
    void testUnregisterTool() {
        service.unregisterTool("WEATHER");
        assertNull(service.getToolDefinition("WEATHER"));
    }

    @Test
    void testListToolDescriptors() {
        List<McpToolDescriptor> descriptors = service.listToolDescriptors();
        assertTrue(descriptors.size() >= 5);
        assertTrue(descriptors.stream().allMatch(d -> d.getName() != null));
    }

    @Test
    void testGetToolDescriptor() {
        McpToolDescriptor desc = service.getToolDescriptor("WEATHER");
        assertNotNull(desc);
        assertTrue(desc.isBuiltin());
    }

    @Test
    void testCalculatorDivisionByZero() {
        ToolService.ToolExecutionResult result = service.execute("CALCULATOR", Map.of("expression", "1/0"));
        assertTrue(result.success()); // 返回错误信息的 Map，执行未抛异常
        Map<String, Object> data = extractResult(result.result());
        assertTrue(data.containsKey("error"));
    }
}
