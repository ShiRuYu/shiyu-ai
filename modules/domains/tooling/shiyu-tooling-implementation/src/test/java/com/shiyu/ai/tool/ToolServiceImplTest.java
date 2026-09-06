package com.shiyu.ai.tool;

import com.shiyu.ai.tool.mcp.McpToolDescriptor.ParameterInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToolServiceImplTest {
    private ToolServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ToolServiceImpl();
        service.init();
    }

    @Test
    void validatesNamesAndUnknownTools() {
        assertFalse(service.execute(null, Map.of()).success());
        assertFalse(service.execute("  ", Map.of()).success());
        var unknown = service.execute("MISSING", Map.of());
        assertFalse(unknown.success());
        assertTrue(unknown.errorMessage().contains("未知工具"));
    }

    @Test
    void executesBuiltinToolsAndReportsRequiredParameters() {
        var missingWeather = service.execute("WEATHER", Map.of());
        assertFalse(missingWeather.success());
        assertTrue(missingWeather.errorMessage().contains("location"));
        var weather = service.execute("WEATHER", Map.of("location", "上海"));
        assertTrue(weather.success());
        assertEquals("WEATHER", ((Map<?, ?>) weather.result()).get("tool"));

        var text = service.execute("TEXT_STATS", Map.of("text", "你好 world\n第二行"));
        assertTrue(text.success());
        Map<?, ?> textResult = (Map<?, ?>) ((Map<?, ?>) text.result()).get("result");
        assertEquals(2, textResult.get("line_count"));
        assertEquals(5, textResult.get("chinese_char_count"));

        var date = service.execute("DATETIME", Map.of("timezone", "Asia/Shanghai"));
        assertTrue(date.success());
        assertEquals("Asia/Shanghai", ((Map<?, ?>) ((Map<?, ?>) date.result()).get("result")).get("timezone_id"));
        assertTrue(service.execute("DATETIME", Map.of()).success());
    }

    @Test
    void evaluatesArithmeticIncludingErrors() {
        assertEquals(7.0, ((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "1+2*3")).result()).get("result")).get("result"));
        assertEquals(5.0, ((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "-(2-7)")).result()).get("result")).get("result"));
        assertEquals(3.0, ((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "(8+4)/4")).result()).get("result")).get("result"));
        assertTrue(((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "1/0")).result()).get("result")).containsKey("error"));
        assertTrue(((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "1+a")).result()).get("result")).containsKey("error"));
        assertTrue(((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "(1+2")).result()).get("result")).containsKey("error"));
        assertTrue(((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", "1)")).result()).get("result")).containsKey("error"));
        assertTrue(((Map<?, ?>) ((Map<?, ?>) service.execute("CALCULATOR", Map.of("expression", ".")).result()).get("result")).containsKey("error"));
    }

    @Test
    void appliesOptionalDefaultsAndNormalizesRandomBounds() {
        var random = service.execute("RANDOM", Map.of("min", 4, "max", 4));
        assertTrue(random.success());
        assertEquals(4, ((Map<?, ?>) ((Map<?, ?>) random.result()).get("result")).get("value"));
        var reversed = service.execute("RANDOM", Map.of("min", 9, "max", 3));
        assertTrue(reversed.success());
        Map<?, ?> result = (Map<?, ?>) ((Map<?, ?>) reversed.result()).get("result");
        assertEquals(3, result.get("min"));
        assertEquals(9, result.get("max"));
        assertTrue(service.execute("RANDOM", Map.of()).success());
    }

    @Test
    void supportsCustomRegistrationValidationAndExecutorFailures() throws Exception {
        service.registerTool("CUSTOM", "custom", Map.of("value", new ParameterInfo("string", "value", true)), p -> p.get("value"));
        assertEquals("custom", service.getToolDefinition("CUSTOM").description());
        assertFalse(service.execute("CUSTOM", Map.of()).success());
        Map<String, Object> nullValue = new HashMap<>();
        nullValue.put("value", null);
        assertFalse(service.execute("CUSTOM", nullValue).success());
        assertEquals("ok", ((Map<?, ?>) service.execute("CUSTOM", Map.of("value", "ok")).result()).get("result"));

        service.registerTool("EMPTY", "empty", null, p -> "empty");
        assertNotNull(service.getToolDefinition("EMPTY"));
        assertTrue(service.execute("EMPTY", null).success());

        service.registerTool("BROKEN", "broken", Map.of(), p -> { throw new IllegalStateException("boom"); });
        var failed = service.execute("BROKEN", Map.of());
        assertFalse(failed.success());
        assertTrue(failed.errorMessage().contains("boom"));

        Field executors = ToolServiceImpl.class.getDeclaredField("executorRegistry");
        executors.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, Object> executorRegistry = (Map<String, Object>) executors.get(service);
        executorRegistry.remove("EMPTY");
        var missingExecutor = service.execute("EMPTY", null);
        assertFalse(missingExecutor.success());
        assertTrue(missingExecutor.errorMessage().contains("未注册执行器"));

        assertNotNull(service.getToolDescriptor("CUSTOM"));
        assertTrue(service.listTools().stream().anyMatch(t -> t.name().equals("CUSTOM")));
        assertTrue(service.listToolDescriptors().stream().anyMatch(t -> t.getName().equals("CUSTOM")));
        service.unregisterTool("CUSTOM");
        assertNull(service.getToolDefinition("CUSTOM"));
    }
}
