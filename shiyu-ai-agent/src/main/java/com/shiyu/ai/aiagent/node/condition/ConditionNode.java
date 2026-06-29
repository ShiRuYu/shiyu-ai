package com.shiyu.ai.aiagent.node.condition;

import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.aiagent.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 条件判断节点
 * 用于根据条件决定执行路径
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class ConditionNode extends BaseNode {

    private ConditionConfig config;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private ConditionNode(ConditionConfig config) {
        super(config != null ? config : new ConditionConfig());
        this.config = config != null ? config : new ConditionConfig();
        // 设置节点类型??CONDITION
        this.config.setNodeType(NodeType.CONDITION);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 ConditionNode 实例
     */
    public static class Builder {
        private ConditionConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(ConditionConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返??ConditionNode 实例
         * @return ConditionNode 实例
         */
        public ConditionNode build() {
            return new ConditionNode(config);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行条件判断节点：{}", config.getNodeName());
        log.debug("条件配置：conditionType={}, conditionExpression={}, defaultBranch={}", 
                config.getConditionType(), config.getConditionExpression(), config.getDefaultBranch());
        
        try {
            // 1. 获取条件表达??
            String conditionExpression = input.getParameter(FieldKey.CONDITION_EXPRESSION, config.getConditionExpression());
            String conditionType = input.getParameter(FieldKey.CONDITION_TYPE, config.getConditionType() != null ? config.getConditionType() : "EXPRESSION");
            
            // 2. 执行条件判断
            boolean result = evaluateCondition(input, conditionType, conditionExpression);
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("条件判断执行成功");
            output.addData(FieldKey.CONDITION_RESULT, result);

            // 4. 根据结果设置下一个分??
            String nextBranch = result ? input.getParameter(FieldKey.TRUE_BRANCH, config.getTrueBranch()) : input.getParameter(FieldKey.DEFAULT_BRANCH, config.getDefaultBranch());
            output.addData(FieldKey.NEXT_NODE, nextBranch);
            output.addData(FieldKey.BRANCH, result ? "true" : "false");
            
            log.info("条件判断完成：result={}, nextNode={}", result, nextBranch);
            return output;
            
        } catch (Exception e) {
            log.error("条件判断节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("条件判断节点执行失败?? + e.getMessage());
            // 返回默认分支
            output.addData(FieldKey.NEXT_NODE, config.getDefaultBranch());
            return output;
        }
    }
    
    /**
     * 评估条件
     */
    private boolean evaluateCondition(NodeInput input, String conditionType, String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            log.warn("条件表达式为空，返回 false");
            return false;
        }
        
        switch (conditionType) {
            case "EXPRESSION":
                return evaluateExpression(expression, input);
            case "SCRIPT":
                return evaluateScript(expression, input);
            case "INTENT":
                return evaluateIntent(expression, input);
            default:
                log.warn("未知的条件类型：{}，使用表达式评估", conditionType);
                return evaluateExpression(expression, input);
        }
    }
    
    /**
     * 评估表达式（简化实现）
     */
    private boolean evaluateExpression(String expression, NodeInput input) {
        // 简单的变量检查：如果表达式是变量名，检查其值是否为 true
        Object value = input.getParameter(expression, null);
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        
        // 支持简单的比较表达式（??"var == value"??
        if (expression.contains("==")) {
            String[] parts = expression.split("==");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = input.getParameter(varName, null);
                return expectedValue.equals(actualValue != null ? actualValue.toString() : null);
            }
        }
        
        // 默认返回 false
        return false;
    }
    
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    /**
     * 评估脚本（使??Spring Expression Language??
     * <p>支持 #variableName 引用 state 中的任意字段，支持布尔表达式如 {@code #score > 0.5}</p>
     */
    private boolean evaluateScript(String script, NodeInput input) {
        try {
            StandardEvaluationContext ctx = new StandardEvaluationContext();
            input.toMap().forEach((k, v) -> ctx.setVariable(k, v));
            Boolean result = SPEL_PARSER.parseExpression(script).getValue(ctx, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("脚本条件评估失败: script={}, error={}", script, e.getMessage());
            return false;
        }
    }
    
    /**
     * 评估意图条件
     */
    private boolean evaluateIntent(String expectedIntent, NodeInput input) {
        // 检查当前意图是否匹配期望的意图
        Object currentIntent = input.getParameter(FieldKey.INTENT_CODE, null);
        return expectedIntent.equals(currentIntent);
    }
}
