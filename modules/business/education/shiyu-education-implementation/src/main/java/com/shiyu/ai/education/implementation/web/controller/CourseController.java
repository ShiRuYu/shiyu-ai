package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.CourseService;
import com.shiyu.ai.education.implementation.web.dto.CourseResponse;
import com.shiyu.ai.education.implementation.web.request.CourseRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code CourseController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@SaCheckPermission("edu:course:list")
public class CourseController {

    /**
     * 课程服务，表示当前对象中的对应属性。
     */
    private final CourseService courseService;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<CourseResponse> getById(@RequestParam Long id) {
        return Result.success(courseService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<PageData<CourseResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                courseService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * {@code listBySubjectCode} 查询并返回当前操作所需的数据。
     *
     * @param subjectCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/subject")
    public Result<List<CourseResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(
                courseService.listBySubjectCode(
                        ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    /**
     * {@code listByGrade} 查询并返回当前操作所需的数据。
     *
     * @param grade 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/grade")
    public Result<List<CourseResponse>> listByGrade(@RequestParam Integer grade) {
        return Result.success(
                courseService.listByGrade(ActorContextHttpAdapter.currentActor(), grade));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:course:create")
    public Result<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return Result.success(
                courseService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/update")
    @SaCheckPermission("edu:course:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody CourseRequest request) {
        request.setId(id);
        courseService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * {@code startLearning} 执行当前类型定义的业务操作。
     *
     * @param courseId 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/learn")
    public Result<CourseResponse> startLearning(
            @RequestParam Long courseId, @RequestParam Long studentId) {
        CourseResponse course =
                courseService.getById(ActorContextHttpAdapter.currentActor(), courseId);
        return Result.success(course);
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:course:delete")
    public Result<Void> delete(@RequestParam Long id) {
        courseService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
