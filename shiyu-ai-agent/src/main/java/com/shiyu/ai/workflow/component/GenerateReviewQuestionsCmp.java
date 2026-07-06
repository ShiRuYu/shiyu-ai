package com.shiyu.ai.workflow.component;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 生成复习题组件
 */
@Slf4j
@Component("generateReviewQuestionsCmp")
@RequiredArgsConstructor
public class GenerateReviewQuestionsCmp extends NodeComponent {

    private final ChatEngine chatEngine;
    private final KnowledgeService knowledgeService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("GenerateReviewQuestionsCmp: 生成复习题, knowledgeId={}", ctx.getKnowledgeId());

        if (ctx.getKnowledgeId() == null) {
            log.warn("无待复习知识点");
            return;
        }

        String knowledgeName = "知识点";
        try {
            var k = knowledgeService.getById(ctx.getKnowledgeId());
            if (k != null) knowledgeName = k.name();
        } catch (Exception ignored) {}

        String prompt = "你是一位K12教师，请围绕知识点「" + knowledgeName + "」生成3道复习题，"
                + "包含选择题和填空题，给出答案。用JSON格式输出。";

        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());
        if (resp.isSuccess()) {
            log.info("复习题生成完成");
        }
    }
}
