package com.shiyu.ai.tooling.implementation.tool;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.tooling.implementation.tool.mcp.McpToolDescriptor.ParameterInfo;
import com.shiyu.ai.tooling.contract.api.ToolService.ToolExecutionContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 工具 Service Impl 相关功能、边界条件、异常路径和协作行为。
 */
class ToolServiceImplTest {
    private ToolServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ToolServiceImpl();
        service.init();
    }

    @Test
    void customAndUnknownToolsRequireApprovalEvenWhenGraphCallsThemNormal() {
        service.registerTool("external.write", "external action", Map.of(), args -> "done");

        assertFalse(service.requiresApproval("CALCULATOR"));
        assertTrue(service.requiresApproval("external.write"));
        assertTrue(service.requiresApproval("missing-tool"));
    }

    @Test
    void forwardsOperationIdToContextAwareToolExecutors() {
        AtomicReference<ToolExecutionContext> received = new AtomicReference<>();
        service.registerToolWithContext("REMOTE_WRITE", "remote write", Map.of(),
                (parameters, context) -> {
                    received.set(context);
                    return "done";
                });

        var result = service.execute("REMOTE_WRITE", Map.of(),
                new ToolExecutionContext("approval-42"));

        assertTrue(result.success());
        assertEquals("approval-42", received.get().operationId());
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
        var invalidWeather = service.execute("WEATHER", Map.of("location", 123));
        assertFalse(invalidWeather.success());
        assertTrue(invalidWeather.errorMessage().contains("类型"));
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
        assertEquals(
                "Asia/Shanghai",
                ((Map<?, ?>) ((Map<?, ?>) date.result()).get("result")).get("timezone_id"));
        assertTrue(service.execute("DATETIME", Map.of()).success());
    }

    @Test
    void evaluatesArithmeticIncludingErrors() {
        assertEquals(
                7.0,
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "1+2*3"))
                                                        .result())
                                        .get("result"))
                        .get("result"));
        assertEquals(
                5.0,
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "-(2-7)"))
                                                        .result())
                                        .get("result"))
                        .get("result"));
        assertEquals(
                3.0,
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "(8+4)/4"))
                                                        .result())
                                        .get("result"))
                        .get("result"));
        assertTrue(
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "1/0"))
                                                        .result())
                                        .get("result"))
                        .containsKey("error"));
        Map<?, ?> invalidExpression =
                (Map<?, ?>)
                        ((Map<?, ?>)
                                        service.execute("CALCULATOR", Map.of("expression", "1+a"))
                                                .result())
                                .get("result");
        assertEquals("计算失败，请检查表达式后重试", invalidExpression.get("error"));
        assertFalse(String.valueOf(invalidExpression.get("error")).contains("非法字符"));
        assertTrue(
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "(1+2"))
                                                        .result())
                                        .get("result"))
                        .containsKey("error"));
        assertTrue(
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "1)"))
                                                        .result())
                                        .get("result"))
                        .containsKey("error"));
        assertTrue(
                ((Map<?, ?>)
                                ((Map<?, ?>)
                                                service.execute(
                                                                "CALCULATOR",
                                                                Map.of("expression", "."))
                                                        .result())
                                        .get("result"))
                        .containsKey("error"));
    }

    @Test
    void appliesOptionalDefaultsAndNormalizesRandomBounds() {
        var random = service.execute("RANDOM", Map.of("min", 4, "max", 4));
        assertTrue(random.success());
        assertEquals(4, ((Map<?, ?>) ((Map<?, ?>) random.result()).get("result")).get("value"));
        var invalidRandom = service.execute("RANDOM", Map.of("min", "4"));
        assertFalse(invalidRandom.success());
        assertTrue(invalidRandom.errorMessage().contains("类型"));
        var reversed = service.execute("RANDOM", Map.of("min", 9, "max", 3));
        assertTrue(reversed.success());
        Map<?, ?> result = (Map<?, ?>) ((Map<?, ?>) reversed.result()).get("result");
        assertEquals(3, result.get("min"));
        assertEquals(9, result.get("max"));
        assertTrue(service.execute("RANDOM", Map.of()).success());
    }

    @Test
    void supportsCustomRegistrationValidationAndExecutorFailures() throws Exception {
        service.registerTool(
                "CUSTOM",
                "custom",
                Map.of("value", new ParameterInfo("string", "value", true)),
                p -> p.get("value"));
        assertEquals("custom", service.getToolDefinition("CUSTOM").description());
        assertFalse(service.execute("CUSTOM", Map.of()).success());
        Map<String, Object> nullValue = new HashMap<>();
        nullValue.put("value", null);
        assertFalse(service.execute("CUSTOM", nullValue).success());
        assertEquals(
                "ok",
                ((Map<?, ?>) service.execute("CUSTOM", Map.of("value", "ok")).result())
                        .get("result"));

        service.registerTool("EMPTY", "empty", null, p -> "empty");
        assertNotNull(service.getToolDefinition("EMPTY"));
        assertTrue(service.execute("EMPTY", null).success());

        service.registerTool(
                "BROKEN",
                "broken",
                Map.of(),
                p -> {
                    throw new IllegalStateException("boom");
                });
        var failed = service.execute("BROKEN", Map.of());
        assertFalse(failed.success());
        assertEquals("工具执行失败，请稍后重试", failed.errorMessage());
        assertFalse(failed.errorMessage().contains("boom"));

        Field executors = ToolServiceImpl.class.getDeclaredField("executorRegistry");
        executors.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> executorRegistry = (Map<String, Object>) executors.get(service);
        executorRegistry.remove("EMPTY");
        var missingExecutor = service.execute("EMPTY", null);
        assertFalse(missingExecutor.success());
        assertTrue(missingExecutor.errorMessage().contains("未注册执行器"));

        assertNotNull(service.getToolDescriptor("CUSTOM"));
        assertTrue(service.listTools().stream().anyMatch(t -> t.name().equals("CUSTOM")));
        assertTrue(
                service.listToolDescriptors().stream().anyMatch(t -> t.getName().equals("CUSTOM")));
        service.unregisterTool("CUSTOM");
        assertNull(service.getToolDefinition("CUSTOM"));
    }

    @Test
    void validatesDeclaredParameterFamiliesBeforeCallingCustomExecutors() {
        Map<String, Object> accepted = Map.of(
                "boolean", true,
                "integer", 2L,
                "number", 2.5D,
                "object", Map.of("key", "value"),
                "array", List.of("value"),
                "any", new Object());
        for (var entry : accepted.entrySet()) {
            String tool = "TYPE_" + entry.getKey().toUpperCase();
            service.registerTool(tool, "typed", Map.of("value",
                    new ParameterInfo(entry.getKey(), "value", true)), p -> "called");
            assertTrue(service.execute(tool, Map.of("value", entry.getValue())).success(),
                    entry.getKey());
            if (!entry.getKey().equals("any")) {
                assertFalse(service.execute(tool, Map.of("value", new Object())).success(),
                        entry.getKey());
            }
        }
        assertTrue(service.execute("TYPE_ARRAY",
                Map.of("value", new String[] {"value"})).success());
        for (Object integer : List.of((byte) 1, (short) 1, 1,
                java.math.BigInteger.ONE)) {
            assertTrue(service.execute("TYPE_INTEGER", Map.of("value", integer)).success());
        }
        assertFalse(service.execute("TYPE_INTEGER", Map.of("value", 1.5D)).success());
        service.registerTool("TYPE_UNTYPED", "legacy untyped parameter",
                Map.of("value", new ParameterInfo(null, "value", true)), p -> "called");
        service.registerTool("TYPE_BLANK", "legacy blank parameter",
                Map.of("value", new ParameterInfo(" ", "value", true)), p -> "called");
        assertTrue(service.execute("TYPE_UNTYPED", Map.of("value", new Object())).success());
        assertTrue(service.execute("TYPE_BLANK", Map.of("value", new Object())).success());
    }
}
