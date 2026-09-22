package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.ExamService;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 考试 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController {

    /**
     * 考试服务，表示当前对象中的对应属性。
     */
    private final ExamService examService;

    /**
     * 查询 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:exam:list")
    @GetMapping("/{id}")
    public Result<ExamResponse> getById(@PathVariable Long id) {
        return Result.success(examService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 考试 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 考试 相关操作生成的结果数据。
     */
    @SaCheckPermission("edu:exam:list")
    @GetMapping
    public Result<PageData<ExamResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                examService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * 查询 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param subject 用于完成本次业务处理的 subject 参数。
     */
    @SaCheckPermission("edu:exam:list")
    @GetMapping("/subject")
    public Result<List<ExamResponse>> listBySubjectCode(@RequestParam String subjectCode) {
        return Result.success(
                examService.listBySubjectCode(ActorContextHttpAdapter.currentActor(), subjectCode));
    }

    /**
     * 查询 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param teacher 用于完成本次业务处理的 teacher 参数。
     */
    @SaCheckPermission("edu:exam:list")
    @GetMapping("/teacher")
    public Result<List<ExamResponse>> listByTeacherId(@RequestParam Long teacherId) {
        return Result.success(
                examService.listByTeacherId(ActorContextHttpAdapter.currentActor(), teacherId));
    }

    /**
     * 执行 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:exam:create")
    public Result<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return Result.success(examService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("edu:exam:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        request.setId(id);
        examService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("edu:exam:delete")
    public Result<Void> delete(@PathVariable Long id) {
        examService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
