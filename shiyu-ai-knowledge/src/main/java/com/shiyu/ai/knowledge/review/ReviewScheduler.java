package com.shiyu.ai.knowledge.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class ReviewScheduler {

    public record ReviewTask(Long studentId, Long knowledgeId, LocalDate reviewDate, int reviewRound) {
    }

    public List<ReviewTask> scheduleAfterLearning(Long studentId, Long knowledgeId, Instant learnedAt) {
        LocalDate learnedDate = LocalDate.ofInstant(learnedAt, java.time.ZoneId.systemDefault());
        List<LocalDate> dates = EbbinghausCurve.scheduleReviewDates(learnedDate);

        return dates.stream()
                .map(date -> new ReviewTask(studentId, knowledgeId, date, dates.indexOf(date) + 1))
                .toList();
    }

    public ReviewTask nextReview(Long studentId, Long knowledgeId, Instant lastStudyAt) {
        int round = EbbinghausCurve.currentRound(lastStudyAt);
        if (round >= EbbinghausCurve.INTERVALS_DAYS.length) {
            return null;
        }
        LocalDate nextDate = LocalDate.ofInstant(lastStudyAt, java.time.ZoneId.systemDefault())
                .plusDays(EbbinghausCurve.INTERVALS_DAYS[round]);
        return new ReviewTask(studentId, knowledgeId, nextDate, round + 1);
    }
}
