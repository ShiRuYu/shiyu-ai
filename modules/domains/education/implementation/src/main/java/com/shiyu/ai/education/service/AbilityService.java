package com.shiyu.ai.education.service;

import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Ability 接口
 */

public interface AbilityService {

    /**
     * Get
     * @return 处理结果
     */
    AbilityValue get(ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * Update
     * @param BloomTaxonomy BloomTaxonomy
     * @param double double
     * @return 处理结果
     */
    void update(ActorContext actor, Long studentId, Long knowledgeId, BloomTaxonomy dimension, double accuracy);
}
