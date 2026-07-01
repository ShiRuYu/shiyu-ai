package com.shiyu.ai.workflow.component;

import com.shiyu.ai.aiagent.education.TeacherAgent;
import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TeacherAgent 讲解组件
 *
 * 调用 TeacherAgent 对当前知识点进行 AI 讲解。
 */
@Slf4j
@Component("teacherCmp")
@RequiredArgsConstructor
public class TeacherCmp extends NodeComponent {

    private final TeacherAgent teacherAgent;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("TeacherCmp: AI 讲解, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        TeacherAgent.TeachResponse response = teacherAgent.teach(
                ctx.getStudentId(), ctx.getKnowledgeId());

        ctx.setTeachResponse(response.content());
        log.info("TeacherCmp: 讲解完成, 内容长度={}",
                response.content() != null ? response.content().length() : 0);
    }
}
