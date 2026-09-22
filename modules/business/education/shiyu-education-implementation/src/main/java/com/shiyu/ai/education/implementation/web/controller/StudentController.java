package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 学生 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    /**
     * 学生服务，表示当前对象中的对应属性。
     */
    private final StudentService studentService;

    /**
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 学生 相关操作生成的结果数据。
     */
    @SaCheckPermission("edu:student:list")
    @GetMapping
    public Result<PageData<StudentResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studentService.page(actor(), pageNum, pageSize));
    }

    /**
     * 查询 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @SaCheckPermission("edu:student:list")
    @GetMapping("/{id}")
    public Result<StudentResponse> getById(@PathVariable Long id) {
        return Result.success(studentService.getById(actor(), id));
    }

    /**
     * 查询 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param user 当前操作涉及的用户标识。
     */
    @SaCheckPermission("edu:student:list")
    @GetMapping("/user")
    public Result<StudentResponse> getByUserId(@RequestParam Long userId) {
        return Result.success(studentService.getByUserId(actor(), userId));
    }

    /**
     * 执行 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping
    @SaCheckPermission("edu:student:create")
    public Result<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return Result.success(studentService.create(actor(), request));
    }

    /**
     * 执行 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PutMapping("/{id}")
    @SaCheckPermission("edu:student:update")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        request.setId(id);
        studentService.update(actor(), request);
        return Result.success();
    }

    /**
     * 执行 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("edu:student:delete")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.deleteById(actor(), id);
        return Result.success();
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
