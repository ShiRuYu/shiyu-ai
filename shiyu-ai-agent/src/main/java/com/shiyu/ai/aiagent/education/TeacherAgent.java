package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.education.ability.AbilityService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TeacherAgent — AI 讲解 Agent
 *
 * 职责：根据知识点、前置知识、学生能力值，调用 LLM 生成个性化的教学内容。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherAgent {

    private final ChatEngine chatEngine;
    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final AbilityService abilityService;

    /**
     * 对指定知识点进行 AI 讲解
     *
     * @param studentId   学生 ID
     * @param knowledgeId 知识点 ID
     * @return 教学响应，包含讲解内容、前置知识、能力值
     */
    public TeachResponse teach(Long studentId, Long knowledgeId) {
        log.info("TeacherAgent.teach: studentId={}, knowledgeId={}", studentId, knowledgeId);

        // 1. 获取知识点详情
        KnowledgeResponse knowledge = knowledgeService.getById(knowledgeId);
        if (knowledge == null) {
            throw new IllegalArgumentException("知识点不存在: " + knowledgeId);
        }

        // 2. 获取前置知识点
        List<KnowledgeResponse> prerequisites = knowledgeRelationService.getPrerequisites(knowledgeId);

        // 3. 获取学生当前能力值
        AbilityValue ability = abilityService.get(studentId, knowledgeId);

        // 4. 组装教学 Prompt
        String prompt = buildTeachPrompt(knowledge, prerequisites, ability);

        // 5. 调用 LLM
        ChatResponse resp = chatEngine.chat(ChatRequest.builder()
                .prompt(prompt)
                .build());

        if (!resp.isSuccess()) {
            log.error("TeacherAgent: LLM 调用失败: {}", resp.getErrorMessage());
            throw new RuntimeException("AI 教学服务异常: " + resp.getErrorMessage());
        }

        log.info("TeacherAgent.teach: 讲解完成, knowledgeId={}", knowledgeId);
        return new TeachResponse(knowledge, prerequisites, resp.getContent(), ability);
    }

    // ========== Prompt 构建 ==========

    private String buildTeachPrompt(KnowledgeResponse knowledge,
                                    List<KnowledgeResponse> prerequisites,
                                    AbilityValue ability) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的 K12 教师，请根据以下信息为学生讲解知识点。\n\n");
        sb.append("## 当前知识点\n");
        sb.append("- 名称：").append(knowledge.name()).append("\n");
        sb.append("- 编码：").append(knowledge.code()).append("\n");
        if (knowledge.description() != null && !knowledge.description().isBlank()) {
            sb.append("- 描述：").append(knowledge.description()).append("\n");
        }
        sb.append("- 难度等级：").append(knowledge.difficulty() != null ? knowledge.difficulty() : "未标注").append("\n\n");

        if (!prerequisites.isEmpty()) {
            sb.append("## 前置知识（学生已掌握）\n");
            for (KnowledgeResponse pre : prerequisites) {
                sb.append("- ").append(pre.name());
                if (pre.description() != null && !pre.description().isBlank()) {
                    sb.append("：").append(pre.description());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 学生当前水平\n");
        sb.append("- 总体掌握度：").append(String.format("%.1f%%", ability.overallScore())).append("\n");
        sb.append("- 记忆：").append(String.format("%.1f", ability.remember())).append("\n");
        sb.append("- 理解：").append(String.format("%.1f", ability.understand())).append("\n");
        sb.append("- 应用：").append(String.format("%.1f", ability.apply())).append("\n\n");

        sb.append("## 教学要求\n");
        sb.append("1. 用通俗易懂的语言讲解该知识点，注重概念和原理的阐述\n");
        sb.append("2. 结合前置知识，帮助学生建立知识关联\n");
        sb.append("3. 根据学生的掌握度调整讲解深度（薄弱环节重点讲解）\n");
        sb.append("4. 给出 1-2 个生活化的例子帮助学生理解\n");
        sb.append("5. 最后总结重点内容\n");
        sb.append("6. 请用中文回答\n");

        return sb.toString();
    }

    // ========== 响应 DTO ==========

    /**
     * 教学响应
     */
    public record TeachResponse(
            KnowledgeResponse knowledge,
            List<KnowledgeResponse> prerequisites,
            String content,
            AbilityValue ability
    ) {}
}
