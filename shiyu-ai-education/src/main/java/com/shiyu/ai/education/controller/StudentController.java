package com.shiyu.ai.education.controller;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.dataobject.education.StudentDO;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学生管理")
@RestController
@RequestMapping("/edu/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/list")
    @Operation(summary = "分页获取学生")
    public Result<PageData<StudentResponse>> listAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageData<StudentDO> page = studentService.page(pageNum, pageSize);
        List<StudentResponse> items = page.getItems().stream().map(this::toResponse).toList();
        return Result.success(new PageData<>(items, page.getTotal()));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取学生详情")
    public Result<StudentResponse> getById(@RequestParam Long id) {
        StudentDO student = studentService.getById(id);
        return Result.success(toResponse(student));
    }

    @GetMapping("/user")
    @Operation(summary = "根据用户ID获取学生")
    public Result<StudentResponse> getByUserId(@RequestParam Long userId) {
        StudentDO student = studentService.getByUserId(userId);
        return Result.success(toResponse(student));
    }

    @PostMapping("/create")
    @Operation(summary = "创建学生")
    public Result<StudentResponse> create(@Valid @RequestBody StudentDO student) {
        StudentDO created = studentService.create(student);
        return Result.success(toResponse(created));
    }

    @PostMapping("/update")
    @Operation(summary = "更新学生")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody StudentDO student) {
        student.setId(id);
        studentService.update(student);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除学生")
    public Result<Void> delete(@RequestParam Long id) {
        studentService.deleteById(id);
        return Result.success();
    }

    private StudentResponse toResponse(StudentDO student) {
        if (student == null) return null;
        return new StudentResponse(
                student.getId(), student.getUserId(), student.getStudentNo(),
                student.getName(), student.getGender(), student.getGrade(),
                student.getGradeLevel(), student.getSchool(), student.getClassName());
    }
}
