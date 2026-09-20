package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeAuditService;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeAuditResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理 知识 Audit 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/audits")
@RequiredArgsConstructor
@Tag(name = "知识平台审计")
@SaCheckPermission("knowledge:list")
public class KnowledgeAuditController {

    /**
     * 审计服务，表示当前对象中的对应属性。
     */
    private final KnowledgeAuditService auditService;

    /**
     * 查询 知识 Audit 相关业务数据，并返回处理结果。
     *
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @return 返回 知识 Audit 相关操作生成的结果数据。
     */
    @GetMapping
    public Result<PageData<KnowledgeAuditResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long spaceId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                auditService.page(
                        ActorContextHttpAdapter.currentActor(),
                        pageNum,
                        Math.min(pageSize, 100),
                        spaceId));
    }
}
