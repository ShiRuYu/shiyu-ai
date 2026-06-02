package com.shiyu.ai.agent.langgraph4j.node.intent;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.biz.agent.service.IntentService;
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
            String userInput = input.getParameter(FieldKey.USER_INPUT, "");
            if (userInput.trim().isEmpty()) {
                userInput = input.getParameter(FieldKey.QUERY, "");
            }

            if (userInput.trim().isEmpty()) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("用户输入为空");
                output.addData(FieldKey.INTENT_CODE, "UNKNOWN");
                return output;
            }
            
            // 2. 调用意图识别服务
            List<IntentDefinition> supportedIntents = getSupportedIntents();
            IntentService.IntentRecognitionResult result = 
                    intentService.recognize(userInput, supportedIntents);
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(result.success());
            output.setMsg(result.errorMessage() != null ? result.errorMessage() : "意图识别成功");
            
            // 4. 添加识别结果到输出
            output.addData(FieldKey.INTENT_CODE, result.intentCode());
            output.addData(FieldKey.INTENT_NAME, result.intentName());
            output.addData(FieldKey.CONFIDENCE, result.confidence());
            output.addData(FieldKey.SLOTS, result.slots());

            // 5. 如果识别成功，将意图代码添加到状态中供条件边使用
            if (result.success()) {
                output.addData(FieldKey.NEXT_NODE, getResultIntentKey(result.intentCode()));
                log.info("意图识别成功：code={}, name={}, confidence={}", 
                        result.intentCode(), result.intentName(), result.confidence());
            } else {
                log.warn("意图识别失败或置信度不足：{}", result.errorMessage());
            }
            
            log.info("意图识别完成，结果：{}", output);
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

    /**
     * 获取支持的意图列表
     * @return 支持的意图定义列表
     */
    private List<IntentDefinition> getSupportedIntents() {
        if (config.getSupportedIntents() != null && config.getSupportedIntents().length > 0) {
            return List.of(config.getSupportedIntents());
        }
        return null;
    }
    
    /**
     * 获取结果意图键（用于条件边路由）
     * @param intentCode 意图代码
     * @return 意图键
     */
    private String getResultIntentKey(String intentCode) {
        // 返回意图代码本身，用于条件边的映射
        return intentCode != null ? intentCode : "UNKNOWN";
    }

}
