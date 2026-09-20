package com.shiyu.ai.education.implementation.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 实现 Ebbinghaus Curve 所属领域的业务规则和状态变化。
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
     * 执行 Ebbinghaus Curve 相关业务数据，并返回处理结果。
     *
     * @param lastStudyAt 用于完成本次业务处理的 lastStudyAt 参数。
     * @param reviewRound 用于完成本次业务处理的 reviewRound 参数。
     * @return 返回 Ebbinghaus Curve 相关操作生成的结果数据。
     */
    public static double mastery(Instant lastStudyAt, int reviewRound) {
        double lambda = LAMBDAS[Math.min(reviewRound, LAMBDAS.length - 1)];
        long days = Duration.between(lastStudyAt, Instant.now()).toDays();
        return Math.exp(-lambda * days);
    }

    /**
     * 执行 Ebbinghaus Curve 相关业务数据，并返回处理结果。
     *
     * @param learnedAt 用于完成本次业务处理的 learnedAt 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public static List<LocalDate> scheduleReviewDates(LocalDate learnedAt) {
        return Arrays.stream(INTERVALS_DAYS).mapToObj(learnedAt::plusDays).toList();
    }

    /**
     * 执行 Ebbinghaus Curve 相关业务数据，并返回处理结果。
     *
     * @param lastStudyAt 用于完成本次业务处理的 lastStudyAt 参数。
     * @return 返回 Ebbinghaus Curve 相关操作生成的结果数据。
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
