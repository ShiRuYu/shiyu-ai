package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ExamPublishingService;
import com.shiyu.ai.education.implementation.web.request.ExamCompositionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Administrative paper composition and publishing. */
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamPublishingController {
    private final ExamPublishingService publishing;

    @PutMapping("/{examId}/composition")
    @SaCheckPermission("edu:exam:edit")
    public Result<Void> replaceComposition(@PathVariable Long examId,
            @Valid @RequestBody ExamCompositionRequest request) {
        publishing.replaceComposition(ActorContextHttpAdapter.currentActor(), examId, request);
        return Result.success();
    }

    @PostMapping("/{examId}/publish")
    @SaCheckPermission("edu:exam:publish")
    public Result<Void> publish(@PathVariable Long examId) {
        publishing.publish(ActorContextHttpAdapter.currentActor(), examId);
        return Result.success();
    }
}
