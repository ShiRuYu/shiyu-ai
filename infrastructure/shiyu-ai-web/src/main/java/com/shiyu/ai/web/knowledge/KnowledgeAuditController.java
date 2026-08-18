package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.api.response.KnowledgeAuditResponse;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/knowledge/audits")
@RequiredArgsConstructor
@Tag(name = "知识平台审计")
@SaCheckPermission("knowledge:list")
public class KnowledgeAuditController {

    private final KnowledgeAuditService auditService;

    @GetMapping
    public Result<PageData<KnowledgeAuditResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long spaceId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(auditService.page(pageNum, Math.min(pageSize, 100), spaceId));
    }
}
