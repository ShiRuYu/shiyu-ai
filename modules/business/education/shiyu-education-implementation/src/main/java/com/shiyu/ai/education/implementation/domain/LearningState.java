package com.shiyu.ai.education.implementation.domain;

/**
 * 定义 Learning 可用的枚举值及其业务语义。
 */
public enum LearningState {
    NOT_STARTED,
    LEARNING,
    MASTERED,
    PROFICIENT,
    FORGOTTEN,
    REVIEWING;

    /**
     * 执行 Learning 相关业务数据，并返回处理结果。
     *
     * @return 返回 Learning 相关操作生成的结果数据。
     */
    public LearningState startLearning() {
        return switch (this) {
            case NOT_STARTED, FORGOTTEN -> LEARNING;
            default -> this;
        };
    }

    /**
     * 执行 Learning 相关业务数据，并返回处理结果。
     *
     * @return 返回 Learning 相关操作生成的结果数据。
     */
    public LearningState passAssessment() {
        return switch (this) {
            case LEARNING, REVIEWING -> MASTERED;
            default -> this;
        };
    }

    /**
     * 执行 Learning 相关业务数据，并返回处理结果。
     *
     * @return 返回 Learning 相关操作生成的结果数据。
     */
    public LearningState deepPractice() {
        return this == MASTERED ? PROFICIENT : this;
    }

    /**
     * 执行 Learning 相关业务数据，并返回处理结果。
     *
     * @return 返回 Learning 相关操作生成的结果数据。
     */
    public LearningState forget() {
        return switch (this) {
            case MASTERED, PROFICIENT, REVIEWING -> FORGOTTEN;
            default -> this;
        };
    }

    /**
     * 执行 Learning 相关业务数据，并返回处理结果。
     *
     * @return 返回 Learning 相关操作生成的结果数据。
     */
    public LearningState scheduleReview() {
        return this == FORGOTTEN ? REVIEWING : this;
    }

    /**
     * 执行 Learning 相关业务数据，并返回处理结果。
     *
     * @return 返回 Learning 相关操作生成的结果数据。
     */
    public LearningState giveUp() {
        return this == LEARNING ? NOT_STARTED : this;
    }
}
