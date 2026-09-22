package com.shiyu.ai.model.implementation.application.service.impl;

import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.model.implementation.application.assembler.AiPlatformAssembler;
import com.shiyu.ai.model.implementation.application.service.AiPlatformService;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.domain.model.PlatformAdapterType;
import com.shiyu.ai.model.implementation.domain.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.implementation.infrastructure.config.PlatformProperties;
import com.shiyu.ai.model.implementation.web.request.AiPlatformRequest;
import com.shiyu.ai.model.implementation.web.response.AiPlatformResponse;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提供 AI 平台 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class AiPlatformServiceImpl implements AiPlatformService {

    /**
     * 按名称和编码条件分页查询当前租户的 AI 平台，并将领域对象转换为响应数据。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param pageNo 页码，从 1 开始。
     * @param pageSize 每页返回的平台数量。
     * @param name 可选的平台名称关键字。
     * @param code 可选的平台唯一编码。
     *
     * @return 左值为平台总数，右值为当前页的平台响应列表。
     */
    @Override
    public Pair<Long, List<AiPlatformResponse>> pageResponse(
            ActorContext actor, Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result = getPageBO(actor, pageNo, pageSize, name, code);
        return Pair.of(
                result.getLeft(),
                result.getRight().stream().map(AiPlatformAssembler::toResponse).toList());
    }

    /**
     * 查询当前租户下已启用的全部 AI 平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     *
     * @return 已启用的平台响应列表。
     */
    @Override
    public List<AiPlatformResponse> enabledResponse(ActorContext actor) {
        return getAllEnabledBO(actor).stream().map(AiPlatformAssembler::toResponse).toList();
    }

    /**
     * 查询指定 AI 平台的详细信息。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 平台 ID。
     *
     * @return 指定平台的响应数据。
     */
    @Override
    public AiPlatformResponse detailResponse(ActorContext actor, Long id) {
        return AiPlatformAssembler.toResponse(getByIdBO(actor, id));
    }

    /**
     * 根据平台编码查询 AI 平台详情。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param code 平台唯一编码。
     *
     * @return 对应平台的响应数据。
     */
    @Override
    public AiPlatformResponse codeResponse(ActorContext actor, String code) {
        return AiPlatformAssembler.toResponse(getByCodeBO(actor, code));
    }

    /**
     * 查询当前租户配置的默认 AI 平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     *
     * @return 默认平台的响应数据。
     */
    @Override
    public AiPlatformResponse defaultResponse(ActorContext actor) {
        return AiPlatformAssembler.toResponse(getDefaultBO(actor));
    }

    /**
     * 根据请求参数创建 AI 平台，并维护租户默认平台状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param request 平台创建请求，包含平台名称、编码、适配器和默认标志等信息。
     *
     * @return 创建后的平台响应数据。
     */
    @Override
    public AiPlatformResponse createResponse(ActorContext actor, AiPlatformRequest request) {
        return AiPlatformAssembler.toResponse(createBO(actor, AiPlatformAssembler.toBO(request)));
    }

    /**
     * 更新指定 AI 平台，并维护租户默认平台状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待更新的平台 ID。
     * @param request 平台更新请求，包含需要变更的平台属性。
     *
     * @return 更新后的平台响应数据。
     */
    @Override
    public AiPlatformResponse updateResponse(
            ActorContext actor, Long id, AiPlatformRequest request) {
        AiPlatformBO bo = AiPlatformAssembler.toBO(request);
        bo.setId(id);
        return AiPlatformAssembler.toResponse(updateBO(actor, bo));
    }

    /**
     * 将指定 AI 平台设置为当前租户的默认平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待设为默认平台的平台 ID。
     *
     * @return 更新后的默认平台响应数据。
     */
    @Override
    public AiPlatformResponse setDefaultResponse(ActorContext actor, Long id) {
        return AiPlatformAssembler.toResponse(setDefaultBO(actor, id));
    }

    @Resource private AiPlatformRepository aiPlatformRepository;

    /**
     * platformProperties 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private PlatformProperties platformProperties;

    private Pair<Long, List<AiPlatformBO>> getPageBO(
            ActorContext actor, Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result =
                aiPlatformRepository.selectPage(actor.tenantId(), pageNo, pageSize, name, code);
        result.getRight().forEach(this::fillApiKey);
        return result;
    }

    private List<AiPlatformBO> getAllEnabledBO(ActorContext actor) {
        List<AiPlatformBO> list = aiPlatformRepository.selectAllEnabled(actor.tenantId());
        list.forEach(this::fillApiKey);
        return list;
    }

    private AiPlatformBO getByIdBO(ActorContext actor, Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(actor.tenantId(), id);
        fillApiKey(bo);
        return bo;
    }

    private AiPlatformBO getByCodeBO(ActorContext actor, String code) {
        AiPlatformBO bo = aiPlatformRepository.selectByCode(actor.tenantId(), code);
        fillApiKey(bo);
        return bo;
    }

    private AiPlatformBO getDefaultBO(ActorContext actor) {
        AiPlatformBO bo = aiPlatformRepository.selectDefault(actor.tenantId());
        fillApiKey(bo);
        return bo;
    }

    private AiPlatformBO createBO(ActorContext actor, AiPlatformBO bo) {
        bo.setAdapterType(PlatformAdapterType.parse(bo.getAdapterType()).name());
        if ("Y".equals(bo.getIsDefault())) {
            aiPlatformRepository.clearDefaultExcept(actor.tenantId(), null);
        }
        return aiPlatformRepository.create(actor.tenantId(), bo);
    }

    private AiPlatformBO updateBO(ActorContext actor, AiPlatformBO bo) {
        AiPlatformBO existing = null;
        if (StringUtils.isBlank(bo.getAdapterType()) || StringUtils.isBlank(bo.getApiKey())) {
            existing = aiPlatformRepository.selectById(actor.tenantId(), bo.getId());
            if (existing == null) {
                throw new IllegalArgumentException("平台不存在: " + bo.getId());
            }
        }
        if (StringUtils.isBlank(bo.getApiKey())) {
            bo.setApiKey(existing.getApiKey());
        }
        if (StringUtils.isBlank(bo.getAdapterType())) {
            bo.setAdapterType(PlatformAdapterType.parse(existing.getAdapterType()).name());
        } else {
            bo.setAdapterType(PlatformAdapterType.parse(bo.getAdapterType()).name());
        }
        if ("Y".equals(bo.getIsDefault())) {
            aiPlatformRepository.clearDefaultExcept(actor.tenantId(), bo.getId());
        }
        return aiPlatformRepository.update(actor.tenantId(), bo);
    }

    /**
     * 删除指定 AI 平台及其关联配置。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待删除的平台 ID。
     */
    @Override
    public void deleteById(ActorContext actor, Long id) {
        aiPlatformRepository.deleteById(actor.tenantId(), id);
    }

    /**
     * 查询当前租户下用于选择平台的 ID 和名称选项。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     *
     * @return 平台 ID 与名称选项列表。
     */
    @Override
    public List<IdNameOptionVO> getOptions(ActorContext actor) {
        return aiPlatformRepository.selectOptions(actor.tenantId());
    }

    private AiPlatformBO setDefaultBO(ActorContext actor, Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(actor.tenantId(), id);
        if (bo == null) {
            throw new IllegalArgumentException("平台不存在: " + id);
        }
        aiPlatformRepository.clearDefaultExcept(actor.tenantId(), id);
        bo.setIsDefault("Y");
        AiPlatformBO updated = aiPlatformRepository.update(actor.tenantId(), bo);
        fillApiKey(updated);
        return updated;
    }

    private void fillApiKey(AiPlatformBO bo) {
        if (bo == null || StringUtils.isBlank(bo.getCode())) {
            return;
        }
        String code = bo.getCode().toUpperCase();
        String externalApiKey =
                switch (code) {
                    case "OLLAMA" -> platformProperties.getOllama().getApiKey();
                    case "DEEPSEEK" -> platformProperties.getDeepseek().getApiKey();
                    case "OPENAI" -> platformProperties.getOpenai().getApiKey();
                    case "OPENROUTER" -> platformProperties.getOpenrouter().getApiKey();
                    case "SILICON_FLOW" -> platformProperties.getSiliconflow().getApiKey();
                    default -> null;
                };
        if (StringUtils.isNotBlank(externalApiKey)) {
            bo.setApiKey(externalApiKey);
        }
    }
}
