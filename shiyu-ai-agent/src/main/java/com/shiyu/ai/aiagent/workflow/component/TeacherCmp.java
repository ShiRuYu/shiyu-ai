package com.shiyu.ai.aiagent.workflow.component;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import com.shiyu.ai.aiagent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 讲解组件
 *
 * 通过 ChatEngine 对当前知识点进行 AI 讲解。
 */
@Slf4j
@Component("teacherCmp")
@RequiredArgsConstructor
public class TeacherCmp extends NodeComponent {

    private final ChatEngine chatEngine;
    private final KnowledgeService knowledgeService;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("TeacherCmp: AI 讲解, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        KnowledgeResponse knowledge = knowledgeService.getById(ctx.getKnowledgeId());
        String prompt = "你是一位经验丰富的K12教师，请讲解知识点：" +
                (knowledge != null ? knowledge.name() : "ID=" + ctx.getKnowledgeId());

        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());
        ctx.setTeachResponse(resp.isSuccess() ? resp.getContent() : "讲解服务暂不可用");

        log.info("TeacherCmp: 讲解完成");
    }
}
