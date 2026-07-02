package com.shiyu.ai.education.analytics;

import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;

import java.util.List;

/**
 * Analytics 接口
 */

public interface AnalyticsService {

    /**
     * List Records By Student
     * @return 处理结果
     */
    List<StudyRecordDO> listRecordsByStudent(Long studentId);

    /**
     * List Records By Student And Knowledge
     * @return 处理结果
     */
    List<StudyRecordDO> listRecordsByStudentAndKnowledge(Long studentId, Long knowledgeId);

    /**
     * Create Record
     * @param StudyRecordDO StudyRecordDO
     * @return 处理结果
     */
    StudyRecordDO createRecord(StudyRecordDO record);

    /** 获取能力雷达图数据 */
    com.shiyu.ai.education.dto.AbilityRadarResponse getAbilityRadar(Long studentId, Long knowledgeId);

    /** 获取学习概览 */
    com.shiyu.ai.education.dto.OverviewResponse getOverview(Long studentId);

    /** 获取薄弱知识点列表（掌握度 < 60） */
    java.util.List<com.shiyu.ai.education.dto.WeakPointResponse> getWeakPoints(Long studentId);

    /** 获取学习趋势（近7天学习量） */
    com.shiyu.ai.education.dto.TrendResponse getTrend(Long studentId);
}
