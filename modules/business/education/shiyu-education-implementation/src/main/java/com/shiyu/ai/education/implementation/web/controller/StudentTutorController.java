package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.StudentTutorService;
import com.shiyu.ai.education.implementation.web.dto.StudentTutorResponse;
import com.shiyu.ai.education.implementation.web.request.StudentTutorRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Bounded tutor endpoints available to logged-in users with an active student profile. */
@RestController
@RequestMapping("/student-tutor")
@RequiredArgsConstructor
@SaCheckLogin
public class StudentTutorController {
    private final StudentTutorService tutor;

    /** Returns education points published to the current student's tenant. */
    @GetMapping("/knowledge")
    public Result<List<KnowledgeResponse>> knowledge() {
        return Result.success(tutor.knowledge(ActorContextHttpAdapter.currentActor()));
    }

    /** Generates a grounded lesson for the current student. */
    @PostMapping("/teach")
    public Result<StudentTutorResponse> teach(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.teach(ActorContextHttpAdapter.currentActor(), request));
    }

    /** Generates non-graded practice suggestions for the current student. */
    @PostMapping("/practice")
    public Result<StudentTutorResponse> practice(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.practice(ActorContextHttpAdapter.currentActor(), request));
    }

    /** Generates a study plan for a visible point. */
    @PostMapping("/plan")
    public Result<StudentTutorResponse> plan(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.plan(ActorContextHttpAdapter.currentActor(), request));
    }

    /** Summarizes the current student's real records. */
    @PostMapping("/report")
    public Result<StudentTutorResponse> report(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.report(ActorContextHttpAdapter.currentActor(), request));
    }
}
