package com.shiyu.ai.education.contract.agent;

import com.shiyu.ai.agent.contract.node.NodeType;

/**
 * 集中定义教育模块支持的 Agent 节点类型。
 */
public final class EducationNodeTypes {

    public static final NodeType ABILITY_QUERY =
            NodeType.custom("ABILITY_QUERY", "能力值查询节点", "查询学生 Bloom 能力值和知识点详情");
    public static final NodeType EDUCATION_TEACH =
            NodeType.custom("EDUCATION_TEACH", "教学讲解节点", "AI 个性化教学讲解");
    public static final NodeType EDUCATION_PRACTICE =
            NodeType.custom("EDUCATION_PRACTICE", "教育出题节点", "根据知识点和学生水平生成练习题");
    public static final NodeType SCORE_ANALYSIS =
            NodeType.custom("SCORE_ANALYSIS", "评分分析节点", "对练习结果评分并更新能力值");
    public static final NodeType REVIEW_SCHEDULE =
            NodeType.custom("REVIEW_SCHEDULE", "复习安排节点", "艾宾浩斯遗忘曲线复习安排");
    public static final NodeType PREREQ_CHECK =
            NodeType.custom("PREREQ_CHECK", "前置知识检查节点", "检测学生对目标知识点缺失的前置知识");

    private EducationNodeTypes() {}
}
