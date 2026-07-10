package com.shiyu.ai.agent.education;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;
import com.shiyu.ai.dal.dataobject.education.StudyPlanItemDO;
import com.shiyu.ai.education.service.StudyPlanService;
import com.shiyu.ai.dal.repository.education.StudyPlanItemRepository;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * PlannerAgent — 学习计划 Agent
 *
 * 职责：根据学习目标和时间范围，自动生成学习计划。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlannerAgent {

    private final ChatEngine chatEngine;
    private final KnowledgeService knowledgeService;
    private final LearningPathService learningPathService;
    private final StudyPlanService studyPlanService;
    private final StudyPlanItemRepository studyPlanItemRepository;

    /**
     * 生成学习计划
     *
     * @param studentId         学生 ID
     * @param targetKnowledgeId 目标知识点 ID
     * @param startDate         开始日期
     * @param endDate           结束日期
     * @return 生成的学习计划
     */
    public StudyPlanDO generatePlan(Long studentId, Long targetKnowledgeId,
                                     LocalDate startDate, LocalDate endDate) {
        log.info("PlannerAgent.generatePlan: studentId={}, targetKnowledgeId={}",
                studentId, targetKnowledgeId);

        // 1. 获取知识点详情
        KnowledgeResponse target = knowledgeService.getById(targetKnowledgeId);
        if (target == null) {
            throw new IllegalArgumentException("目标知识点不存在: " + targetKnowledgeId);
        }

        // 2. 获取学习路径（前置知识拓扑排序）
        List<Long> path = learningPathService.generatePath(targetKnowledgeId);

        // 3. 构建学习计划
        StudyPlanDO plan = new StudyPlanDO();
        plan.setStudentId(studentId);
        plan.setTargetKnowledgeId(targetKnowledgeId);
        plan.setName("学习计划: " + target.name());
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setStatus("ACTIVE");
        StudyPlanDO savedPlan = studyPlanService.create(plan);

        // 4. 生成每日任务
        List<StudyPlanItemDO> items = generatePlanItems(
                savedPlan.getId(), path.size() > 0 ? path : List.of(targetKnowledgeId),
                startDate, endDate);
        // 保存每日任务（当前仅创建计划本身，items 持久化后续扩展）
        studyPlanItemRepository.insertBatch(items);
        log.info("PlannerAgent: {} 条计划明细已持久化", items.size());

        log.info("PlannerAgent.generatePlan: 计划创建完成, planId={}, totalDays={}",
                savedPlan.getId(), ChronoUnit.DAYS.between(startDate, endDate));
        return savedPlan;
    }

    /**
     * 根据总天数和知识点数量，生成按天分配的学习计划
     */
    private List<StudyPlanItemDO> generatePlanItems(Long planId, List<Long> knowledgeIds,
                                                      LocalDate startDate, LocalDate endDate) {
        List<StudyPlanItemDO> items = new ArrayList<>();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int knowledgeCount = knowledgeIds.size();
        int daysPerKnowledge = Math.max(1, (int) (totalDays / knowledgeCount));

        LocalDate currentDate = startDate;
        int orderNo = 0;
        for (Long knowledgeId : knowledgeIds) {
            StudyPlanItemDO item = new StudyPlanItemDO();
            item.setPlanId(planId);
            item.setKnowledgeId(knowledgeId);
            item.setPlanDate(currentDate);
            item.setOrderNo(orderNo++);
            item.setStatus("PENDING");
            items.add(item);

            // 跳到下一个知识点的计划日期
            if (currentDate.plusDays(daysPerKnowledge).isBefore(endDate)
                    || items.size() == knowledgeCount) {
                currentDate = currentDate.plusDays(daysPerKnowledge);
            }
        }
        return items;
    }
}
