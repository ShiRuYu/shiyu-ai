package com.shiyu.ai.aiagent.node;

import com.shiyu.ai.aiagent.node.NodeFields.FieldKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 节点输入类
 * 用于封装节点的输入参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeInput {

    /**
     * 输入参数 Map
     */
    @Builder.Default
    private Map<String, Object> parameters = new java.util.HashMap<>();

    /**
     * 从 Map 创建 NodeInput
     *
     * @param params 参数 Map
     * @return NodeInput
     */
    public static NodeInput fromMap(Map<String, Object> params) {
        return NodeInput.builder()
                .parameters(params != null ? params : new java.util.HashMap<>())
                .build();
    }

    /**
     * 转换为 Map
     *
     * @return Map<String, Object>
     */
    public Map<String, Object> toMap() {
        return this.parameters;
    }

    // ==================== String-key overloads ====================

    /**
     * 获取参数
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        Object value = parameters.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 获取参数
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    public <T> T getParameter(String key) {
        return getParameter(key, null);
    }

    /**
     * 添加参数
     *
     * @param key   键
     * @param value 值
     */
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    // ==================== FieldKey overloads ====================

    /**
     * 通过 {@link FieldKey} 获取参数
     *
     * @param field        字段键枚举
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 参数值
     * @see #getParameter(String, Object)
     */
    public <T> T getParameter(FieldKey field, T defaultValue) {
        return getParameter(field.key(), defaultValue);
    }

    /**
     * 通过 {@link FieldKey} 获取参数
     *
     * @param field 字段键枚举
     * @param <T>   值类型
     * @return 参数值，不存在返回 {@code null}
     */
    public <T> T getParameter(FieldKey field) {
        return getParameter(field.key(), null);
    }

    /**
     * 通过 {@link FieldKey} 添加参数
     *
     * @param field 字段键枚举
     * @param value 值
     */
    public void addParameter(FieldKey field, Object value) {
        addParameter(field.key(), value);
    }
}
