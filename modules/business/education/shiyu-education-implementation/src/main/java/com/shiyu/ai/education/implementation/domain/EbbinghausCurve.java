package com.shiyu.ai.education.implementation.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * {@code EbbinghausCurve} 承载教育模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class EbbinghausCurve {

    /**
     * 天数，表示当前对象中的对应属性。
     */
    public static final int[] INTERVALS_DAYS = {1, 3, 7, 15, 30, 90};

    /**
     * LAMBDAS 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final double[] LAMBDAS = {0.20, 0.15, 0.10, 0.07, 0.04, 0.02, 0.01};

    private EbbinghausCurve() {}

    /**
     * {@code mastery} 执行当前类型定义的业务操作。
     *
     * @param lastStudyAt 参数值，用于执行当前操作。
     * @param reviewRound 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static double mastery(Instant lastStudyAt, int reviewRound) {
        double lambda = LAMBDAS[Math.min(reviewRound, LAMBDAS.length - 1)];
        long days = Duration.between(lastStudyAt, Instant.now()).toDays();
        return Math.exp(-lambda * days);
    }

    /**
     * {@code scheduleReviewDates} 执行当前类型定义的业务操作。
     *
     * @param learnedAt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static List<LocalDate> scheduleReviewDates(LocalDate learnedAt) {
        return Arrays.stream(INTERVALS_DAYS).mapToObj(learnedAt::plusDays).toList();
    }

    /**
     * {@code currentRound} 执行当前类型定义的业务操作。
     *
     * @param lastStudyAt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static int currentRound(Instant lastStudyAt) {
        long days = Duration.between(lastStudyAt, Instant.now()).toDays();
        int round = 0;
        for (int interval : INTERVALS_DAYS) {
            if (days <= interval) {
                break;
            }
            round++;
        }
        return Math.min(round, INTERVALS_DAYS.length);
    }
}
