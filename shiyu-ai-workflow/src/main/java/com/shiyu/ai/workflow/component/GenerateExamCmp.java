package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("generateExamCmp")
public class GenerateExamCmp extends NodeComponent {
    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("GenerateExamCmp: 生成试卷, studentId={}", ctx.getStudentId());
    }
}
