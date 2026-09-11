package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.StudyRecordResponse;
import com.shiyu.ai.education.implementation.web.request.StudyRecordRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/** Analytics 接口 */
public interface AnalyticsService {

    /**
     * List Records By Student
     *
     * @return 处理结果
     */
    List<StudyRecordResponse> listRecordsByStudent(ActorContext actor, Long studentId);

    /**
     * List Records By Student And Knowledge
     *
     * @return 处理结果
     */
    List<StudyRecordResponse> listRecordsByStudentAndKnowledge(
            ActorContext actor, Long studentId, Long knowledgeId);

    /**
     * Create Record
     *
     * @param StudyRecordBO StudyRecordDO
     * @return 处理结果
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
