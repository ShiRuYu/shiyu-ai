package com.shiyu.ai.education.implementation.agent.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.ReviewScheduler;
import com.shiyu.ai.education.implementation.domain.enums.ReviewTaskStatus;
import com.shiyu.ai.education.implementation.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ReviewTaskRepository;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

/**
 * 执行 复习 Schedule 相关流程节点的输入处理和状态转移。
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("this-escape")
public class ReviewScheduleNode extends BaseNode {

    /**
     * reviewScheduler 属性，保存当前对象中的业务数据或协作依赖。
     */
    @JsonIgnore private final ReviewScheduler reviewScheduler;

    /**
     * reviewService 属性，保存当前对象中的业务数据或协作依赖。
     */
    @JsonIgnore private final ReviewService reviewService;

    /**
     * reviewTaskRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewTaskRepository reviewTaskRepository;

    /**
     * 执行 复习 Schedule 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param reviewScheduler 用于完成本次业务处理的 reviewScheduler 参数。
     * @param reviewService 用于完成本次业务处理的 reviewService 参数。
     * @param reviewTaskRepository 用于完成本次业务处理的 reviewTaskRepository 参数。
     */
    public ReviewScheduleNode(
            ReviewScheduler reviewScheduler,
            ReviewService reviewService,
            ReviewTaskRepository reviewTaskRepository) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("reviewSchedule");
        this.reviewScheduler = reviewScheduler;
        this.reviewService = reviewService;
        this.reviewTaskRepository = reviewTaskRepository;
    }

    /**
     * 执行 复习 Schedule 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 复习 Schedule 相关操作生成的结果数据。
     */
    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("ReviewScheduleNode: 安排复习");

        Long studentId = input.getParameter("studentId", null);
        Long knowledgeId = input.getParameter("knowledgeId", null);
        ActorContext actor = input.getParameter("__knowledgeAccessContext");
        if (actor == null) {
            throw new IllegalStateException("actor context is required");
        }

        if (studentId == null || knowledgeId == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("缺少 studentId 或 knowledgeId");
            return err;
        }

        // 使用 ReviewScheduler 生成艾宾浩斯复习计划
        List<ReviewScheduler.ReviewTask> scheduledTasks =
                reviewScheduler.scheduleAfterLearning(studentId, knowledgeId, Instant.now());

        // 持久化复习任务
        List<ReviewTaskBO> savedTasks =
                scheduledTasks.stream()
                        .map(
                                task -> {
                                    ReviewTaskBO rt = new ReviewTaskBO();
                                    rt.setStudentId(task.studentId());
                                    rt.setKnowledgeId(task.knowledgeId());
                                    rt.setReviewDate(task.reviewDate());
                                    rt.setReviewRound(task.reviewRound());
                                    rt.setStatus(ReviewTaskStatus.PENDING.getCode());
                                    reviewTaskRepository.insert(actor.tenantId(), rt);
                                    return rt;
                                })
                        .toList();

        List<java.time.LocalDate> reviewDates =
                scheduledTasks.stream().map(ReviewScheduler.ReviewTask::reviewDate).toList();

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

    /**
     * 查询 复习 Schedule 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.previous("studentId", "number", "学生 ID"),
                NodeInputParam.previous("knowledgeId", "number", "知识点 ID"));
    }
}
