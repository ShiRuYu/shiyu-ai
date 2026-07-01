package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("analyzeExamCmp")
public class AnalyzeExamCmp extends NodeComponent {
    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("AnalyzeExamCmp: 考试分析, studentId={}", ctx.getStudentId());
    }
}
