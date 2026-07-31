package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
import com.shiyu.ai.agent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 出题练习组件
 *
 * 调用 ChatEngine 根据知识点生成练习题。
 */
@Slf4j
@Component("practiceCmp")
@RequiredArgsConstructor
public class PracticeCmp extends NodeComponent {

    private final ChatEngine chatEngine;
    private final KnowledgePointService knowledgePointService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("PracticeCmp: AI 出题, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        KnowledgeResponse knowledge = knowledgePointService.getResponse(ctx.getKnowledgeId());
        String prompt = "你是一位K12出题教师，请围绕知识点「" +
                (knowledge != null ? knowledge.name() : "ID=" + ctx.getKnowledgeId()) +
                "」生成5道练习题，包含选择题和填空题，给出答案和解析。用JSON格式输出。";

        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());

        if (resp.isSuccess()) {
            ctx.setPracticeQuestions(java.util.Collections.emptyList());
            log.info("PracticeCmp: 题目生成完成");
        }
    }
}
