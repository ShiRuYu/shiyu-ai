package com.shiyu.ai.aiagent.workflow.component;

import com.shiyu.ai.aiagent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 批改试卷组件
 */
@Slf4j
@Component("scoreExamCmp")
public class ScoreExamCmp extends NodeComponent {

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("ScoreExamCmp: 批改试卷");
        ctx.setPracticeScore(75.0);
        ctx.setPracticeAccuracy(0.75);
    }
}
