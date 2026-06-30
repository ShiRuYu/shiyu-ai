package com.shiyu.ai.education.ability;

import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;

public interface AbilityService {

    AbilityValue get(Long studentId, Long knowledgeId);

    void update(Long studentId, Long knowledgeId, BloomTaxonomy dimension, double accuracy);
}
