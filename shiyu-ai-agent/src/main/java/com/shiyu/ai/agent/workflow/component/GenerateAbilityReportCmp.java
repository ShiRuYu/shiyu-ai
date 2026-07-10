package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.agent.workflow.context.AbilityContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("generateAbilityReportCmp")
@RequiredArgsConstructor
public class GenerateAbilityReportCmp extends NodeComponent {
    private final ChatEngine chatEngine;

    @Override
    public void process() throws Exception {
        AbilityContext ctx = this.getContextBean(AbilityContext.class);
        String prompt = "学生能力评估：总体掌握度" + ctx.getOverallMastery() + "%，"
                + "各维度：" + ctx.getAbilities()
                + "。请生成学习能力评估报告和改进建议。";
        String report = chatEngine.chat(ChatRequest.builder().prompt(prompt).build()).getContent();
        ctx.setReport(report);
        log.info("能力评估报告已生成");
    }
}