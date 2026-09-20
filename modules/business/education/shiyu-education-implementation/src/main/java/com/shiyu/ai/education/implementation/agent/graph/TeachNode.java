package com.shiyu.ai.education.implementation.agent.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.implementation.domain.AbilityValue;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatMessage;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 执行 Teach 相关流程节点的输入处理和状态转移。
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("this-escape")
public class TeachNode extends BaseNode {

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * 执行 Teach 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param chatEngine 用于完成本次业务处理的 chatEngine 参数。
     */
    public TeachNode(ChatEngine chatEngine) {
        super();
        this.getConfig().setNodeType(NodeType.LLM_CALL);
        this.getConfig().setNodeName("teach");
        this.chatEngine = chatEngine;
    }

    /**
     * 执行 Teach 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 Teach 相关操作生成的结果数据。
     */
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
        long tenantId = requirePositiveLong(input, "tenantId");
        long userId = requirePositiveLong(input, "userId");
        ChatResponse resp =
                chatEngine.chat(
                        ChatRequest.builder()
                                .platform("default")
                                .tenantId(tenantId)
                                .userId(userId)
                                .messages(java.util.List.of(ChatMessage.text("user", prompt)))
                                .build());

        NodeOutput output = new NodeOutput();
        if (!resp.isSuccess()) {
            output.setSuccess(false);
            output.setMsg("LLM 教学调用失败，请稍后重试");
            output.addData("teachContent", "AI 教学服务暂时不可用，请稍后重试。");
        } else {
            output.setSuccess(true);
            output.setMsg("教学讲解成功");
            output.addData("teachContent", resp.getContent());
        }
        output.addData("teachDone", true);

        log.info(
                "TeachNode: 讲解完成, 内容长度={}",
                resp.getContent() != null ? resp.getContent().length() : 0);
        return output;
    }

    private String buildTeachPrompt(
            KnowledgeResponse knowledge,
            List<KnowledgeResponse> prerequisites,
            double overallScore,
            AbilityValue ability) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的 K12 教师，请根据以下信息为学生讲解知识点。\n\n");
        sb.append("## 当前知识点\n");
        sb.append("- 名称：").append(knowledge.name()).append("\n");
        if (knowledge.description() != null && !knowledge.description().isBlank()) {
            sb.append("- 描述：").append(knowledge.description()).append("\n");
        }
        sb.append("- 难度等级：")
                .append(knowledge.difficulty() != null ? knowledge.difficulty() : "未标注")
                .append("\n\n");

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

    private long requirePositiveLong(NodeInput input, String key) {
        Object value = input.getParameter(key);
        if (!(value instanceof Number number) || number.longValue() <= 0) {
            throw new IllegalArgumentException(key + " is required");
        }
        return number.longValue();
    }

    /**
     * 查询 Teach 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.previous("knowledge", "object", "知识点详情（由 AbilityQueryNode 传入）"),
                NodeInputParam.previous("prerequisites", "array", "前置知识点列表"),
                NodeInputParam.previous("ability", "object", "学生能力值"),
                NodeInputParam.previous("overallScore", "number", "总体掌握度"));
    }
}
