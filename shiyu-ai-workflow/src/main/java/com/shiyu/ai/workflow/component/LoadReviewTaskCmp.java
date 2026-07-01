package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 加载复习任务组件（占位）
 *
 * 获取学生今日待复习的知识点列表。
 * 将在 Phase 4 Step 3 完整实现。
 */
@Slf4j
@Component("loadReviewTaskCmp")
public class LoadReviewTaskCmp extends NodeComponent {

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("LoadReviewTaskCmp: 加载复习任务, studentId={}", ctx.getStudentId());
        // TODO: 从 ReviewService 加载今日复习任务
    }
}
