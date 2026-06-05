package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.langgraph4j.node.intent.IntentType;
import com.shiyu.ai.agent.biz.agent.service.ToolService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * In-memory 模拟工具调用服务实现
 * <p>
 * 在内存中维护工具定义注册表和执行器映射，支持动态注册/注销工具。
 * 内置一组示例工具（天气查询、计算器、时间日期、随机数、文本统计），
 * 模拟 MCP/API 调用的行为模式。
 * 适合开发/测试环境快速验证 Tool Calling 流程。
 */
@Slf4j
@Service
public class ToolServiceImpl implements ToolService {

    /** 工具名 -> 工具定义 */
    private final Map<String, ToolDefinition> toolRegistry = new ConcurrentHashMap<>();

    /** 工具名 -> 执行器 */
    private final Map<String, Function<Map<String, Object>, Object>> executorRegistry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("初始化内存 Tool 服务，注册内置工具");
        registerBuiltinTools();
        log.info("内存 Tool 服务初始化完成，已注册 {} 个工具", toolRegistry.size());
    }

    // ======================== 工具定义 ========================

    /** 工具定义 */
    public record ToolDefinition(
            String name,
            String description,
            Map<String, ParameterDef> parameters,
            boolean builtin
    ) {}

    /** 参数定义 */
    public record ParameterDef(
            String type,
            String description,
            boolean required
    ) {}

    // ======================== 公开管理 API ========================

    /**
     * 注册工具
     */
    public void registerTool(String name, String description,
                             Map<String, ParameterDef> parameters,
                             Function<Map<String, Object>, Object> executor) {
        toolRegistry.put(name, new ToolDefinition(name, description, parameters, false));
        executorRegistry.put(name, executor);
        log.info("工具已注册: {} - {}", name, description);
    }

    /**
     * 注销工具
     */
    public void unregisterTool(String name) {
        ToolDefinition removed = toolRegistry.remove(name);
        executorRegistry.remove(name);
        if (removed != null) {
            log.info("工具已注销: {}", name);
        }
    }

    /**
     * 获取所有已注册的工具定义
     */
    public List<ToolDefinition> listTools() {
        return new ArrayList<>(toolRegistry.values());
    }

    /**
     * 获取单个工具定义
     */
    public ToolDefinition getToolDefinition(String name) {
        return toolRegistry.get(name);
    }

    // ======================== 工具执行 ========================

    @Override
    public ToolExecutionResult execute(String toolName, Map<String, Object> parameters) {
        log.info("工具调用: tool=[{}], params={}", toolName, parameters);

        if (toolName == null || toolName.trim().isEmpty()) {
            return new ToolExecutionResult(false, null, "工具名称不能为空");
        }

        ToolDefinition def = toolRegistry.get(toolName);
        if (def == null) {
            return new ToolExecutionResult(false, null,
                    "未知工具: " + toolName + "，可用工具: " + toolRegistry.keySet());
        }

        // 参数校验
        String validationError = validateParameters(def, parameters);
        if (validationError != null) {
            return new ToolExecutionResult(false, null, "参数校验失败: " + validationError);
        }

        // 查找执行器
        Function<Map<String, Object>, Object> executor = executorRegistry.get(toolName);
        if (executor == null) {
            return new ToolExecutionResult(false, null, "工具 [" + toolName + "] 未注册执行器");
        }

        try {
            Map<String, Object> mergedParams = mergeParameters(def, parameters);
            Object result = executor.apply(mergedParams);

            Map<String, Object> enriched = new HashMap<>();
            enriched.put("tool", toolName);
            enriched.put("result", result);
            enriched.put("executed_at", System.currentTimeMillis());

            log.info("工具 [{}] 执行成功", toolName);
            return new ToolExecutionResult(true, enriched, null);

        } catch (Exception e) {
            log.error("工具 [{}] 执行异常", toolName, e);
            return new ToolExecutionResult(false, null, "工具执行异常: " + e.getMessage());
        }
    }

    // ======================== 内部 ========================

    private String validateParameters(ToolDefinition def, Map<String, Object> params) {
        if (def.parameters() == null || def.parameters().isEmpty()) {
            return null;
        }
        Map<String, Object> safeParams = params != null ? params : Map.of();
        for (Map.Entry<String, ParameterDef> entry : def.parameters().entrySet()) {
            String key = entry.getKey();
            ParameterDef paramDef = entry.getValue();
            if (paramDef.required() && !safeParams.containsKey(key)) {
                return "缺少必填参数: " + key;
            }
            if (safeParams.containsKey(key) && safeParams.get(key) == null && paramDef.required()) {
                return "必填参数不能为 null: " + key;
            }
        }
        return null;
    }

    private Map<String, Object> mergeParameters(ToolDefinition def, Map<String, Object> params) {
        Map<String, Object> merged = new HashMap<>();
        if (params != null) {
            merged.putAll(params);
        }
        if (def.parameters() != null) {
            for (String key : def.parameters().keySet()) {
                merged.putIfAbsent(key, null);
            }
        }
        return merged;
    }

    // ======================== 内置工具 ========================

    private void registerBuiltinTools() {
        // 1. 天气查询
        registerTool(
                IntentType.WEATHER.getCode(),
                IntentType.WEATHER.getDescription(),
                Map.of(
                        "location", new ParameterDef("string", "城市名称，如 北京、上海（支持别名映射）", true)
                ),
                params -> {
                    String location = (String) params.get("location");
                    String city = location != null ? location : "未知";
                    int code = Math.floorMod(city.hashCode(), 6);
                    String[] conditions = {"晴", "多云", "阴天", "小雨", "中雨", "微风"};
                    String condition = conditions[code];
                    int temp = 20 + Math.floorMod(city.hashCode(), 15);
                    return Map.of(
                            "city", city,
                            "temperature", temp + "°C",
                            "condition", condition,
                            "humidity", (40 + Math.floorMod(city.hashCode() * 7, 40)) + "%",
                            "wind", (2 + Math.floorMod(city.hashCode() * 3, 4)) + "级",
                            "updated_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    );
                }
        );

        // 2. 计算器
        registerTool(
                IntentType.CALCULATOR.getCode(),
                IntentType.CALCULATOR.getDescription(),
                Map.of(
                        "expression", new ParameterDef("string", "数学表达式，如 1+2*3", true)
                ),
                params -> {
                    String expr = ((String) params.get("expression")).trim();
                    try {
                        double result = evaluateSimpleExpression(expr);
                        return Map.of("expression", expr, "result", result);
                    } catch (Exception e) {
                        return Map.of("error", "计算失败: " + e.getMessage(), "expression", expr);
                    }
                }
        );

        // 3. 时间日期
        registerTool(
                "datetime",
                "获取当前日期和时间信息",
                Map.of(
                        "timezone", new ParameterDef("string", "时区，如 Asia/Shanghai（可选）", false)
                ),
                params -> {
                    String tz = (String) params.get("timezone");
                    TimeZone zone = tz != null ? TimeZone.getTimeZone(tz) : TimeZone.getDefault();
                    return Map.of(
                            "datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "timezone", zone.getDisplayName(),
                            "timezone_id", zone.getID(),
                            "timestamp", System.currentTimeMillis()
                    );
                }
        );

        // 4. 随机数生成
        registerTool(
                "random",
                "生成指定范围内的随机整数",
                Map.of(
                        "min", new ParameterDef("integer", "最小值（包含），默认 0", false),
                        "max", new ParameterDef("integer", "最大值（包含），默认 100", false)
                ),
                params -> {
                    int min = params.get("min") != null ? ((Number) params.get("min")).intValue() : 0;
                    int max = params.get("max") != null ? ((Number) params.get("max")).intValue() : 100;
                    if (min > max) { int t = min; min = max; max = t; }
                    int value = min + new Random().nextInt(max - min + 1);
                    return Map.of("min", min, "max", max, "value", value);
                }
        );

        // 5. 文本统计
        registerTool(
                "text_stats",
                "统计文本的字数、字符数等信息",
                Map.of(
                        "text", new ParameterDef("string", "要统计的文本", true)
                ),
                params -> {
                    String text = (String) params.get("text");
                    return Map.of(
                            "char_count", text.length(),
                            "word_count", text.split("\\s+").length,
                            "chinese_char_count", text.replaceAll("[^\\u4e00-\\u9fa5]", "").length(),
                            "line_count", text.split("\n").length
                    );
                }
        );
    }

    // ======================== 简易表达式求值器 ========================

    /**
     * 简易数学表达式求值，支持 + - * / 和括号
     */
    private static double evaluateSimpleExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");
        if (!expr.matches("[0-9+\\-*/().]+")) {
            throw new IllegalArgumentException("表达式包含不支持的字符");
        }
        return new ExprParser(expr).parse();
    }

    private static class ExprParser {
        private final String input;
        private int pos;

        ExprParser(String input) {
            this.input = input;
            this.pos = 0;
        }

        double parse() {
            double result = parseAddSub();
            if (pos < input.length()) {
                throw new IllegalArgumentException("无法解析的字符位置 " + pos + ": " + input.charAt(pos));
            }
            return result;
        }

        private double parseAddSub() {
            double left = parseMulDiv();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == '+') { pos++; left += parseMulDiv(); }
                else if (c == '-') { pos++; left -= parseMulDiv(); }
                else break;
            }
            return left;
        }

        private double parseMulDiv() {
            double left = parseAtom();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == '*') { pos++; left *= parseAtom(); }
                else if (c == '/') {
                    pos++;
                    double right = parseAtom();
                    if (right == 0) throw new ArithmeticException("除数不能为 0");
                    left /= right;
                }
                else break;
            }
            return left;
        }

        private double parseAtom() {
            if (pos >= input.length()) throw new IllegalArgumentException("表达式意外结束");
            char c = input.charAt(pos);
            if (c == '(') {
                pos++; // 跳过 '('
                double val = parseAddSub();
                if (pos >= input.length() || input.charAt(pos) != ')') {
                    throw new IllegalArgumentException("缺少右括号");
                }
                pos++; // 跳过 ')'
                return val;
            }
            if (c == '-') {
                pos++;
                return -parseAtom();
            }
            if (c >= '0' && c <= '9') {
                int start = pos;
                while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                    pos++;
                }
                return Double.parseDouble(input.substring(start, pos));
            }
            throw new IllegalArgumentException("意外的字符: " + c);
        }
    }
}
