package com.shiyu.ai.workflow.context;

import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.dal.dataobject.education.ResourceDO;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习流程上下文
 *
 * 在 LiteFlow learningChain 各组件间传递数据的状态对象。
 */
@Data
public class LearningContext {

    /** 学生 ID */
    private Long studentId;

    /** 知识点 ID */
    private Long knowledgeId;

    /** 知识点详情 */
    private KnowledgeResponse knowledge;

    /** 前置知识点列表 */
    private List<KnowledgeResponse> prerequisites = new ArrayList<>();

    /** 缺失的前置知识 ID 列表 */
    private List<Long> missingPrerequisiteIds = new ArrayList<>();

    /** 关联学习资源 */
    private List<ResourceDO> resources = new ArrayList<>();

    /** TeacherAgent 讲解内容 */
    private String teachResponse;

    /** 生成的练习题 */
    private List<QuestionDO> practiceQuestions = new ArrayList<>();

    /** 练习得分 (0~100) */
    private Double practiceScore;

    /** 练习准确率 (0~1) */
    private Double practiceAccuracy;

    /** 学习前能力值 */
    private AbilityValue beforeAbility;

    /** 学习后能力值 */
    private AbilityValue afterAbility;

    /** 安排的复习日期列表 */
    private List<LocalDate> reviewDates = new ArrayList<>();

    /** 总耗时（秒） */
    private Integer durationSec;
}
