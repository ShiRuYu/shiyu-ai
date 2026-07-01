package com.shiyu.ai.mcp.impl;

import com.shiyu.ai.aiagent.education.*;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 教育域 MCP 工具注册
 *
 * 在应用启动时，将教育 Agent 能力注册为 MCP 可调用工具。
 * 注册的工具列表：
 * - searchKnowledge   — 搜索知识点
 * - generateQuestion  — AI 出题
 * - getStudentAbility — 查询学生能力值
 * - scheduleReview    — 安排复习
 * - getLearningPath   — 获取学习路径
 * - teachKnowledge    — 讲解知识点
 * - generateExam      — 智能组卷
 * - generateReport    — 生成学习报告
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EducationToolRegistration {

    private final ToolServiceImpl toolService;
    private final TeacherAgent teacherAgent;
    private final PracticeAgent practiceAgent;
    private final ExamAgent examAgent;
    private final ReviewAgent reviewAgent;
    private final ReportAgent reportAgent;
    private final KnowledgeService knowledgeService;

    @PostConstruct
    public void registerEducationTools() {
        log.info("EducationToolRegistration: 开始注册教育域 MCP 工具");
        registerSearchKnowledge();
        registerGenerateQuestion();
        registerGetStudentAbility();
        registerScheduleReview();
        registerGetLearningPath();
        registerTeachKnowledge();
        registerGenerateExam();
        registerGenerateReport();
        log.info("EducationToolRegistration: 教育域 MCP 工具注册完成");
    }

    // ==================== 1. searchKnowledge ====================

    private void registerSearchKnowledge() {
        toolService.registerTool(
                "searchKnowledge",
                "搜索知识点，根据知识点 ID 获取详情",
                Map.of(
                        "knowledgeId", new ToolServiceImpl.ParameterDef("number", "知识点 ID", true)
                ),
                params -> {
                    Long id = ((Number) params.get("knowledgeId")).longValue();
                    return knowledgeService.getById(id);
                }
        );
    }

    // ==================== 2. generateQuestion ====================

    private void registerGenerateQuestion() {
        toolService.registerTool(
                "generateQuestion",
                "AI 智能出题，根据学生水平和知识点生成练习题",
                Map.of(
                        "studentId", new ToolServiceImpl.ParameterDef("number", "学生 ID", true),
                        "knowledgeId", new ToolServiceImpl.ParameterDef("number", "知识点 ID", true),
                        "count", new ToolServiceImpl.ParameterDef("number", "题目数量（默认 5）", false)
                ),
                params -> {
                    Long studentId = ((Number) params.get("studentId")).longValue();
                    Long knowledgeId = ((Number) params.get("knowledgeId")).longValue();
                    int count = params.get("count") != null
                            ? ((Number) params.get("count")).intValue() : 5;
                    return practiceAgent.generate(studentId, knowledgeId, count);
                }
        );
    }

    // ==================== 3. getStudentAbility ====================

    private void registerGetStudentAbility() {
        toolService.registerTool(
                "getStudentAbility",
                "查询学生在某知识点上的 Bloom 能力值",
                Map.of(
                        "studentId", new ToolServiceImpl.ParameterDef("number", "学生 ID", true),
                        "knowledgeId", new ToolServiceImpl.ParameterDef("number", "知识点 ID", true)
                ),
                params -> {
                    Long studentId = ((Number) params.get("studentId")).longValue();
                    Long knowledgeId = ((Number) params.get("knowledgeId")).longValue();
                    // AbilityService 在 education 模块，通过 TeacherAgent 间接获取
                    return teacherAgent.teach(studentId, knowledgeId).ability();
                }
        );
    }

    // ==================== 4. scheduleReview ====================

    private void registerScheduleReview() {
        toolService.registerTool(
                "scheduleReview",
                "学习后安排艾宾浩斯复习任务",
                Map.of(
                        "studentId", new ToolServiceImpl.ParameterDef("number", "学生 ID", true),
                        "knowledgeId", new ToolServiceImpl.ParameterDef("number", "知识点 ID", true)
                ),
                params -> {
                    Long studentId = ((Number) params.get("studentId")).longValue();
                    Long knowledgeId = ((Number) params.get("knowledgeId")).longValue();
                    return reviewAgent.scheduleAfterLearning(studentId, knowledgeId);
                }
        );
    }

    // ==================== 5. getLearningPath ====================

    private void registerGetLearningPath() {
        toolService.registerTool(
                "getLearningPath",
                "获取学习路径（前置知识拓扑排序）",
                Map.of(
                        "targetKnowledgeId", new ToolServiceImpl.ParameterDef("number", "目标知识点 ID", true)
                ),
                params -> {
                    Long targetId = ((Number) params.get("targetKnowledgeId")).longValue();
                    return knowledgeService.getGraph(targetId);
                }
        );
    }

    // ==================== 6. teachKnowledge ====================

    private void registerTeachKnowledge() {
        toolService.registerTool(
                "teachKnowledge",
                "AI 讲解知识点，返回教学内容和前置知识",
                Map.of(
                        "studentId", new ToolServiceImpl.ParameterDef("number", "学生 ID", true),
                        "knowledgeId", new ToolServiceImpl.ParameterDef("number", "知识点 ID", true)
                ),
                params -> {
                    Long studentId = ((Number) params.get("studentId")).longValue();
                    Long knowledgeId = ((Number) params.get("knowledgeId")).longValue();
                    return teacherAgent.teach(studentId, knowledgeId);
                }
        );
    }

    // ==================== 7. generateExam ====================

    private void registerGenerateExam() {
        toolService.registerTool(
                "generateExam",
                "AI 智能组卷，根据学科/年级/知识点生成试卷",
                Map.of(
                        "subjectCode", new ToolServiceImpl.ParameterDef("string", "学科编码（MATH/PHYSICS/ENGLISH等）", true),
                        "grade", new ToolServiceImpl.ParameterDef("number", "年级 1~12", true),
                        "knowledgeIds", new ToolServiceImpl.ParameterDef("array", "知识点 ID 列表", true),
                        "durationMin", new ToolServiceImpl.ParameterDef("number", "考试时长（分钟，默认 60）", false),
                        "teacherId", new ToolServiceImpl.ParameterDef("number", "出卷教师 ID", true)
                ),
                params -> {
                    String subjectCode = (String) params.get("subjectCode");
                    Integer grade = ((Number) params.get("grade")).intValue();
                    @SuppressWarnings("unchecked")
                    List<Number> ids = (List<Number>) params.get("knowledgeIds");
                    List<Long> knowledgeIds = ids.stream().map(Number::longValue).toList();
                    Integer durationMin = params.get("durationMin") != null
                            ? ((Number) params.get("durationMin")).intValue() : 60;
                    Long teacherId = ((Number) params.get("teacherId")).longValue();
                    return examAgent.generateExam(subjectCode, grade, knowledgeIds, durationMin, teacherId);
                }
        );
    }

    // ==================== 8. generateReport ====================

    private void registerGenerateReport() {
        toolService.registerTool(
                "generateReport",
                "生成学生学习报告及建议",
                Map.of(
                        "studentId", new ToolServiceImpl.ParameterDef("number", "学生 ID", true)
                ),
                params -> {
                    Long studentId = ((Number) params.get("studentId")).longValue();
                    return reportAgent.generateOverviewReport(studentId);
                }
        );
    }
}
