package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.agent.workflow.context.TutorContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tutorReportCmp")
@RequiredArgsConstructor
public class TutorReportCmp extends NodeComponent {
    private final ChatEngine chatEngine;

    @Override
    public void process() throws Exception {
        TutorContext ctx = this.getContextBean(TutorContext.class);
        String prompt = "学生学习完成，得分" + ctx.getPracticeScore() + "分，请给出学习总结和下一步建议。";
        String report = chatEngine.chat(ChatRequest.builder().prompt(prompt).build()).getContent();
        log.info("TutorReportCmp: 辅导报告生成完成");
    }
}