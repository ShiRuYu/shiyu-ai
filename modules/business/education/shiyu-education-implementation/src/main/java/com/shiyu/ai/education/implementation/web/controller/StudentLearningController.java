package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.StudentLearningService;
import com.shiyu.ai.education.implementation.web.dto.CourseResponse;
import com.shiyu.ai.education.implementation.web.dto.CourseDetailResponse;
import com.shiyu.ai.education.implementation.web.dto.PracticeCheckResponse;
import com.shiyu.ai.education.implementation.web.dto.PracticeQuestionResponse;
import com.shiyu.ai.education.implementation.web.dto.PracticeSubmissionResponse;
import com.shiyu.ai.education.implementation.web.dto.WrongPracticeResponse;
import com.shiyu.ai.education.implementation.web.request.PracticeCheckRequest;
import com.shiyu.ai.education.implementation.web.request.PracticeSubmissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** 仅供学生使用的课程与练习接口。 */
@RestController
@RequestMapping("/student-learning")
@RequiredArgsConstructor
public class StudentLearningController {
    private final StudentLearningService learning;

    @SaCheckLogin
    @GetMapping("/courses")
    public Result<PageData<CourseResponse>> courses(
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) Integer grade,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(learning.courses(ActorContextHttpAdapter.currentActor(),
                subjectCode, grade, pageNum, pageSize));
    }

    @SaCheckLogin
    @GetMapping("/courses/{id}")
    public Result<CourseDetailResponse> course(@PathVariable Long id) {
        return Result.success(learning.course(ActorContextHttpAdapter.currentActor(), id));
    }

    @SaCheckLogin
    @GetMapping("/questions")
    public Result<List<PracticeQuestionResponse>> questions(
            @RequestParam String subjectCode,
            @RequestParam(required = false) Integer grade) {
        return Result.success(learning.questions(ActorContextHttpAdapter.currentActor(),
                subjectCode, grade));
    }

    @SaCheckLogin
    @GetMapping("/questions/{id}")
    public Result<PracticeQuestionResponse> question(@PathVariable Long id) {
        return Result.success(learning.question(ActorContextHttpAdapter.currentActor(), id));
    }

    /** 返回当前学生尚未解决的练习错题。 */
    @SaCheckLogin
    @GetMapping("/wrong-questions")
    public Result<List<WrongPracticeResponse>> wrongQuestions() {
        return Result.success(learning.wrongQuestions(ActorContextHttpAdapter.currentActor()));
    }

    @SaCheckLogin
    @PostMapping("/questions/{id}/check")
    public Result<PracticeCheckResponse> check(@PathVariable Long id,
            @Valid @RequestBody PracticeCheckRequest request) {
        return Result.success(learning.check(ActorContextHttpAdapter.currentActor(),
                id, request.answer()));
    }

    /** 记录一次已评分的学生练习作答；使用相同提交键重试时可安全复用结果。 */
    @SaCheckLogin
    @PostMapping("/questions/{id}/submissions")
    public Result<PracticeSubmissionResponse> submit(@PathVariable Long id,
            @Valid @RequestBody PracticeSubmissionRequest request) {
        return Result.success(learning.submit(ActorContextHttpAdapter.currentActor(), id,
                request.answer(), request.submissionKey()));
    }
}
