package com.shiyu.ai.education.implementation.domain;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * {@code ReviewScheduler} 承载教育模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Slf4j
@Component
public class ReviewScheduler {

    /**
     * {@code ReviewTask} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param studentId 学生标识，表示该记录组件承载的数据。
     * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
     * @param reviewDate reviewDate 属性，表示该记录组件承载的数据。
     * @param reviewRound reviewRound 属性，表示该记录组件承载的数据。
     */
    public record ReviewTask(
            Long studentId, Long knowledgeId, LocalDate reviewDate, int reviewRound) {}

    /**
     * {@code scheduleAfterLearning} 执行当前类型定义的业务操作。
     *
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     * @param learnedAt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code nextReview} 执行当前类型定义的业务操作。
     *
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     * @param lastStudyAt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
