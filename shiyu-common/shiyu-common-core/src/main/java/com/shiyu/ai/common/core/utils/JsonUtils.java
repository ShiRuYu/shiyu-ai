package com.shiyu.ai.common.core.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            // 启用标准 JDK 序列化机制处理 java.time 类型（如 LocalDateTime）
            .findAndRegisterModules()
            // 禁止序列化时失败因未知属性导致的异常（兼容性更强）
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // 禁止序列化时将空对象转换为空 JSON 对象时抛出异常
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            // 时间格式输出为字符串而不是时间戳
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // 设置默认时区为系统默认
            .setTimeZone(TimeZone.getDefault())
            // 设置默认的属性命名策略（如驼峰转下划线等，可选）
            // .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            // 设置可见性规则（如允许序列化 private 字段，可选）
            // .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            // 注册 JavaTimeModule 并配置自定义序列化器
            .registerModule(new JavaTimeModule()
                    .addSerializer(Long.class, BigNumberSerializer.INSTANCE)
                    .addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE)
                    .addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE)
                    .addSerializer(BigDecimal.class, ToStringSerializer.instance)
                    .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            );

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJsonString(Object object) {
        if (ObjectUtils.isNull(object)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (text.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (text.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> parseMap(File file) {
        try {
            return OBJECT_MAPPER.readValue(file, OBJECT_MAPPER.getTypeFactory().constructType(Map.class));
        } catch (IOException e) {
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

            return JsonUtils.parseMap(file);
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
        public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            // 超出范围 序列化位字符串
            if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
                super.serialize(value, gen, provider);
            } else {
                gen.writeString(value.toString());
            }
        }
    }
}
