package com.shiyu.ai.aiagent.education.graph;

import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 教学讲解节点
 *
 * LangGraph4j 节点，从 AgentState 读取 knowledge/ability 等上下文，
 * 构建教学 Prompt 并调用 LLM 生成个性化教学内容。
 *
 * 输入字段：knowledge, knowledgeName, knowledgeDesc, prerequisites, ability, overallScore
 * 输出字段：teachContent, teachDone
 */
@Slf4j
@Getter
@Setter
public class TeachNode extends BaseNode {

    private final ChatEngine chatEngine;

    public TeachNode(ChatEngine chatEngine) {
        super();
        this.getConfig().setNodeType(NodeType.LLM_CALL);
        this.getConfig().setNodeName("teach");
        this.chatEngine = chatEngine;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("TeachNode: AI 讲解知识点");

        // 从 State 读取上下文
        @SuppressWarnings("unchecked")
        KnowledgeResponse knowledge = input.getParameter("knowledge", null);
        @SuppressWarnings("unchecked")
        List<KnowledgeResponse> prerequisites = input.getParameter("prerequisites", List.of());
        @SuppressWarnings("unchecked")
        AbilityValue ability = input.getParameter("ability", null);

        if (knowledge == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("缺少 knowledge 上下文，请确认 AbilityQueryNode 已先执行");
            return err;
        }

        double overallScore = ability != null ? ability.overallScore() : 0.0;

        // 构建 Prompt
        String prompt = buildTeachPrompt(knowledge, prerequisites, overallScore, ability);

        // 调用 LLM
        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());

        NodeOutput output = new NodeOutput();
        if (!resp.isSuccess()) {
            output.setSuccess(false);
            output.setMsg("LLM 教学调用失败: " + resp.getErrorMessage());
            output.addData("teachContent", "AI 教学服务暂时不可用，请稍后重试。");
        } else {
            output.setSuccess(true);
            output.setMsg("教学讲解成功");
            output.addData("teachContent", resp.getContent());
        }
        output.addData("teachDone", true);

        log.info("TeachNode: 讲解完成, 内容长度={}",
                resp.getContent() != null ? resp.getContent().length() : 0);
        return output;
    }

    private String buildTeachPrompt(KnowledgeResponse knowledge,
                                    List<KnowledgeResponse> prerequisites,
                                    double overallScore, AbilityValue ability) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的 K12 教师，请根据以下信息为学生讲解知识点。\n\n");
        sb.append("## 当前知识点\n");
        sb.append("- 名称：").append(knowledge.name()).append("\n");
        if (knowledge.description() != null && !knowledge.description().isBlank()) {
            sb.append("- 描述：").append(knowledge.description()).append("\n");
        }
        sb.append("- 难度等级：").append(knowledge.difficulty() != null ? knowledge.difficulty() : "未标注").append("\n\n");

        if (prerequisites != null && !prerequisites.isEmpty()) {
            sb.append("## 前置知识（学生已掌握）\n");
            for (KnowledgeResponse pre : prerequisites) {
                sb.append("- ").append(pre.name()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 学生当前水平\n");
        sb.append("- 总体掌握度：").append(String.format("%.1f%%", overallScore)).append("\n");
        if (ability != null) {
            sb.append("- 记忆：").append(String.format("%.1f", ability.remember())).append("\n");
            sb.append("- 理解：").append(String.format("%.1f", ability.understand())).append("\n");
            sb.append("- 应用：").append(String.format("%.1f", ability.apply())).append("\n");
        }
        sb.append("\n");

        sb.append("## 教学要求\n");
        sb.append("1. 用通俗易懂的语言讲解该知识点，注重概念和原理的阐述\n");
        sb.append("2. 结合前置知识，帮助学生建立知识关联\n");
        sb.append("3. 根据学生的掌握度调整讲解深度（薄弱环节重点讲解）\n");
        sb.append("4. 给出 1-2 个生活化的例子帮助学生理解\n");
        sb.append("5. 最后总结重点内容\n");
        sb.append("6. 请用中文回答\n");

        return sb.toString();
    }
}
