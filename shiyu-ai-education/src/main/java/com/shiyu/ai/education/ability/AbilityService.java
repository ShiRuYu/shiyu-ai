package com.shiyu.ai.education.ability;

import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;

/**
 * Ability 接口
 */

public interface AbilityService {

    /**
     * Get
     * @return 处理结果
     */
    AbilityValue get(Long studentId, Long knowledgeId);

    /**
     * Update
     * @param BloomTaxonomy BloomTaxonomy
     * @param double double
     * @return 处理结果
     */
    void update(Long studentId, Long knowledgeId, BloomTaxonomy dimension, double accuracy);
}
