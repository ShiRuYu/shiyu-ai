package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 教育 Agent REST API
 *
 * 提供 AI 教学、智能出题、组卷、复习、规划、报告等 Agent 能力的 HTTP 接口。
 *
 * API 清单：
 * - POST /api/v1/agent/teacher     — TeacherAgent 讲解
 * - POST /api/v1/agent/practice    — PracticeAgent 出题
 * - POST /api/v1/agent/exam        — ExamAgent 组卷
 * - POST /api/v1/agent/review      — ReviewAgent 复习安排
 * - POST /api/v1/agent/review/complete — 完成复习
 * - POST /api/v1/agent/planner     — PlannerAgent 规划
 * - POST /api/v1/agent/report      — ReportAgent 报告
 */
@Tag(name = "教育 Agent")
@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class EducationAgentController {

    private final TeacherAgent teacherAgent;
    private final PracticeAgent practiceAgent;
    private final ExamAgent examAgent;
    private final ReviewAgent reviewAgent;
    private final PlannerAgent plannerAgent;
    private final ReportAgent reportAgent;

    // ==================== TeacherAgent ====================

    @PostMapping("/teacher")
    @Operation(summary = "TeacherAgent — AI 讲解知识点")
    public Result<TeacherAgent.TeachResponse> teach(
            @RequestParam Long studentId,
            @RequestParam Long knowledgeId) {
        TeacherAgent.TeachResponse response = teacherAgent.teach(studentId, knowledgeId);
        return Result.success(response);
    }

    // ==================== PracticeAgent ====================

    @PostMapping("/practice")
    @Operation(summary = "PracticeAgent — 智能生成练习题")
    public Result<List<QuestionDO>> generatePractice(
            @RequestParam Long studentId,
            @RequestParam Long knowledgeId,
            @RequestParam(defaultValue = "5") int count) {
        List<QuestionDO> questions = practiceAgent.generate(studentId, knowledgeId, count);
        return Result.success(questions);
    }

    // ==================== ExamAgent ====================

    @PostMapping("/exam")
    @Operation(summary = "ExamAgent — 智能组卷")
    public Result<ExamDO> generateExam(
            @RequestParam String subjectCode,
            @RequestParam Integer grade,
            @RequestParam List<Long> knowledgeIds,
            @RequestParam(defaultValue = "60") Integer durationMin,
            @RequestParam Long teacherId) {
        ExamDO exam = examAgent.generateExam(subjectCode, grade, knowledgeIds, durationMin, teacherId);
        return Result.success(exam);
    }

    // ==================== ReviewAgent ====================

    @PostMapping("/review")
    @Operation(summary = "ReviewAgent — 学习后安排复习")
    public Result<List<ReviewTaskDO>> scheduleReview(
            @RequestParam Long studentId,
            @RequestParam Long knowledgeId) {
        List<ReviewTaskDO> tasks = reviewAgent.scheduleAfterLearning(studentId, knowledgeId);
        return Result.success(tasks);
    }

    @GetMapping("/review/today")
    @Operation(summary = "ReviewAgent — 获取今日复习任务")
    public Result<List<ReviewTaskDO>> getTodayReviewTasks(
            @RequestParam Long studentId) {
        List<ReviewTaskDO> tasks = reviewAgent.getTodayTasks(studentId);
        return Result.success(tasks);
    }

    @PostMapping("/review/complete")
    @Operation(summary = "ReviewAgent — 完成复习任务")
    public Result<ReviewTaskDO> completeReview(
            @RequestParam Long taskId,
            @RequestParam Double score) {
        ReviewTaskDO task = reviewAgent.completeReview(taskId, score);
        return Result.success(task);
    }

    // ==================== PlannerAgent ====================

    @PostMapping("/planner")
    @Operation(summary = "PlannerAgent — 生成学习计划")
    public Result<StudyPlanDO> generatePlan(
            @RequestParam Long studentId,
            @RequestParam Long targetKnowledgeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        StudyPlanDO plan = plannerAgent.generatePlan(
                studentId, targetKnowledgeId,
                LocalDate.parse(startDate), LocalDate.parse(endDate));
        return Result.success(plan);
    }

    // ==================== ReportAgent ====================

    @PostMapping("/report")
    @Operation(summary = "ReportAgent — 学习报告")
    public Result<String> generateReport(
            @RequestParam Long studentId) {
        String report = reportAgent.generateOverviewReport(studentId);
        return Result.success(report);
    }
}
