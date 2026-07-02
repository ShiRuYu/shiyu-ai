package com.shiyu.ai.workflow.component;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.workflow.context.RecommendContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("generateRecommendationCmp")
@RequiredArgsConstructor
public class GenerateRecommendationCmp extends NodeComponent {
    private final ChatEngine chatEngine;

    @Override
    public void process() throws Exception {
        RecommendContext ctx = this.getContextBean(RecommendContext.class);
        if (ctx.getWeakKnowledgeIds().isEmpty()) {
            ctx.setRecommendation("暂无薄弱知识点，继续保持！");
            return;
        }
        String prompt = "学生有" + ctx.getWeakKnowledgeIds().size() + "个薄弱知识点需要加强，请给出学习建议。";
        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());
        ctx.setRecommendation(resp.isSuccess() ? resp.getContent() : prompt);
        log.info("推荐建议已生成");
    }
}