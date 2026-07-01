package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 生成复习题目组件（占位）
 * 将在 Phase 4 Step 3 完整实现。
 */
@Slf4j
@Component("generateReviewQuestionsCmp")
public class GenerateReviewQuestionsCmp extends NodeComponent {

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("GenerateReviewQuestionsCmp: 生成复习题目, knowledgeId={}", ctx.getKnowledgeId());
    }
}
