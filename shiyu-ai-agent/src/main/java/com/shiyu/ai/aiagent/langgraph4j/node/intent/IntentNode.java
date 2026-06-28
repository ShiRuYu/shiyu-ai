package com.shiyu.ai.aiagent.langgraph4j.node.intent;

import com.shiyu.ai.aiagent.langgraph4j.node.BaseNode;
import com.shiyu.ai.aiagent.langgraph4j.node.NodeInput;
import com.shiyu.ai.aiagent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.aiagent.langgraph4j.node.NodeType;
import com.shiyu.ai.aiagent.langgraph4j.node.NodeFields.FieldKey;
import com.shiyu.ai.aiagent.service.IntentService;
import com.shiyu.ai.aiagent.langgraph4j.node.intent.IntentDefinition;
import com.shiyu.ai.aiagent.langgraph4j.node.intent.IntentDefinitionFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 意图识别节点
 * 用于识别和处理用户的意图
 *
 * @author shiyu-ai
 * @date 2026-03-26
 */
@Setter
@Getter
@Slf4j
public class IntentNode extends BaseNode {

    private IntentConfig config;
    
    /**
     * 意图识别服务（必须依赖）
     */
    private final IntentService intentService;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     * @param intentService 意图识别服务
     */
    private IntentNode(IntentConfig config, IntentService intentService) {
        super(config != null ? config : new IntentConfig());
        this.config = config != null ? config : new IntentConfig();
        // 设置节点类型为 INTENT
        this.config.setNodeType(NodeType.INTENT);
        this.intentService = intentService;
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 IntentNode 实例
     */
    public static class Builder {
        private IntentConfig config;
        private IntentService intentService;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(IntentConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置意图识别服务
         * @param intentService 意图识别服务
         * @return Builder 实例
         */
        public Builder intentService(IntentService intentService) {
            this.intentService = intentService;
            return this;
        }

        /**
         * 构建并返回 IntentNode 实例
         * 在构建前会进行必要的校验
         * @return IntentNode 实例
         * @throws IllegalStateException 如果校验失败
         */
        public IntentNode build() {
            // 校验：intentService 不能为空
            if (intentService == null) {
                throw new IllegalStateException("创建 IntentNode 失败：intentService 不能为空");
            }
            
            // 校验：如果配置了 config，则 config 不能为空对象（可以为 null，会自动创建）
            // 注意：config 允许为 null，会在构造函数中自动创建默认配置
            
            // 所有校验通过，创建并返回实例
            return new IntentNode(config, intentService);
        }
    }

    @Override
    public NodeOutput doExecute(NodeInput input) {
        log.info("开始执行意图识别节点，输入：{}", input);
        
        try {
            // 1. 获取用户输入
            String query = input.getParameter(FieldKey.QUERY, "");

            if (query.trim().isEmpty()) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("用户输入为空");
                output.addData(FieldKey.INTENT_CODE, "UNKNOWN");
                return output;
            }
            
            // 2. 调用意图识别服务（通过 category + agentId 查找定义）
            String category = config.getCategory();
            String agentId = input.getParameter(FieldKey.AGENT_ID, "default");
            String platform = config.getPlatform();
            String modelName = config.getModelName();
            IntentService.IntentRecognitionResult result =
                    intentService.recognize(agentId, category, query, platform, modelName);
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(result.success());
            output.setMsg(result.errorMessage() != null ? result.errorMessage() : "意图识别成功");
            
            // 4. 添加识别结果到输出（路由由条件边从 IntentDefinitionFactory 驱动）
            output.addData(FieldKey.INTENT_CODE, result.intentCode());
            output.addData(FieldKey.INTENT_NAME, result.intentName());
            output.addData(FieldKey.CONFIDENCE, result.confidence());
            output.addData(FieldKey.SLOTS, result.slots());

            // 5. 从工厂查找 IntentDefinition，将参数映射和 slot schema 传递给下游工具节点
            String intentCode = result.intentCode();
            if (intentCode != null && !intentCode.trim().isEmpty()) {
                List<IntentDefinition> defs = IntentDefinitionFactory.getAll(agentId);
                for (IntentDefinition def : defs) {
                    if (intentCode.equals(def.getCode())) {
                        if (def.getParameterMapping() != null && !def.getParameterMapping().isEmpty()) {
                            output.addData(FieldKey.PARAMETER_MAPPING, def.getParameterMapping());
                        }
                        if (def.getSlotDefaults() != null && !def.getSlotDefaults().isEmpty()) {
                            output.addData(FieldKey.SLOT_DEFAULTS, def.getSlotDefaults());
                        }
                        if (def.getSlots() != null && !def.getSlots().isEmpty()) {
                            output.addData(FieldKey.SLOT_DEFINITIONS, def.getSlots());
                        }
                        log.debug("意图 {} 携带 mapping={}, defaults={}, slots={}", intentCode,
                                def.getParameterMapping(), def.getSlotDefaults(), def.getSlots());
                        break;
                    }
                }
            }

            log.info("意图识别完成，intentCode={}, confidence={}",
                    result.intentCode(), result.confidence());
            return output;
            
        } catch (Exception e) {
            log.error("意图识别失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("意图识别失败：" + e.getMessage());
            output.addData(FieldKey.INTENT_CODE, "ERROR");
            return output;
        }
    }
}
