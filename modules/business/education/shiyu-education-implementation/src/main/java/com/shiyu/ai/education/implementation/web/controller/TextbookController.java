package com.shiyu.ai.education.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.TextbookService;
import com.shiyu.ai.education.implementation.web.dto.TextbookResponse;
import com.shiyu.ai.education.implementation.web.request.TextbookRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 教材 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@RestController
@RequestMapping("/textbook")
@RequiredArgsConstructor
@SaCheckPermission("edu:textbook:list")
public class TextbookController {

    /**
     * textbookService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TextbookService textbookService;

    /**
     * 查询 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    @GetMapping("/detail")
    public Result<TextbookResponse> getById(@RequestParam Long id) {
        return Result.success(textbookService.getById(ActorContextHttpAdapter.currentActor(), id));
    }

    /**
     * 查询 教材 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回 教材 相关操作生成的结果数据。
     */
    @GetMapping("/list")
    public Result<PageData<TextbookResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(
                textbookService.page(ActorContextHttpAdapter.currentActor(), pageNum, pageSize));
    }

    /**
     * 查询 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param grade 用于完成本次业务处理的 grade 参数。
     */
    @GetMapping("/subject-grade")
    public Result<List<TextbookResponse>> listBySubjectAndGrade(
            @RequestParam String subjectCode, @RequestParam Integer grade) {
        return Result.success(
                textbookService.listBySubjectAndGrade(
                        ActorContextHttpAdapter.currentActor(), subjectCode, grade));
    }

    /**
     * 执行 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @PostMapping("/create")
    @SaCheckPermission("edu:textbook:create")
    public Result<TextbookResponse> create(@Valid @RequestBody TextbookRequest request) {
        return Result.success(
                textbookService.create(ActorContextHttpAdapter.currentActor(), request));
    }

    /**
     * 执行 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @PostMapping("/update")
    @SaCheckPermission("edu:textbook:edit")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody TextbookRequest request) {
        request.setId(id);
        textbookService.update(ActorContextHttpAdapter.currentActor(), request);
        return Result.success();
    }

    /**
     * 执行 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @PostMapping("/delete")
    @SaCheckPermission("edu:textbook:delete")
    public Result<Void> delete(@RequestParam Long id) {
        textbookService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }
}
