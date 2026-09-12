package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.StudentService;
import com.shiyu.ai.education.implementation.web.dto.StudentResponse;
import com.shiyu.ai.education.implementation.web.request.StudentRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

/**
 * {@code StudentController} 是教育模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@SaCheckPermission("edu:student:list")
public class StudentController {

    /**
     * 学生服务，表示当前对象中的对应属性。
     */
    private final StudentService studentService;

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/list")
    public Result<PageData<StudentResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studentService.page(actor(), pageNum, pageSize));
    }

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<StudentResponse> getById(@RequestParam Long id) {
        return Result.success(studentService.getById(actor(), id));
    }

    /**
     * {@code getByUserId} 查询并返回当前操作所需的数据。
     *
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/user")
    public Result<StudentResponse> getByUserId(@RequestParam Long userId) {
        return Result.success(studentService.getByUserId(actor(), userId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/create")
    @SaCheckPermission("system:user:create")
    public Result<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return Result.success(studentService.create(actor(), request));
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
    @SaCheckPermission("system:user:update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody StudentRequest request) {
        request.setId(id);
        studentService.update(actor(), request);
        return Result.success();
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/delete")
    @SaCheckPermission("system:user:delete")
    public Result<Void> delete(@RequestParam Long id) {
        studentService.deleteById(actor(), id);
        return Result.success();
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
