package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 课程 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    /**
     * 课程服务，表示当前对象中的对应属性。
     */
    private final CourseService courseService;

    /**
     * 查询 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:course:list")
    @GetMapping("/{id}")
    public Result<CourseResponse> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 课程 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 课程 相关操作生成的结果数据。
     */
    @SaCheckPermission("edu:course:list")
    @GetMapping
    public Result<PageData<CourseResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                courseService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * 查询 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param subject 用于完成本次业务处理的 subject 参数。
     */
    @SaCheckPermission("edu:course:list")
    @GetMapping("/subject")
    public Result<List<CourseResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(
                courseService.listBySubjectCode(
                        ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    /**
     * 查询 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param grade 用于完成本次业务处理的 grade 参数。
     */
    @SaCheckPermission("edu:course:list")
    @GetMapping("/grade")
    public Result<List<CourseResponse>> listByGrade(@RequestParam Integer grade) {
        return Result.success(
                courseService.listByGrade(ActorContextHttpAdapter.currentActor(), grade));
    }

    /**
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:course:create")
    public Result<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return Result.success(
                courseService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("edu:course:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        request.setId(id);
        courseService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param learn 用于完成本次业务处理的 learn 参数。
     */
    @SaCheckPermission("edu:course:list")
    @PostMapping("/learn")
    public Result<CourseResponse> startLearning(
            @RequestParam Long courseId, @RequestParam Long studentId) {
        CourseResponse course =
                courseService.getById(ActorContextHttpAdapter.currentActor(), courseId);
        return Result.success(course);
    }

    /**
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("edu:course:delete")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
