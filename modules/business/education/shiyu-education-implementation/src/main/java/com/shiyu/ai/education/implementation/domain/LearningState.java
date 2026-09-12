package com.shiyu.ai.education.implementation.domain;

/**
 * 学习状态机
 *
 * <p>NOT_STARTED → LEARNING → MASTERED → FORGOTTEN → REVIEWING → MASTERED ↘ REVIEWING → FORGOTTEN
 * MASTERED → PROFICIENT PROFICIENT → FORGOTTEN
 */
public enum LearningState {
    NOT_STARTED,
    LEARNING,
    MASTERED,
    PROFICIENT,
    FORGOTTEN,
    REVIEWING;

    /**
     * {@code startLearning} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public LearningState startLearning() {
        return switch (this) {
            case NOT_STARTED, FORGOTTEN -> LEARNING;
            default -> this;
        };
    }

    /**
     * {@code passAssessment} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public LearningState passAssessment() {
        return switch (this) {
            case LEARNING, REVIEWING -> MASTERED;
            default -> this;
        };
    }

    /**
     * {@code deepPractice} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public LearningState deepPractice() {
        return this == MASTERED ? PROFICIENT : this;
    }

    /**
     * {@code forget} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public LearningState forget() {
        return switch (this) {
            case MASTERED, PROFICIENT, REVIEWING -> FORGOTTEN;
            default -> this;
        };
    }

    /**
     * {@code scheduleReview} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public LearningState scheduleReview() {
        return this == FORGOTTEN ? REVIEWING : this;
    }

    /**
     * {@code giveUp} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public LearningState giveUp() {
        return this == LEARNING ? NOT_STARTED : this;
    }
}
