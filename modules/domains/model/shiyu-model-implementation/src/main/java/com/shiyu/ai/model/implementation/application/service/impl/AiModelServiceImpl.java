package com.shiyu.ai.model.implementation.application.service.impl;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.model.implementation.application.assembler.AiModelAssembler;
import com.shiyu.ai.model.implementation.application.service.AiModelService;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.domain.port.repository.AiModelRepository;
import com.shiyu.ai.model.implementation.domain.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提供 AI 模型 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class AiModelServiceImpl implements AiModelService {

    /**
     * 按当前租户和可选平台分页查询 AI 模型，并将领域对象转换为响应数据。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 可选的平台 ID；为空时查询当前租户下所有平台的模型。
     * @param pageNo 页码，从 1 开始。
     * @param pageSize 每页返回的模型数量。
     *
     * @return 左值为模型总数，右值为当前页的模型响应列表。
     */
    @Override
    public Pair<Long, List<AiModelResponse>> pageResponse(
            ActorContext actor, Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result = getPageBO(actor, platformId, pageNo, pageSize);
        return Pair.of(
                result.getLeft(),
                result.getRight().stream().map(AiModelAssembler::toResponse).toList());
    }

    /**
     * 查询指定平台下当前租户可访问的全部 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 平台 ID。
     *
     * @return 指定平台下的模型响应列表；没有匹配模型时返回空列表。
     */
    @Override
    public List<AiModelResponse> byPlatformResponse(ActorContext actor, Long platformId) {
        return getByPlatformIdBO(actor, platformId).stream()
                .map(AiModelAssembler::toResponse)
                .toList();
    }

    /**
     * 根据平台编码查询当前租户可访问的 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformCode 平台唯一编码。
     *
     * @return 对应平台的模型响应列表；平台不存在时返回空列表。
     */
    @Override
    public List<AiModelResponse> byPlatformCodeResponse(ActorContext actor, String platformCode) {
        return getByPlatformCodeBO(actor, platformCode).stream()
                .map(AiModelAssembler::toResponse)
                .toList();
    }

    /**
     * 查询指定 AI 模型的详细信息。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 模型 ID。
     *
     * @return 指定模型的响应数据。
     */
    @Override
    public AiModelResponse detailResponse(ActorContext actor, Long id) {
        return AiModelAssembler.toResponse(getByIdBO(actor, id));
    }

    /**
     * 查询指定平台当前配置的默认 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 平台 ID。
     *
     * @return 指定平台的默认模型响应数据。
     */
    @Override
    public AiModelResponse defaultResponse(ActorContext actor, Long platformId) {
        return AiModelAssembler.toResponse(getDefaultByPlatformIdBO(actor, platformId));
    }

    /**
     * 根据请求参数创建 AI 模型，并维护其所属平台的默认模型状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param request 模型创建请求，包含所属平台、名称和默认标志等信息。
     *
     * @return 创建后的模型响应数据。
     */
    @Override
    public AiModelResponse createResponse(ActorContext actor, AiModelRequest request) {
        return AiModelAssembler.toResponse(createBO(actor, AiModelAssembler.toBO(request)));
    }

    /**
     * 更新指定 AI 模型，并维护其所属平台的默认模型状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待更新的模型 ID。
     * @param request 模型更新请求，包含需要变更的模型属性。
     *
     * @return 更新后的模型响应数据。
     */
    @Override
    public AiModelResponse updateResponse(ActorContext actor, Long id, AiModelRequest request) {
        AiModelBO bo = AiModelAssembler.toBO(request);
        bo.setId(id);
        return AiModelAssembler.toResponse(updateBO(actor, bo));
    }

    /**
     * 将指定 AI 模型设置为所属平台的默认模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待设为默认模型的模型 ID。
     *
     * @return 更新后的默认模型响应数据。
     */
    @Override
    public AiModelResponse setDefaultResponse(ActorContext actor, Long id) {
        return AiModelAssembler.toResponse(setDefaultBO(actor, id));
    }

    @Resource private AiModelRepository aiModelRepository;

    /**
     * aiPlatformRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private AiPlatformRepository aiPlatformRepository;

    private Pair<Long, List<AiModelBO>> getPageBO(
            ActorContext actor, Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result =
                aiModelRepository.selectPage(actor.tenantId(), platformId, pageNo, pageSize);
        fillPlatformName(actor, result.getRight());
        return result;
    }

    private List<AiModelBO> getByPlatformIdBO(ActorContext actor, Long platformId) {
        List<AiModelBO> list = aiModelRepository.selectByPlatformId(actor.tenantId(), platformId);
        fillPlatformName(actor, list);
        return list;
    }

    private List<AiModelBO> getByPlatformCodeBO(ActorContext actor, String platformCode) {
        AiPlatformBO platform = aiPlatformRepository.selectByCode(actor.tenantId(), platformCode);
        if (platform == null) {
            return List.of();
        }
        return getByPlatformIdBO(actor, platform.getId());
    }

    private AiModelBO getByIdBO(ActorContext actor, Long id) {
        AiModelBO bo = aiModelRepository.selectById(actor.tenantId(), id);
        if (bo != null) {
            fillPlatformName(actor, bo);
        }
        return bo;
    }

    private AiModelBO getDefaultByPlatformIdBO(ActorContext actor, Long platformId) {
        AiModelBO bo = aiModelRepository.selectDefaultByPlatformId(actor.tenantId(), platformId);
        if (bo != null) {
            fillPlatformName(actor, bo);
        }
        return bo;
    }

    private AiModelBO createBO(ActorContext actor, AiModelBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(actor.tenantId(), bo.getPlatformId(), null);
        }
        AiModelBO created = aiModelRepository.create(actor.tenantId(), bo);
        fillPlatformName(actor, created);
        return created;
    }

    private AiModelBO updateBO(ActorContext actor, AiModelBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(actor.tenantId(), bo.getPlatformId(), bo.getId());
        }
        AiModelBO updated = aiModelRepository.update(actor.tenantId(), bo);
        fillPlatformName(actor, updated);
        return updated;
    }

    /**
     * 删除指定 AI 模型及其关联配置。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待删除的模型 ID。
     */
    @Override
    public void deleteById(ActorContext actor, Long id) {
        aiModelRepository.deleteById(actor.tenantId(), id);
    }

    /**
     * 批量删除指定 AI 模型及其关联配置。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param ids 待删除的模型 ID 集合。
     */
    @Override
    public void deleteByIds(ActorContext actor, List<Long> ids) {
        aiModelRepository.deleteByIds(actor.tenantId(), ids);
    }

    /**
     * 查询指定平台下用于选择模型的 ID 和名称选项。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 可选的平台 ID；为空时查询当前租户下所有平台的模型选项。
     *
     * @return 模型 ID 与名称选项列表。
     */
    @Override
    public List<IdNameOptionVO> getOptions(ActorContext actor, Long platformId) {
        return aiModelRepository.selectOptions(actor.tenantId(), platformId);
    }

    private AiModelBO setDefaultBO(ActorContext actor, Long id) {
        AiModelBO bo = aiModelRepository.selectById(actor.tenantId(), id);
        if (bo == null) {
            throw new IllegalArgumentException("模型不存在: " + id);
        }
        aiModelRepository.clearDefaultExcept(actor.tenantId(), bo.getPlatformId(), id);
        bo.setIsDefault("Y");
        AiModelBO updated = aiModelRepository.update(actor.tenantId(), bo);
        fillPlatformName(actor, updated);
        return updated;
    }

    private void fillPlatformName(ActorContext actor, List<AiModelBO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(bo -> fillPlatformName(actor, bo));
    }

    private void fillPlatformName(ActorContext actor, AiModelBO bo) {
        if (bo == null || bo.getPlatformId() == null) {
            return;
        }
        AiPlatformBO platform =
                aiPlatformRepository.selectById(actor.tenantId(), bo.getPlatformId());
        if (platform != null) {
            bo.setPlatformName(platform.getName());
        }
    }
}
