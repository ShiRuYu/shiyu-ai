package com.shiyu.ai.workflow.component;

import com.shiyu.ai.aiagent.education.ExamAgent;
import com.shiyu.ai.dal.dataobject.education.ExamDO;
import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 组卷组件
 */
@Slf4j
@Component("generateExamCmp")
@RequiredArgsConstructor
public class GenerateExamCmp extends NodeComponent {

    private final ExamAgent examAgent;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("GenerateExamCmp: 智能组卷, subjectCode=MATH, grade=7");

        ExamDO exam = examAgent.generateExam("MATH", 7,
                List.of(5L, 4L, 3L), 60, 1L);
        ctx.setKnowledgeId(exam.getId());
        log.info("GenerateExamCmp: 组卷完成, examId={}", exam.getId());
    }
}
