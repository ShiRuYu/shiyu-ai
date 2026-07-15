package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.dal.bo.education.QuestionBO;
import com.shiyu.ai.education.domain.DifficultyLevel;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能出题节点
 *
 * LangGraph4j 节点，根据知识点和学生能力水平生成练习题。
 *
 * 输入字段：knowledge, knowledgeName, overallScore, studentId
 * 输出字段：practiceQuestions, questionCount, practiceDone
 */
@Slf4j
@Getter
@Setter
public class PracticeNode extends BaseNode {

    private final ChatEngine chatEngine;

    /** 默认出题数量 */
    private int defaultCount = 5;

    public PracticeNode(ChatEngine chatEngine) {
        super();
        this.getConfig().setNodeType(NodeType.LLM_CALL);
        this.getConfig().setNodeName("practice");
        this.chatEngine = chatEngine;
    }

    public PracticeNode(ChatEngine chatEngine, int defaultCount) {
        this(chatEngine);
        this.defaultCount = defaultCount;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("PracticeNode: 智能出题");

        @SuppressWarnings("unchecked")
        KnowledgeResponse knowledge = input.getParameter("knowledge", null);
        Double overallScore = input.getParameter("overallScore", 0.0);
        Integer count = input.getParameter("practiceCount", defaultCount);
        Long studentId = input.getParameter("studentId", null);

        if (knowledge == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("缺少 knowledge 上下文");
            return err;
        }

        // 能力值 → 难度映射
        DifficultyLevel difficulty = mapDifficulty(overallScore);

        // 构建 Prompt
        String prompt = buildPracticePrompt(knowledge, difficulty, count);

        // 调用 LLM
        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());

        NodeOutput output = new NodeOutput();
        if (!resp.isSuccess()) {
            output.setSuccess(false);
            output.setMsg("AI 出题失败: " + resp.getErrorMessage());
            output.addData("practiceQuestions", new ArrayList<QuestionBO>());
            output.addData("questionCount", 0);
        } else {
            // 解析 LLM 返回的 JSON 题目
            List<QuestionBO> questions = parseQuestions(resp.getContent(), knowledge, difficulty);
            output.setSuccess(true);
            output.setMsg("出题成功");
            output.addData("practiceQuestions", questions);
            output.addData("questionCount", questions.size());
        }
        output.addData("practiceDone", true);
        output.addData("difficultyLevel", difficulty.getLevel());
        output.addData("difficultyName", difficulty.getName());

        log.info("PracticeNode: 生成 {} 道 {} 难度的题目", output.getData("questionCount", 0), difficulty.getName());
        return output;
    }

    private String buildPracticePrompt(KnowledgeResponse knowledge,
                                       DifficultyLevel difficulty, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的 K12 出题教师，请根据以下要求生成练习题。\n\n");
        sb.append("## 知识点\n");
        sb.append("- 名称：").append(knowledge.name()).append("\n");
        if (knowledge.description() != null && !knowledge.description().isBlank()) {
            sb.append("- 描述：").append(knowledge.description()).append("\n");
        }
        sb.append("\n");
        sb.append("## 出题要求\n");
        sb.append("- 题目难度：").append(difficulty.getName()).append("\n");
        sb.append("- 题目数量：").append(count).append(" 道\n");
        sb.append("- 题目类型：选择题（60%）和填空题（40%）\n\n");
        sb.append("## 输出格式\n");
        sb.append("每行一个 JSON：\n");
        sb.append("{\"type\":\"CHOICE\",\"title\":\"题干\",\"options\":[\"A.\",\"B.\",\"C.\",\"D.\"],\"answer\":\"A\",\"analysis\":\"解析\",\"ability_dimension\":\"apply\"}\n");
        sb.append("{\"type\":\"FILL\",\"title\":\"题干___\",\"options\":null,\"answer\":\"答案\",\"analysis\":\"解析\",\"ability_dimension\":\"remember\"}\n");
        sb.append("仅输出 JSON 数据，用中文出题。\n");
        return sb.toString();
    }

    private DifficultyLevel mapDifficulty(double overallScore) {
        if (overallScore < 40) return DifficultyLevel.BASIC;
        if (overallScore < 70) return DifficultyLevel.MEDIUM;
        if (overallScore < 90) return DifficultyLevel.HARD;
        return DifficultyLevel.COMPETITION;
    }

    private List<QuestionBO> parseQuestions(String content, KnowledgeResponse knowledge,
                                            DifficultyLevel difficulty) {
        List<QuestionBO> questions = new ArrayList<>();
        for (String line : content.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("```")) continue;
            try {
                QuestionBO q = parseJsonLine(line, knowledge, difficulty);
                if (q != null) questions.add(q);
            } catch (Exception e) {
                log.warn("解析题目行失败: {}", e.getMessage());
            }
        }
        return questions;
    }

    private QuestionBO parseJsonLine(String json, KnowledgeResponse knowledge,
                                     DifficultyLevel difficulty) {
        if (!json.startsWith("{") || !json.endsWith("}")) return null;
        QuestionBO q = new QuestionBO();
        q.setDifficulty(difficulty.getLevel());
        q.setSubjectCode(parseSubjectCode(knowledge.code()));
        q.setGrade(8);
        q.setType(extract(json, "type"));
        q.setTitle(extract(json, "title"));
        q.setOptions(extract(json, "options"));
        q.setAnswer(extract(json, "answer"));
        q.setAnalysis(extract(json, "analysis"));
        q.setAbilityDimension(extract(json, "ability_dimension"));
        q.setTags("[\"" + knowledge.name() + "\"]");
        return q;
    }

    private String extract(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '[') {
            int end = json.indexOf(']', start);
            return end > start ? json.substring(start, end + 1) : null;
        }
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        }
        int end = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return end > start ? json.substring(start, end).trim() : null;
    }

    private String parseSubjectCode(String code) {
        if (code == null) return "MATH";
        String u = code.toUpperCase();
        if (u.startsWith("MATH") || u.startsWith("M")) return "MATH";
        if (u.startsWith("PHYS") || u.startsWith("P")) return "PHYSICS";
        if (u.startsWith("ENG") || u.startsWith("E")) return "ENGLISH";
        if (u.startsWith("CHN") || u.startsWith("CH")) return "CHINESE";
        return "MATH";
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.previous("knowledge", "object", "知识点详情"),
            NodeInputParam.previous("overallScore", "number", "总体掌握度（用于自动适配难度）"),
            NodeInputParam.apiOptional("practiceCount", "number", "题目数量（默认5）", 5)
        );
    }
}
