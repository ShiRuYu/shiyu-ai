package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.education.domain.ExamType;
import com.shiyu.ai.education.service.ExamService;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ExamAgent — AI 智能组卷 Agent
 *
 * 职责：根据学科、年级、知识点范围，自动生成试卷并进行评分分析。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamAgent {

    private final ChatEngine chatEngine;
    private final KnowledgeService knowledgeService;
    private final ExamService examService;

    /**
     * 智能组卷
     *
     * @param subjectCode  学科编码
     * @param grade        年级
     * @param knowledgeIds 知识点范围
     * @param durationMin  考试时长（分钟）
     * @param teacherId    出卷教师 ID
     * @return 生成的试卷
     */
    public ExamDO generateExam(String subjectCode, Integer grade,
                                List<Long> knowledgeIds, Integer durationMin,
                                Long teacherId) {
        log.info("ExamAgent.generateExam: subject={}, grade={}, knowledgeIds={}",
                subjectCode, grade, knowledgeIds);

        // 1. 收集知识点详情
        List<String> knowledgeNames = new ArrayList<>();
        for (Long kid : knowledgeIds) {
            try {
                KnowledgeResponse k = knowledgeService.getById(kid);
                if (k != null) knowledgeNames.add(k.name());
            } catch (Exception e) {
                log.warn("ExamAgent: 知识点 {} 不存在", kid);
            }
        }

        // 2. 构建组卷 Prompt
        String prompt = buildExamPrompt(subjectCode, grade, knowledgeNames, durationMin);

        // 3. 调用 LLM 生成试卷
        ChatResponse resp = chatEngine.chat(ChatRequest.builder()
                .prompt(prompt)
                .build());

        if (!resp.isSuccess()) {
            log.error("ExamAgent: LLM 组卷失败: {}", resp.getErrorMessage());
            throw new RuntimeException("AI 组卷服务异常: " + resp.getErrorMessage());
        }

        // 4. 保存试卷元数据
        ExamDO exam = new ExamDO();
        exam.setSubjectCode(subjectCode);
        exam.setGrade(grade);
        exam.setTeacherId(teacherId);
        exam.setDurationMin(durationMin);
        exam.setType(ExamType.AI_GENERATED.getCode());
        exam.setStatus(1);
        exam.setTotalScore(100);
        exam.setName(subjectCode + " Grade " + grade + " 智能组卷");
        examService.create(exam);

        log.info("ExamAgent.generateExam: 组卷完成, examId={}", exam.getId());
        return exam;
    }

    private String buildExamPrompt(String subjectCode, Integer grade,
                                    List<String> knowledgeNames, Integer durationMin) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的 K12 出卷教师，请根据以下要求生成一份试卷。\n\n");
        sb.append("## 基本信息\n");
        sb.append("- 学科：").append(subjectCode).append("\n");
        sb.append("- 年级：").append(grade).append(" 年级\n");
        sb.append("- 考试时长：").append(durationMin).append(" 分钟\n");
        sb.append("- 总分：100 分\n\n");

        if (!knowledgeNames.isEmpty()) {
            sb.append("## 考察知识点\n");
            for (String name : knowledgeNames) {
                sb.append("- ").append(name).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 试卷结构\n");
        sb.append("1. 选择题（每题 3 分，共 10 题）—— 考察基础概念\n");
        sb.append("2. 填空题（每题 4 分，共 5 题）—— 考察理解与应用\n");
        sb.append("3. 解答题（每题 10 分，共 3 题）—— 考察综合分析能力\n\n");

        sb.append("## 注意事项\n");
        sb.append("1. 难度分布：基础题 40%，中等题 40%，难题 20%\n");
        sb.append("2. 每题给出参考答案和评分标准\n");
        sb.append("3. 请用中文出题\n");

        return sb.toString();
    }
}
