package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ExamTakingService;
import com.shiyu.ai.education.implementation.web.dto.ExamPaperResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamResultResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamSummaryResponse;
import com.shiyu.ai.education.implementation.web.request.SubmitExamRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** Student-only exam journey. */
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamTakingController {
    private final ExamTakingService taking;

    @GetMapping("/available")
    @SaCheckPermission("edu:exam:take")
    public Result<PageData<ExamResponse>> available(
            @RequestParam(required = false) String subjectCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(taking.available(ActorContextHttpAdapter.currentActor(),
                subjectCode, pageNum, pageSize));
    }

    @GetMapping("/available-summary")
    @SaCheckPermission("edu:exam:take")
    public Result<PageData<ExamSummaryResponse>> availableSummary(
            @RequestParam(required = false) String subjectCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(taking.availableSummaries(ActorContextHttpAdapter.currentActor(),
                subjectCode, pageNum, pageSize));
    }

    @GetMapping("/history")
    @SaCheckPermission("edu:exam:take")
    public Result<PageData<ExamSummaryResponse>> history(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(taking.history(ActorContextHttpAdapter.currentActor(),
                pageNum, pageSize));
    }

    @GetMapping("/{examId}/paper")
    @SaCheckPermission("edu:exam:take")
    public Result<ExamPaperResponse> paper(@PathVariable Long examId) {
        return Result.success(taking.paper(ActorContextHttpAdapter.currentActor(), examId));
    }

    @PostMapping("/{examId}/attempts")
    @SaCheckPermission("edu:exam:take")
    public Result<ExamPaperResponse> start(@PathVariable Long examId) {
        return Result.success(taking.start(ActorContextHttpAdapter.currentActor(), examId));
    }

    @GetMapping("/attempts/{attemptId}/draft")
    @SaCheckPermission("edu:exam:take")
    public Result<Map<Long, String>> draft(@PathVariable Long attemptId) {
        return Result.success(taking.draft(ActorContextHttpAdapter.currentActor(), attemptId));
    }

    @PutMapping("/attempts/{attemptId}/draft")
    @SaCheckPermission("edu:exam:take")
    public Result<Void> saveDraft(@PathVariable Long attemptId,
            @Valid @RequestBody SubmitExamRequest.Answer answer) {
        taking.saveDraft(ActorContextHttpAdapter.currentActor(), attemptId, answer);
        return Result.success();
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @SaCheckPermission("edu:exam:take")
    public Result<ExamResultResponse> submit(@PathVariable Long attemptId,
            @Valid @RequestBody SubmitExamRequest request) {
        return Result.success(taking.submit(ActorContextHttpAdapter.currentActor(), attemptId, request));
    }

    @GetMapping("/attempts/{attemptId}")
    @SaCheckPermission("edu:exam:take")
    public Result<ExamResultResponse> result(@PathVariable Long attemptId) {
        return Result.success(taking.result(ActorContextHttpAdapter.currentActor(), attemptId));
    }
}
