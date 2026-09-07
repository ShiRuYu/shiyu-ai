package com.shiyu.ai.education.domain;

import com.shiyu.ai.education.domain.enums.ReviewTaskStatus;
import com.shiyu.ai.education.domain.enums.StudyPlanItemStatus;
import com.shiyu.ai.education.domain.enums.StudyPlanStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EducationDomainBehaviorTest {

    @Test
    void calculatesWeightedAbilityAndProvidesAnEmptyValue() {
        AbilityValue value = new AbilityValue(7L, 9L, 100, 80, 60, 40, 20, 0, null);

        assertEquals(56.0, value.overallScore(), 0.0001);
        AbilityValue empty = AbilityValue.empty(7L, 9L);
        assertEquals(7L, empty.studentId());
        assertEquals(9L, empty.knowledgeId());
        assertEquals(0.0, empty.overallScore());
        assertNotNull(empty.lastUpdated());
    }

    @Test
    void validatesDifficultyLevels() {
        assertEquals(DifficultyLevel.HARD, DifficultyLevel.fromLevel(3));
        assertThrows(IllegalArgumentException.class, () -> DifficultyLevel.fromLevel(99));
    }

    @Test
    void schedulesEbbinghausReviewsAndAdvancesRounds() {
        LocalDate learned = LocalDate.of(2026, 8, 25);
        assertEquals(
                List.of(learned.plusDays(1), learned.plusDays(3), learned.plusDays(7),
                        learned.plusDays(15), learned.plusDays(30), learned.plusDays(90)),
                EbbinghausCurve.scheduleReviewDates(learned));

        Instant recent = Instant.now().minusSeconds(2 * 24 * 60 * 60L);
        assertEquals(1, EbbinghausCurve.currentRound(recent));
        assertEquals(1.0, EbbinghausCurve.mastery(Instant.now(), 0), 0.0001);
    }

    @Test
    void appliesLearningStateTransitions() {
        assertEquals(LearningState.LEARNING, LearningState.NOT_STARTED.startLearning());
        assertEquals(LearningState.LEARNING, LearningState.FORGOTTEN.startLearning());
        assertEquals(LearningState.MASTERED, LearningState.REVIEWING.passAssessment());
        assertEquals(LearningState.PROFICIENT, LearningState.MASTERED.deepPractice());
        assertEquals(LearningState.FORGOTTEN, LearningState.PROFICIENT.forget());
        assertEquals(LearningState.REVIEWING, LearningState.FORGOTTEN.scheduleReview());
        assertEquals(LearningState.NOT_STARTED, LearningState.LEARNING.giveUp());
        assertEquals(LearningState.MASTERED, LearningState.MASTERED.passAssessment());
    }

    @Test
    void createsReviewTasksAndStopsAfterTheLastRound() {
        ReviewScheduler scheduler = new ReviewScheduler();
        Instant learnedAt = Instant.parse("2026-08-25T00:00:00Z");
        List<ReviewScheduler.ReviewTask> tasks = scheduler.scheduleAfterLearning(7L, 9L, learnedAt);

        assertEquals(6, tasks.size());
        assertEquals(1, tasks.get(0).reviewRound());
        assertEquals(6, tasks.get(5).reviewRound());
        assertEquals(learnedAt.atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1),
                tasks.get(0).reviewDate());
        assertNotNull(scheduler.nextReview(7L, 9L, Instant.now().minusSeconds(2 * 24 * 60 * 60L)));
        assertNull(scheduler.nextReview(7L, 9L, Instant.now().minusSeconds(365 * 24 * 60 * 60L)));
    }

    @Test
    void mapsPersistedStatusCodesAndUnknownCodesToNull() {
        assertEquals(ReviewTaskStatus.COMPLETED, ReviewTaskStatus.fromCode(2));
        assertEquals(StudyPlanItemStatus.SKIPPED, StudyPlanItemStatus.fromCode(3));
        assertEquals(StudyPlanStatus.ABANDONED, StudyPlanStatus.fromCode(2));
        assertNull(ReviewTaskStatus.fromCode(99));
        assertNull(StudyPlanItemStatus.fromCode(99));
        assertNull(StudyPlanStatus.fromCode(99));
    }
}
