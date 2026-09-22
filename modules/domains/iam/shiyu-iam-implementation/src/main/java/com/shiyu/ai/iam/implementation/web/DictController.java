package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.request.DictPageRequest;
import com.shiyu.ai.iam.implementation.request.DictRequest;
import com.shiyu.ai.iam.implementation.service.DictService;
import com.shiyu.ai.iam.implementation.vo.DictVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 Dict 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Dict", description = "Dict")
@RestController
@RequestMapping("/api/iam/dicts")
public class DictController {

    /**
     * dictService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final DictService dictService;

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dictService 用于完成本次业务处理的 dictService 参数。
     */
    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param List 用于完成本次业务处理的 List 参数。
     */
    @Operation(summary = "Get Dict List")
    @SaCheckPermission("system:dict:list")
    @GetMapping
    public Result<PageData<DictVO>> getDictList(@Valid DictPageRequest request) {
        log.info("获取字典列表，pageNum: {}, pageSize: {}", request.getPageNum(), request.getPageSize());
        var result =
                dictService.pageView(
                        ActorContextHttpAdapter.currentActor(),
                        request.getPageNum(),
                        request.getPageSize());
        return Result.success(new PageData<>(result.getRight(), result.getLeft()));
    }

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Type 用于完成本次业务处理的 Type 参数。
     */
    @Operation(summary = "Get Dict By Type")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type")
    public Result<List<DictVO>> getDictByType(@RequestParam String dictType) {
        log.info("根据字典类型查询字典列表，dictType: {}", dictType);
        return Result.success(
                dictService.byTypeView(ActorContextHttpAdapter.currentActor(), dictType));
    }

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Dict 用于完成本次业务处理的 Dict 参数。
     */
    @Operation(summary = "Create Dict")
    @SaCheckPermission("system:dict:create")
    @PostMapping
    public Result<DictVO> createDict(@Valid @RequestBody DictRequest dictBO) {
        log.info("新增字典");
        return Result.success(dictService.create(ActorContextHttpAdapter.currentActor(), dictBO));
    }

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Dict 用于完成本次业务处理的 Dict 参数。
     */
    @Operation(summary = "Update Dict")
    @SaCheckPermission("system:dict:update")
    @PutMapping("/{id}")
    public Result<DictVO> updateDict(
            @PathVariable Long id, @Valid @RequestBody DictRequest dictBO) {
        log.info("修改字典，id: {}", id);
        return Result.success(
                dictService.update(ActorContextHttpAdapter.currentActor(), id, dictBO));
    }

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Dict 用于完成本次业务处理的 Dict 参数。
     */
    @Operation(summary = "Delete Dict")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        log.info("删除字典，id: {}", id);
        dictService.deleteById(ActorContextHttpAdapter.currentActor(), id);
        return Result.success();
    }

    /**
     * 执行 Dict 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Dicts 用于完成本次业务处理的 Dicts 参数。
     */
    @Operation(summary = "Delete Dicts")
    @SaCheckPermission("system:dict:delete")
    @PostMapping("/batch-delete")
    public Result<Void> deleteDicts(@RequestBody List<Long> ids) {
        log.info("批量删除字典，ids: {}", ids);
        dictService.deleteByIds(ActorContextHttpAdapter.currentActor(), ids);
        return Result.success();
    }
}
