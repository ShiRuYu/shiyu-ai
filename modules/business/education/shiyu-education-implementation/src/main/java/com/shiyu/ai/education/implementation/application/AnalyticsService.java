package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.StudyRecordResponse;
import com.shiyu.ai.education.implementation.web.request.StudyRecordRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 分析 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface AnalyticsService {

    /**
     * 查询 分析 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<StudyRecordResponse> listRecordsByStudent(ActorContext actor, Long studentId);

    /**
     * 查询 分析 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
