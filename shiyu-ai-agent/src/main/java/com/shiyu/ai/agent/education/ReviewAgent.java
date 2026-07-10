package com.shiyu.ai.agent.education;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.education.domain.ReviewStatus;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.education.domain.ReviewScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewAgent — 艾宾浩斯复习 Agent
 *
 * 职责：根据遗忘曲线安排复习任务，管理复习进度。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAgent {

    private final ReviewScheduler reviewScheduler;
    private final ReviewService reviewService;

    /**
     * 学习完成后安排复习任务
     *
     * @param studentId   学生 ID
     * @param knowledgeId 知识点 ID
     * @return 安排的复习任务列表
     */
    public List<ReviewTaskDO> scheduleAfterLearning(Long studentId, Long knowledgeId) {
        log.info("ReviewAgent.scheduleAfterLearning: studentId={}, knowledgeId={}",
                studentId, knowledgeId);

        // 1. 使用 ReviewScheduler 生成复习计划
        List<ReviewScheduler.ReviewTask> tasks = reviewScheduler.scheduleAfterLearning(
                studentId, knowledgeId, Instant.now());

        // 2. 将复习计划持久化到数据库
        List<ReviewTaskDO> savedTasks = tasks.stream()
                .map(task -> {
                    ReviewTaskDO reviewTask = new ReviewTaskDO();
                    reviewTask.setStudentId(task.studentId());
                    reviewTask.setKnowledgeId(task.knowledgeId());
                    reviewTask.setReviewDate(task.reviewDate());
                    reviewTask.setReviewRound(task.reviewRound());
                    reviewTask.setStatus(ReviewStatus.PENDING.name());
                    return reviewService.create(reviewTask);
                })
                .toList();

        log.info("ReviewAgent.scheduleAfterLearning: 安排了 {} 轮复习任务", savedTasks.size());
        return savedTasks;
    }

    /**
     * 获取今日复习任务
     *
     * @param studentId 学生 ID
     * @return 今日应完成的复习任务
     */
    public List<ReviewTaskDO> getTodayTasks(Long studentId) {
        log.info("ReviewAgent.getTodayTasks: studentId={}", studentId);
        return reviewService.listTodayTasks(studentId);
    }

    /**
     * 完成复习任务
     *
     * @param taskId    任务 ID
     * @param score     得分
     * @return 更新后的任务
     */
    public ReviewTaskDO completeReview(Long taskId, Double score) {
        log.info("ReviewAgent.completeReview: taskId={}, score={}", taskId, score);

        ReviewTaskDO task = reviewService.getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("复习任务不存在: " + taskId);
        }

        task.setStatus(ReviewStatus.COMPLETED.name());
        task.setResultScore(score);
        task.setCompletedAt(java.time.LocalDateTime.now());
        reviewService.update(task);

        log.info("ReviewAgent.completeReview: 完成, taskId={}", taskId);
        return task;
    }

    /**
     * 检查是否有过期未完成的复习任务
     *
     * @param studentId 学生 ID
     * @return 过期任务列表
     */
    public List<ReviewTaskDO> getOverdueTasks(Long studentId) {
        log.info("ReviewAgent.getOverdueTasks: studentId={}", studentId);
        List<ReviewTaskDO> tasks = reviewService.listByStudentAndStatus(
                studentId, ReviewStatus.PENDING.name());
        return tasks.stream()
                .filter(t -> t.getReviewDate() != null
                        && t.getReviewDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
    }
}
