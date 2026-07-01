package com.shiyu.ai.workflow.component;

import com.shiyu.ai.aiagent.education.PracticeAgent;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PracticeAgent 出题练习组件
 *
 * 调用 PracticeAgent 根据知识点和学生水平生成练习题。
 */
@Slf4j
@Component("practiceCmp")
@RequiredArgsConstructor
public class PracticeCmp extends NodeComponent {

    private final PracticeAgent practiceAgent;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("PracticeCmp: AI 出题, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        // 生成 5 道练习题
        List<QuestionDO> questions = practiceAgent.generate(
                ctx.getStudentId(), ctx.getKnowledgeId(), 5);

        ctx.setPracticeQuestions(questions);
        log.info("PracticeCmp: 生成 {} 道题目", questions.size());
    }
}
