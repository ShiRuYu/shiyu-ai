package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.dal.bo.education.ReviewTaskBO;
import com.shiyu.ai.dal.repository.education.ReviewTaskRepository;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.agent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 加载复习任务组件
 *
 * 获取学生今日待复习的知识点列表。
 */
@Slf4j
@Component("loadReviewTaskCmp")
@RequiredArgsConstructor
public class LoadReviewTaskCmp extends NodeComponent {

    private final ReviewService reviewService;
    private final ReviewTaskRepository reviewTaskRepository;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("LoadReviewTaskCmp: 加载复习任务, studentId={}", ctx.getStudentId());

        List<ReviewTaskBO> tasks = reviewTaskRepository.selectTodayTasks(ctx.getStudentId());
        if (tasks.isEmpty()) {
            log.info("LoadReviewTaskCmp: 今日无待复习任务");
            return;
        }
        ctx.setReviewDates(tasks.stream().map(ReviewTaskBO::getReviewDate).toList());
        ctx.setKnowledgeId(tasks.get(0).getKnowledgeId());
        log.info("LoadReviewTaskCmp: 加载 {} 条复习任务", tasks.size());
    }
}
