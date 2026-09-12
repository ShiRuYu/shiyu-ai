package com.shiyu.ai.education.implementation.domain;

import java.time.LocalDateTime;

/**
 * {@code AbilityValue} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param remember remember 属性，表示该记录组件承载的数据。
 * @param understand understand 属性，表示该记录组件承载的数据。
 * @param apply apply 属性，表示该记录组件承载的数据。
 * @param analyze analyze 属性，表示该记录组件承载的数据。
 * @param evaluate evaluate 属性，表示该记录组件承载的数据。
 * @param create create 属性，表示该记录组件承载的数据。
 * @param lastUpdated lastUpdated 属性，表示该记录组件承载的数据。
 */
public record AbilityValue(
        Long studentId,
        Long knowledgeId,
        double remember,
        double understand,
        double apply,
        double analyze,
        double evaluate,
        double create,
        LocalDateTime lastUpdated) {

    public double overallScore() {
        return remember * BloomTaxonomy.REMEMBER.getWeight()
                + understand * BloomTaxonomy.UNDERSTAND.getWeight()
                + apply * BloomTaxonomy.APPLY.getWeight()
                + analyze * BloomTaxonomy.ANALYZE.getWeight()
                + evaluate * BloomTaxonomy.EVALUATE.getWeight()
                + create * BloomTaxonomy.CREATE.getWeight();
    }

    public static AbilityValue empty(Long studentId, Long knowledgeId) {
        return new AbilityValue(studentId, knowledgeId, 0, 0, 0, 0, 0, 0, LocalDateTime.now());
    }
}
