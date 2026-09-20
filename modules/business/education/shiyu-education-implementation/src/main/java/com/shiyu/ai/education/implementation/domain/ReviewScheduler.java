package com.shiyu.ai.education.implementation.domain;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 实现 复习 Scheduler 所属领域的业务规则和状态变化。
 */
@Slf4j
@Component
public class ReviewScheduler {

    /**
     * 封装 复习 相关的不可变数据及其字段约束。
     */
    public record ReviewTask(
            Long studentId, Long knowledgeId, LocalDate reviewDate, int reviewRound) {}

    /**
     * 执行 复习 Scheduler 相关业务数据，并返回处理结果。
     *
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @param learnedAt 用于完成本次业务处理的 learnedAt 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<ReviewTask> scheduleAfterLearning(
            Long studentId, Long knowledgeId, Instant learnedAt) {
        LocalDate learnedDate = LocalDate.ofInstant(learnedAt, java.time.ZoneId.systemDefault());
        List<LocalDate> dates = EbbinghausCurve.scheduleReviewDates(learnedDate);

        return dates.stream()
                .map(date -> new ReviewTask(studentId, knowledgeId, date, dates.indexOf(date) + 1))
                .toList();
    }

    /**
     * 执行 复习 Scheduler 相关业务数据，并返回处理结果。
     *
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @param lastStudyAt 用于完成本次业务处理的 lastStudyAt 参数。
     * @return 返回 复习 Scheduler 相关操作生成的结果数据。
     */
    public ReviewTask nextReview(Long studentId, Long knowledgeId, Instant lastStudyAt) {
        int round = EbbinghausCurve.currentRound(lastStudyAt);
        if (round >= EbbinghausCurve.INTERVALS_DAYS.length) {
            return null;
        }
        LocalDate nextDate =
                LocalDate.ofInstant(lastStudyAt, java.time.ZoneId.systemDefault())
                        .plusDays(EbbinghausCurve.INTERVALS_DAYS[round]);
        return new ReviewTask(studentId, knowledgeId, nextDate, round + 1);
    }
}
