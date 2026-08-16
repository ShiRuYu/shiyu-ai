package com.shiyu.ai.web.education;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.education.dto.*;
import com.shiyu.ai.education.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能推荐控制器 — Phase 6
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "智能推荐")
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
@SaCheckPermission("edu:analytics")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ChatEngine chatEngine;

    @GetMapping("/knowledge")
    @Operation(summary = "推荐薄弱知识点 — 基于能力差距 + 遗忘紧迫度")
    public Result<List<KnowledgeRecommendResponse>> recommendKnowledge(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("推荐薄弱知识点 studentId={}, topK={}", studentId, topK);
        return Result.success(recommendationService.recommendKnowledge(studentId, topK));
    }

    @GetMapping("/questions")
    @Operation(summary = "推荐题目 — 基于薄弱知识点 + 难度匹配 + 能力维度")
    public Result<List<QuestionRecommendResponse>> recommendQuestions(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "10") int count) {
        log.info("推荐题目 studentId={}, count={}", studentId, count);
        return Result.success(recommendationService.recommendQuestions(studentId, count));
    }

    @GetMapping("/resources")
    @Operation(summary = "推荐学习资源 — 基于薄弱点 + 最近学习知识点")
    public Result<List<ResourceRecommendResponse>> recommendResources(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("推荐学习资源 studentId={}, topK={}", studentId, topK);
        return Result.success(recommendationService.recommendResources(studentId, topK));
    }

    @GetMapping("/review")
    @Operation(summary = "推荐复习任务 — 基于遗忘曲线的到期/即将到期复习")
    public Result<List<QuestionRecommendResponse>> recommendReview(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "5") int count) {
        log.info("推荐复习任务 studentId={}, count={}", studentId, count);
        return Result.success(recommendationService.recommendReviewTasks(studentId, count));
    }

    @GetMapping("/hybrid")
    @Operation(summary = "混合推荐 — 聚合知识点/题目/资源/复习 + AI 综合学习建议")
    public Result<HybridRecommendResponse> hybridRecommend(@RequestParam Long studentId) {
        log.info("混合推荐 studentId={}", studentId);
        List<Long> weakIds = recommendationService.getWeakKnowledgeIds(studentId);
        String advice = generateOverallAdvice(studentId, weakIds.size());
        return Result.success(recommendationService.hybridRecommend(studentId, advice));
    }

    private String generateOverallAdvice(Long studentId, int weakCount) {
        try {
            String prompt = """
                    你是一位 AI 学习规划师。学生的基本信息：
                    - 学生 ID: %s
                    - 薄弱知识点数量: %d
                    
                    请根据上述信息，给出综合学习建议（50-100字），包括：
                    1. 学习优先级建议
                    2. 薄弱点攻克策略
                    3. 复习安排建议
                    
                    要求：简洁有重点，带鼓励性语气。
                    """.formatted(studentId, weakCount);
            var resp = chatEngine.chat(ChatRequest.builder().platform("default")
                    .messages(List.of(ChatMessage.text("user", prompt))).build());
            if (resp.isSuccess() && resp.getContent() != null && !resp.getContent().isBlank()) {
                return resp.getContent();
            }
        } catch (Exception e) {
            log.warn("生成学习建议失败", e);
        }
        if (weakCount > 0) {
            return "你有 %d 个薄弱知识点需要加强，建议优先巩固薄弱点，再结合复习任务查漏补缺。加油！".formatted(weakCount);
        }
        return "暂无薄弱知识点，继续保持当前学习节奏！建议定期复习巩固已学知识点。";
    }
}
