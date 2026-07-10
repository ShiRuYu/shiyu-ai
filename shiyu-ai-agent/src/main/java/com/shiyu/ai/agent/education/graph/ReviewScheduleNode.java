package com.shiyu.ai.agent.education.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.education.domain.ReviewScheduler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

/**
 * 复习安排节点
 *
 * LangGraph4j 节点，学习完成后安排艾宾浩斯遗忘曲线复习任务。
 *
 * 输入字段：studentId, knowledgeId
 * 输出字段：reviewTasks, reviewDates, reviewScheduled
 */
@Slf4j
@Getter
@Setter
public class ReviewScheduleNode extends BaseNode {

    @JsonIgnore
    private final ReviewScheduler reviewScheduler;
    @JsonIgnore
    private final ReviewService reviewService;

    public ReviewScheduleNode(ReviewScheduler reviewScheduler, ReviewService reviewService) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("reviewSchedule");
        this.reviewScheduler = reviewScheduler;
        this.reviewService = reviewService;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("ReviewScheduleNode: 安排复习");

        Long studentId = input.getParameter("studentId", null);
        Long knowledgeId = input.getParameter("knowledgeId", null);

        if (studentId == null || knowledgeId == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("缺少 studentId 或 knowledgeId");
            return err;
        }

        // 使用 ReviewScheduler 生成艾宾浩斯复习计划
        List<ReviewScheduler.ReviewTask> scheduledTasks = reviewScheduler.scheduleAfterLearning(
                studentId, knowledgeId, Instant.now());

        // 持久化复习任务
        List<ReviewTaskDO> savedTasks = scheduledTasks.stream()
                .map(task -> {
                    ReviewTaskDO rt = new ReviewTaskDO();
                    rt.setStudentId(task.studentId());
                    rt.setKnowledgeId(task.knowledgeId());
                    rt.setReviewDate(task.reviewDate());
                    rt.setReviewRound(task.reviewRound());
                    rt.setStatus("PENDING");
                    return reviewService.create(rt);
                })
                .toList();

        List<java.time.LocalDate> reviewDates = scheduledTasks.stream()
                .map(ReviewScheduler.ReviewTask::reviewDate)
                .toList();

        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("复习安排完成");
        output.addData("reviewTasks", savedTasks);
        output.addData("reviewDates", reviewDates);
        output.addData("reviewCount", savedTasks.size());
        output.addData("reviewScheduled", true);

        log.info("ReviewScheduleNode: 安排了 {} 轮复习", savedTasks.size());
        return output;
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.previous("studentId", "number", "学生 ID"),
            NodeInputParam.previous("knowledgeId", "number", "知识点 ID")
        );
    }
}
