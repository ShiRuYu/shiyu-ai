package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.education.ability.AbilityService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.DifficultyLevel;
import com.shiyu.ai.education.question.QuestionService;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * PracticeAgent — AI 智能出题 Agent
 *
 * 职责：根据知识点和学生能力水平，调用 LLM 自动生成个性化练习题。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PracticeAgent {

    private final ChatEngine chatEngine;
    private final KnowledgeService knowledgeService;
    private final AbilityService abilityService;
    private final QuestionService questionService;

    /**
     * 为指定知识点生成练习题
     *
     * @param studentId   学生 ID（用于评估难度）
     * @param knowledgeId 知识点 ID
     * @param count       题目数量
     * @return 生成的题目列表（已保存到数据库）
     */
    public List<QuestionDO> generate(Long studentId, Long knowledgeId, int count) {
        log.info("PracticeAgent.generate: studentId={}, knowledgeId={}, count={}",
                studentId, knowledgeId, count);

        // 1. 获取知识点
        KnowledgeResponse knowledge = knowledgeService.getById(knowledgeId);
        if (knowledge == null) {
            throw new IllegalArgumentException("知识点不存在: " + knowledgeId);
        }

        // 2. 获取学生能力值，映射难度
        AbilityValue ability = abilityService.get(studentId, knowledgeId);
        DifficultyLevel difficulty = mapDifficulty(ability.overallScore());

        // 3. 组装 Prompt
        String prompt = buildPracticePrompt(knowledge, difficulty, count);

        // 4. 调用 LLM
        ChatResponse resp = chatEngine.chat(ChatRequest.builder()
                .prompt(prompt)
                .build());

        if (!resp.isSuccess()) {
            log.error("PracticeAgent: LLM 调用失败: {}", resp.getErrorMessage());
            throw new RuntimeException("AI 出题服务异常: " + resp.getErrorMessage());
        }

        // 5. 解析 LLM 返回的题目并保存
        List<QuestionDO> questions = parseQuestions(resp.getContent(), knowledge, difficulty);
        for (QuestionDO q : questions) {
            questionService.create(q);
        }

        log.info("PracticeAgent.generate: 成功生成 {} 道题目, knowledgeId={}", questions.size(), knowledgeId);
        return questions;
    }

    // ========== Prompt 构建 ==========

    private String buildPracticePrompt(KnowledgeResponse knowledge,
                                       DifficultyLevel difficulty,
                                       int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的 K12 出题教师，请根据以下要求生成练习题。\n\n");
        sb.append("## 知识点\n");
        sb.append("- 名称：").append(knowledge.name()).append("\n");
        sb.append("- 编码：").append(knowledge.code()).append("\n");
        if (knowledge.description() != null && !knowledge.description().isBlank()) {
            sb.append("- 描述：").append(knowledge.description()).append("\n");
        }
        sb.append("\n");

        sb.append("## 出题要求\n");
        sb.append("- 题目难度：").append(difficulty.getName()).append("\n");
        sb.append("- 题目数量：").append(count).append(" 道\n");
        sb.append("- 题目类型：选择题（占 60%）和填空题（占 40%）\n\n");

        sb.append("## 输出格式\n");
        sb.append("每道题目请严格按照以下 JSON 格式输出，每行一个 JSON 对象：\n\n");
        sb.append("```\n");
        sb.append("{\"type\":\"CHOICE\",\"title\":\"题目标题\",\"options\":[\"A. 选项A\",\"B. 选项B\",\"C. 选项C\",\"D. 选项D\"],\"answer\":\"A\",\"analysis\":\"解析\", \"ability_dimension\":\"apply\"}\n");
        sb.append("{\"type\":\"FILL\",\"title\":\"填空题题干___\",\"options\":null,\"answer\":\"标准答案\",\"analysis\":\"解析\", \"ability_dimension\":\"remember\"}\n");
        sb.append("```\n\n");

        sb.append("## 注意事项\n");
        sb.append("1. 选择题请提供 4 个选项\n");
        sb.append("2. 答案必须准确无误\n");
        sb.append("3. 每题附带详细解析\n");
        sb.append("4. ability_dimension 可选值：remember(记忆), understand(理解), apply(应用), analyze(分析)\n");
        sb.append("5. 请用中文出题\n");
        sb.append("6. 仅输出 JSON 数据，不要输出其他文字\n");

        return sb.toString();
    }

    // ========== 工具方法 ==========

    /**
     * 将能力总体得分映射为难度级别
     */
    private DifficultyLevel mapDifficulty(double overallScore) {
        if (overallScore < 40) return DifficultyLevel.BASIC;
        if (overallScore < 70) return DifficultyLevel.MEDIUM;
        if (overallScore < 90) return DifficultyLevel.HARD;
        return DifficultyLevel.COMPETITION;
    }

    /**
     * 解析 LLM 返回的 JSON 格式题目文本
     */
    private List<QuestionDO> parseQuestions(String content, KnowledgeResponse knowledge,
                                            DifficultyLevel difficulty) {
        List<QuestionDO> questions = new ArrayList<>();
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("```")) continue;
            try {
                // 简单 JSON 解析 — 实际生产推荐使用 Jackson
                QuestionDO q = parseJsonLine(line, knowledge, difficulty);
                if (q != null) {
                    questions.add(q);
                }
            } catch (Exception e) {
                log.warn("PracticeAgent: 解析题目行失败, line={}, error={}", line, e.getMessage());
            }
        }
        return questions;
    }

    private QuestionDO parseJsonLine(String json, KnowledgeResponse knowledge,
                                     DifficultyLevel difficulty) {
        if (!json.startsWith("{") || !json.endsWith("}")) return null;

        QuestionDO q = new QuestionDO();
        q.setSubjectCode(parseSubjectCode(knowledge.code()));
        q.setGrade(8); // 默认年级，可根据知识点编码推断
        q.setDifficulty(difficulty.getLevel());
        q.setStatus(1);

        // 简易 JSON 字段提取（不依赖 Jackson，保持轻量）
        q.setType(extractJsonString(json, "type"));
        q.setTitle(extractJsonString(json, "title"));
        q.setOptions(extractJsonString(json, "options"));
        q.setAnswer(extractJsonString(json, "answer"));
        q.setAnalysis(extractJsonString(json, "analysis"));
        q.setAbilityDimension(extractJsonString(json, "ability_dimension"));
        q.setTags("[\"" + knowledge.name() + "\"]");

        return q;
    }

    private String extractJsonString(String json, String key) {
        int keyIdx = json.indexOf("\"" + key + "\"");
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(':', keyIdx);
        if (colonIdx < 0) return null;
        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;
        // 如果是对象/数组，提取括号内容
        if (json.charAt(start) == '[') {
            int end = json.indexOf(']', start);
            return end > start ? json.substring(start, end + 1) : null;
        }
        // 如果是字符串，提取引号内容
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        }
        // 如果是裸值（数字等）
        int end = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return end > start ? json.substring(start, end).trim() : null;
    }

    /**
     * 从知识点编码中推断学科编码
     */
    private String parseSubjectCode(String knowledgeCode) {
        if (knowledgeCode == null) return "MATH";
        String upper = knowledgeCode.toUpperCase();
        if (upper.startsWith("MATH") || upper.startsWith("M")) return "MATH";
        if (upper.startsWith("PHYS") || upper.startsWith("P")) return "PHYSICS";
        if (upper.startsWith("ENG") || upper.startsWith("E")) return "ENGLISH";
        if (upper.startsWith("CHN") || upper.startsWith("CH")) return "CHINESE";
        return "MATH";
    }
}
