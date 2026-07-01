package com.shiyu.ai.education.review;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public final class EbbinghausCurve {

    public static final int[] INTERVALS_DAYS = {1, 3, 7, 15, 30, 90};

    private static final double[] LAMBDAS = {0.20, 0.15, 0.10, 0.07, 0.04, 0.02, 0.01};

    private EbbinghausCurve() {
    }

    public static double mastery(Instant lastStudyAt, int reviewRound) {
        double lambda = LAMBDAS[Math.min(reviewRound, LAMBDAS.length - 1)];
        long days = Duration.between(lastStudyAt, Instant.now()).toDays();
        return Math.exp(-lambda * days);
    }

    public static List<LocalDate> scheduleReviewDates(LocalDate learnedAt) {
        return Arrays.stream(INTERVALS_DAYS)
                .mapToObj(learnedAt::plusDays)
                .toList();
    }

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
