package com.shiyu.ai.agent.langgraph4j.node.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图定义类
 * 用于定义和描述一个具体的意图类型及其属性
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentDefinition {
    
    /**
     * 意图类型代码（唯一标识）
     */
    @Builder.Default
    private String code = "";
    
    /**
     * 意图名称
     */
    @Builder.Default
    private String name = "";
    
    /**
     * 意图描述
     */
    @Builder.Default
    private String description = "";
    
    /**
     * 意图分类
     */
    @Builder.Default
    private String category = "GENERAL";
    
    /**
     * 意图优先级（数值越大优先级越高，默认 50）
     */
    @Builder.Default
    private Integer priority = 50;
    
    /**
     * 置信度阈值（覆盖配置中的默认值）
     */
    private Double confidenceThreshold;
    
    /**
     * 示例语句列表
     */
    @Builder.Default
    private String[] examples = new String[0];
    
    /**
     * 槽位定义（用于任务型意图）
     * key: 槽位名称，value: 槽位描述
     */
    @Builder.Default
    private Map<String, String> slots = new HashMap<>();
    
    /**
     * 是否需要槽位填充（默认 false）
     */
    @Builder.Default
    private Boolean requireSlotFilling = false;
    
    /**
     * 关联的子链名称
     */
    @Builder.Default
    private String chainToCall = "chatDirect";
    
    /**
     * 是否启用（默认 true）
     */
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 自定义参数
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * 添加示例语句
     *
     * @param example 示例语句
     * @return 当前对象（支持链式调用）
     */
    public IntentDefinition addExample(String example) {
        if (this.examples == null) {
            this.examples = new String[0];
        }
        
        String[] newExamples = new String[this.examples.length + 1];
        System.arraycopy(this.examples, 0, newExamples, 0, this.examples.length);
        newExamples[this.examples.length] = example;
        this.examples = newExamples;
        
        return this;
    }
    
    /**
     * 添加槽位定义
     *
     * @param slotName 槽位名称
     * @param slotDescription 槽位描述
     * @return 当前对象（支持链式调用）
     */
    public IntentDefinition addSlot(String slotName, String slotDescription) {
        if (this.slots == null) {
            this.slots = new HashMap<>();
        }
        this.slots.put(slotName, slotDescription);
        return this;
    }
    
    /**
     * 添加自定义参数
     *
     * @param key 参数键
     * @param value 参数值
     * @return 当前对象（支持链式调用）
     */
    public IntentDefinition addParameter(String key, Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
        return this;
    }
    
    /**
     * 从 IntentType 枚举创建 IntentDefinition
     *
     * @param type 意图类型枚举
     * @return IntentDefinition
     */
    public static IntentDefinition fromIntentType(IntentType type) {
        return IntentDefinition.builder()
                .code(type.getCode())
                .name(type.getName())
                .description(type.getDescription())
                .build();
    }
    
    /**
     * 构建方法 - 设置默认的置信度阈值
     *
     * @return IntentDefinition
     */
    public IntentDefinition buildWithDefaults() {
        if (this.confidenceThreshold == null) {
            this.confidenceThreshold = 0.75;
        }
        return this;
    }
}
