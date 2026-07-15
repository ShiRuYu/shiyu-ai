package com.shiyu.ai.education.dto;

/**
 * 题目推荐响应
 *
 * @param questionId   题目 ID
 * @param title        题目标题
 * @param type         题目类型 (SINGLE_CHOICE / MULTIPLE_CHOICE / TRUE_FALSE / FILL_BLANK / SHORT_ANSWER)
 * @param difficulty   难度等级 (1-5)
 * @param knowledgeId  关联知识点 ID
 * @param knowledgeName 知识点名称
 * @param recommendType 推荐类型: WEAK_POINT_PRACTICE / DIFFICULTY_MATCH / RANDOM
 * @param reason       推荐理由
 * @param score        推荐评分 (0-100)，越高越推荐
 */
public record QuestionRecommendResponse(
        Long questionId,
        String title,
        String type,
        Integer difficulty,
        Long knowledgeId,
        String knowledgeName,
        String recommendType,
        String reason,
        Integer score
) {}
