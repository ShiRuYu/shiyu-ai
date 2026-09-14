package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.domain.AbilityValue;
import com.shiyu.ai.education.implementation.domain.BloomTaxonomy;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * AbilityService 服务接口，负责执行教育领域相关业务操作。
 */
public interface AbilityService {

    /**
     * 获取指定学生和知识点的能力值。
     *
     * @param actor 调用方上下文
     * @param studentId 学生标识
     * @param knowledgeId 知识点标识
     * @return 能力值
     */
    AbilityValue get(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 更新学生的能力值。
     *
     * @param actor 调用方上下文
     * @param studentId 学生标识
     * @param knowledgeId 知识点标识
     * @param dimension 能力维度
     * @param accuracy 正确率
     */
    void update(
            ActorContext actor,
            Long studentId,
            Long knowledgeId,
            BloomTaxonomy dimension,
            double accuracy);
}
