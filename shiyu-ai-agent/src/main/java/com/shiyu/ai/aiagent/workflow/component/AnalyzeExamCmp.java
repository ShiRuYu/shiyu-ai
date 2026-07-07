package com.shiyu.ai.aiagent.workflow.component;

import com.shiyu.ai.aiagent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 考试分析组件
 */
@Slf4j
@Component("analyzeExamCmp")
public class AnalyzeExamCmp extends NodeComponent {

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("AnalyzeExamCmp: 考试分析, score={}", ctx.getPracticeScore());
        log.info("AnalyzeExamCmp: 分析完成");
    }
}
