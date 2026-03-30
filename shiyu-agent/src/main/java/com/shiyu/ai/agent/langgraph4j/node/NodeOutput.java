package com.shiyu.ai.agent.langgraph4j.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点输出类
 * 用于封装节点的执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeOutput {

    private boolean success;

    private String msg;
    
    /**
     * 输出结果 Map
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * 从 Map 创建 NodeOutput
     * @param data 数据 Map
     * @return NodeOutput
     */
    public static NodeOutput fromMap(Map<String, Object> data) {
        return NodeOutput.builder()
                .data(data != null ? data : new HashMap<>())
                .build();
    }
    
    /**
     * 转换为 Map
     * @return Map<String, Object>
     */
    public Map<String, Object> toMap() {
        return this.data;
    }
    
    /**
     * 获取数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 数据值
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, T defaultValue) {
        Object value = data.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 添加数据
     * @param key 键
     * @param value 值
     */
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }
}
