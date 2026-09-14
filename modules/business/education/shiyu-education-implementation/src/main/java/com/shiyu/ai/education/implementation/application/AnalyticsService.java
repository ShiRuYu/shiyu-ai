package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.StudyRecordResponse;
import com.shiyu.ai.education.implementation.web.request.StudyRecordRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * AnalyticsService 服务接口，负责执行教育领域相关业务操作。
 */
public interface AnalyticsService {

    /**
     * 查询记录按学生列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     *
     * @return 结果列表。
     */
    List<StudyRecordResponse> listRecordsByStudent(ActorContext actor, Long studentId);

    /**
     * 查询记录按学生知识列表。
     *
     * @param actor 调用方上下文。
     * @param studentId 学生标识。
     * @param knowledgeId knowledgeId 参数。
     *
     * @return 结果列表。
     */
    List<StudyRecordResponse> listRecordsByStudentAndKnowledge(
            ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * 创建记录。
     *
     * @param actor 调用方上下文。
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    StudyRecordResponse createRecord(ActorContext actor, StudyRecordRequest request);

    /** 获取能力雷达图数据 */
    com.shiyu.ai.education.implementation.web.dto.AbilityRadarResponse getAbilityRadar(
            ActorContext actor, Long studentId, Long knowledgeId);

    /** 获取学习概览 */
    com.shiyu.ai.education.implementation.web.dto.OverviewResponse getOverview(
            ActorContext actor, Long studentId);

    /** 获取薄弱知识点列表（掌握度 < 60） */
    java.util.List<com.shiyu.ai.education.implementation.web.dto.WeakPointResponse> getWeakPoints(
            ActorContext actor, Long studentId);

    /** 获取学习趋势（近7天学习量） */
    com.shiyu.ai.education.implementation.web.dto.TrendResponse getTrend(
            ActorContext actor, Long studentId);
}
