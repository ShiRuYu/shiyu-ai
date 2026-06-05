package com.shiyu.ai.common.core.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.annotation.JacksonStdImpl;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.jdk.NumberSerializer;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class JSONUtils {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            // 禁止序列化时失败因未知属性导致的异常（兼容性更强）
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // 禁止序列化时将空对象转换为空 JSON 对象时抛出异常
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            // 时间格式输出为字符串而不是时间戳
            // 设置默认时区为系统默认
            .defaultTimeZone(TimeZone.getDefault())
            // 设置默认的属性命名策略（如驼峰转下划线等，可选）
            // .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            // 设置可见性规则（如允许序列化 private 字段，可选）
            // .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            // 注册 JavaTimeModule 并配置自定义序列化器
            .addModule(new SimpleModule()
                    .addSerializer(Long.class, BigNumberSerializer.INSTANCE)
                    .addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE)
                    .addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE)
                    .addSerializer(BigDecimal.class, ToStringSerializer.instance)
                    .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            )
            .build();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJsonString(Object object) {
        if (ObjectUtils.isNull(object)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (text.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (text.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 LLM 响应或任意字符串中提取第一个 JSON 片段（{} 或 []）。
     * <p>
     * 通过括号匹配定位 JSON，不依赖任何外层包裹格式。
     * 因此无论外层是 Markdown 代码块、{@code <|begin_of_box|>}、XML 还是纯文本，
     * 都能正确提取。支持转义引号内的括号，避免误判。
     *
     * @param raw 原始字符串（可为 null）
     * @return 提取的纯 JSON 字符串
     * @throws IllegalArgumentException 未找到有效的 JSON 起始括号或括号未闭合
     */
    public static String extractJsonFragment(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("输入字符串为空，无法提取 JSON");
        }
        String trimmed = raw.trim();
        // 定位第一个 JSON 起始括号
        int start = -1;
        char openChar = 0;
        char closeChar = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '{') {
                start = i;
                openChar = '{';
                closeChar = '}';
                break;
            } else if (c == '[') {
                start = i;
                openChar = '[';
                closeChar = ']';
                break;
            }
        }
        if (start < 0) {
            throw new IllegalArgumentException("响应中未找到 JSON 起始字符（{ 或 [）");
        }

        // 括号匹配：找到对应的闭合括号
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            // 跳过字符串内的内容（处理转义引号）
            if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }

            if (c == openChar) {
                depth++;
            } else if (c == closeChar) {
                depth--;
                if (depth == 0) {
                    return trimmed.substring(start, i + 1);
                }
            }
        }

        throw new IllegalArgumentException("JSON 括号未闭合，depth=" + depth);
    }

    public static Map<String, Object> parseMap(File file) {
        try {
            return OBJECT_MAPPER.readValue(file, OBJECT_MAPPER.getTypeFactory().constructType(Map.class));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载 JSON 文件（绝对路径）到 Map<String, Object>
     */
    public static Map<String, Object> loadJsonFile(String absolutePath) {
        try {
            File file = new File(absolutePath);
            if (!file.exists()) {
                throw new RuntimeException("JSON file not found at: " + absolutePath);
            }

            return JSONUtils.parseMap(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON config", e);
        }
    }

    /**
     * 超出 JS 最大最小值 处理
     */
    @JacksonStdImpl
    public static class BigNumberSerializer extends NumberSerializer {

        /**
         * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
         */
        private static final long MAX_SAFE_INTEGER = 9007199254740991L;
        private static final long MIN_SAFE_INTEGER = -9007199254740991L;

        /**
         * 提供实例
         */
        public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

        public BigNumberSerializer(Class<? extends Number> rawType) {
            super(rawType);
        }

        @Override
        public void serialize(Number value, JsonGenerator gen, SerializationContext provider) {
            // 超出范围 序列化位字符串
            if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
                super.serialize(value, gen, provider);
            } else {
                gen.writeString(value.toString());
            }
        }
    }
}
