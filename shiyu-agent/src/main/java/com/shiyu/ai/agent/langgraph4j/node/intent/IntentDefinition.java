package com.shiyu.ai.agent.langgraph4j.node.intent;

import com.google.common.collect.Maps;
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
     * Slot → 工具参数名的显式映射
     * <p>
     * key: slot 名称（LLM 输出），value: 工具服务方法参数名。
     * 当 slot 名与工具参数名不一致时，通过此映射重命名。
     * 例如：{"city" → "location", "date" → "queryDate"}
     */
    @Builder.Default
    private Map<String, String> parameterMapping = new HashMap<>();

    /**
     * Slot 默认值
     * <p>
     * 当 LLM 未提取到某个 slot 时，使用此默认值兜底。
     * 例如：{"unit" → "celsius", "lang" → "zh"}
     */
    @Builder.Default
    private Map<String, String> slotDefaults = new HashMap<>();

    /** 路由目标节点 ID（识别后路由到哪个节点） */
    @Builder.Default
    private String targetNode = "";
    
    /** 关联工具名称（当 targetNode 指向工具节点时，指定具体工具） */
    @Builder.Default
    private String toolName = "";
    
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
            this.slots = Maps.newHashMap();
        } else if (!(this.slots instanceof HashMap)) {
            this.slots = Maps.newHashMap(this.slots);
        }
        this.slots.put(slotName, slotDescription);
        return this;
    }
    
    /**
     * 添加槽位→工具参数的映射
     *
     * @param slotName     slot 名称
     * @param paramName    工具参数名
     * @return 当前对象（支持链式调用）
     */
    public IntentDefinition addParameterMapping(String slotName, String paramName) {
        if (this.parameterMapping == null) {
            this.parameterMapping = Maps.newHashMap();
        } else if (!(this.parameterMapping instanceof HashMap)) {
            this.parameterMapping = Maps.newHashMap(this.parameterMapping);
        }
        this.parameterMapping.put(slotName, paramName);
        return this;
    }

    /**
     * 添加槽位默认值
     *
     * @param slotName slot 名称
     * @param defaultValue 默认值
     * @return 当前对象（支持链式调用）
     */
    public IntentDefinition addSlotDefault(String slotName, String defaultValue) {
        if (this.slotDefaults == null) {
            this.slotDefaults = Maps.newHashMap();
        } else if (!(this.slotDefaults instanceof HashMap)) {
            this.slotDefaults = Maps.newHashMap(this.slotDefaults);
        }
        this.slotDefaults.put(slotName, defaultValue);
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
            this.parameters = Maps.newHashMap();
        } else if (!(this.parameters instanceof HashMap)) {
            this.parameters = Maps.newHashMap(this.parameters);
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
