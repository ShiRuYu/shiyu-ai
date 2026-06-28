package com.shiyu.ai.aiagent.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 条件边
 * 用于定义节点之间的条件连接关系
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionEdge {

    /**
     * 源节点 ID（from）
     */
    @Builder.Default
    private String from = "";

    /**
     * 默认目标节点
     */
    @Builder.Default
    private String defaultTarget = "";

    /**
     * 函数式条件
     * 接收 AgentState，返回 Boolean 或其他类型结果
     */
    @Builder.Default
    private Function<Map<String, Object>, String> functionCondition = null;

    /**
     * 节点映射
     */
    @Builder.Default
    private Map<String, String> nodeMappings = new HashMap<>();

    /**
     * 谓语条件列表（有序）
     * 使用 List 而非 Map 以避免 Predicate 作为 HashMap key 的身份等价问题
     */
    @Builder.Default
    private List<PredicateCondition> predicateConditions = new ArrayList<>();

    /**
     * 添加节点映射
     *
     * @param conditionResult 条件结果标识
     * @param target          映射的目标节点 ID
     */
    public void addNodeMapping(String conditionResult, String target) {
        this.nodeMappings.put(conditionResult, target);
    }

    /**
     * 添加谓语条件
     *
     * @param predicate 谓语条件
     * @param target    目标节点 ID
     */
    public void addPredicateCondition(Predicate<Map<String, Object>> predicate, String target) {
        this.predicateConditions.add(new PredicateCondition(predicate, target));
    }

    /**
     * 链式添加谓语条件（Builder 模式辅助方法）
     *
     * @param predicate 谓语条件
     * @param target    目标节点 ID
     * @return 当前 ConditionEdge 实例
     */
    public ConditionEdge predicateCondition(Predicate<Map<String, Object>> predicate, String target) {
        addPredicateCondition(predicate, target);
        return this;
    }

    /**
     * 谓语条件记录
     */
    @lombok.Value
    public static class PredicateCondition {
        Predicate<Map<String, Object>> predicate;
        String target;
    }

    /**
     * 添加函数式条件
     *
     * @param condition 函数式条件
     */
    public void addFunctionCondition(Function<Map<String, Object>, String> condition) {
        this.functionCondition = condition;
    }

    /**
     * 是否有函数式条件
     *
     * @return true-有函数式条件，false-无
     */
    public boolean hasFunctionCondition() {
        return this.functionCondition != null;
    }

    /**
     * 是否有谓语条件
     * @return true-有谓语条件，false-无
     */
    public boolean hasPredicateCondition() {
        return this.predicateConditions != null && !this.predicateConditions.isEmpty();
    }

    /**
     * 获取谓语条件列表（不可变视图）
     * @return 谓语条件列表
     */
    public List<PredicateCondition> getPredicateConditions() {
        return predicateConditions;
    }

    /**
     * 是否有有效的节点映射
     *
     * @return true-有有效映射，false-无
     */
    public boolean hasNodeMappings() {
        return this.nodeMappings != null && !this.nodeMappings.isEmpty();
    }

    /**
     * 验证条件边的配置
     *
     * @throws IllegalStateException 当配置无效时
     */
    public void validate() {
        if (from == null || from.isEmpty()) {
            throw new IllegalStateException("条件边的源节点 ID 不能为空");
        }

        if (!hasFunctionCondition() && !hasPredicateCondition()) {
            throw new IllegalStateException("条件边必须至少有一个条件（函数式或谓语式）");
        }

        if (!hasNodeMappings() && (defaultTarget == null || defaultTarget.isEmpty())) {
            throw new IllegalStateException("条件边必须至少有一个节点映射或默认目标节点");
        }
    }

    /**
     * 获取条件结果
     *
     * @param state 状态对象
     * @return 条件结果
     */
    public String getTarget(Map<String, Object> state) {
        // 先尝试函数式条件
        if (this.hasFunctionCondition()) {
            String result = this.functionCondition.apply(state);
            log.debug("函数式条件执行结果：{}", result);
            return result != null ? result : this.defaultTarget;
        }
        // 再尝试谓语条件（按添加顺序依次匹配）
        if (this.hasPredicateCondition()) {
            for (PredicateCondition pc : this.predicateConditions) {
                if (pc.getPredicate().test(state)) {
                    log.debug("谓语条件匹配成功，目标节点：{}", pc.getTarget());
                    return pc.getTarget();
                }
            }
            log.debug("谓语条件未匹配，返回默认目标：{}", this.defaultTarget);
        }

        return this.defaultTarget;
    }
}
