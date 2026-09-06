package com.shiyu.ai.education.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.request.StudentRequest;
import com.shiyu.ai.education.service.StudentService;
import com.shiyu.ai.kernel.context.ActorContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@SaCheckPermission("edu:student:list")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/list")
    public Result<PageData<StudentResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studentService.page(actor(), pageNum, pageSize));
    }

    @GetMapping("/detail")
    public Result<StudentResponse> getById(@RequestParam Long id) {
        return Result.success(studentService.getById(actor(), id));
    }

    @GetMapping("/user")
    public Result<StudentResponse> getByUserId(@RequestParam Long userId) {
        return Result.success(studentService.getByUserId(actor(), userId));
    }

    @PostMapping("/create")
    @SaCheckPermission("system:user:create")
    public Result<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return Result.success(studentService.create(actor(), request));
    }

    @PostMapping("/update")
    @SaCheckPermission("system:user:update")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody StudentRequest request) {
        request.setId(id);
        studentService.update(actor(), request);
        return Result.success();
    }

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
