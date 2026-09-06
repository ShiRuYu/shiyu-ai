package com.shiyu.ai.education.domain;

/**
 * 学习状态机
 *
 * NOT_STARTED → LEARNING → MASTERED → FORGOTTEN → REVIEWING → MASTERED
 *                                                  ↘  REVIEWING → FORGOTTEN
 *                              MASTERED → PROFICIENT
 *                              PROFICIENT → FORGOTTEN
 */
public enum LearningState {
    NOT_STARTED,
    LEARNING,
    MASTERED,
    PROFICIENT,
    FORGOTTEN,
    REVIEWING;

    public LearningState startLearning() {
        return switch (this) {
            case NOT_STARTED, FORGOTTEN -> LEARNING;
            default -> this;
        };
    }

    public LearningState passAssessment() {
        return switch (this) {
            case LEARNING, REVIEWING -> MASTERED;
            default -> this;
        };
    }

    public LearningState deepPractice() {
        return this == MASTERED ? PROFICIENT : this;
    }

    public LearningState forget() {
        return switch (this) {
            case MASTERED, PROFICIENT, REVIEWING -> FORGOTTEN;
            default -> this;
        };
    }

    public LearningState scheduleReview() {
        return this == FORGOTTEN ? REVIEWING : this;
    }

    public LearningState giveUp() {
        return this == LEARNING ? NOT_STARTED : this;
    }
}
