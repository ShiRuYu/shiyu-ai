package com.shiyu.ai.knowledge.ability;

import com.shiyu.ai.knowledge.domain.AbilityValue;
import com.shiyu.ai.knowledge.domain.BloomTaxonomy;

public interface AbilityService {

    AbilityValue get(Long studentId, Long knowledgeId);

    void update(Long studentId, Long knowledgeId, BloomTaxonomy dimension, double accuracy);
}
