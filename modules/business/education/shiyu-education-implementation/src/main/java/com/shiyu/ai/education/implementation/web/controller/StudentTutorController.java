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

/** 面向已登录且有有效学生档案的用户提供受限的辅导接口。 */
@RestController
@RequestMapping("/student-tutor")
@RequiredArgsConstructor
@SaCheckLogin
public class StudentTutorController {
    private final StudentTutorService tutor;

    /** 返回已发布给当前学生所属租户的教育知识点。 */
    @GetMapping("/knowledge")
    public Result<List<KnowledgeResponse>> knowledge() {
        return Result.success(tutor.knowledge(ActorContextHttpAdapter.currentActor()));
    }

    /** 根据已选知识点为当前学生生成讲解。 */
    @PostMapping("/teach")
    public Result<StudentTutorResponse> teach(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.teach(ActorContextHttpAdapter.currentActor(), request));
    }

    /** 为当前学生生成不计分的练习建议。 */
    @PostMapping("/practice")
    public Result<StudentTutorResponse> practice(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.practice(ActorContextHttpAdapter.currentActor(), request));
    }

    /** 针对一个可见知识点生成学习计划。 */
    @PostMapping("/plan")
    public Result<StudentTutorResponse> plan(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.plan(ActorContextHttpAdapter.currentActor(), request));
    }

    /** 汇总当前学生的实际学习记录。 */
    @PostMapping("/report")
    public Result<StudentTutorResponse> report(@RequestBody StudentTutorRequest request) {
        return Result.success(tutor.report(ActorContextHttpAdapter.currentActor(), request));
    }
}
