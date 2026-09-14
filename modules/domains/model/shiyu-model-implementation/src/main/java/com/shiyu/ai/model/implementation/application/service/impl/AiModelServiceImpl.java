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

/** AI 模型服务实现。 */
@Slf4j
@Service
public class AiModelServiceImpl implements AiModelService {

    /**
     * {@code pageResponse} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param platformId 参数值，用于执行当前操作。
     * @param pageNo 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code byPlatformResponse} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param platformId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiModelResponse> byPlatformResponse(ActorContext actor, Long platformId) {
        return getByPlatformIdBO(actor, platformId).stream()
                .map(AiModelAssembler::toResponse)
                .toList();
    }

    /**
     * {@code byPlatformCodeResponse} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param platformCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiModelResponse> byPlatformCodeResponse(ActorContext actor, String platformCode) {
        return getByPlatformCodeBO(actor, platformCode).stream()
                .map(AiModelAssembler::toResponse)
                .toList();
    }

    /**
     * {@code detailResponse} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public AiModelResponse detailResponse(ActorContext actor, Long id) {
        return AiModelAssembler.toResponse(getByIdBO(actor, id));
    }

    /**
     * {@code defaultResponse} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param platformId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public AiModelResponse defaultResponse(ActorContext actor, Long platformId) {
        return AiModelAssembler.toResponse(getDefaultByPlatformIdBO(actor, platformId));
    }

    /**
     * {@code createResponse} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public AiModelResponse createResponse(ActorContext actor, AiModelRequest request) {
        return AiModelAssembler.toResponse(createBO(actor, AiModelAssembler.toBO(request)));
    }

    /**
     * {@code updateResponse} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public AiModelResponse updateResponse(ActorContext actor, Long id, AiModelRequest request) {
        AiModelBO bo = AiModelAssembler.toBO(request);
        bo.setId(id);
        return AiModelAssembler.toResponse(updateBO(actor, bo));
    }

    /**
     * {@code setDefaultResponse} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteById(ActorContext actor, Long id) {
        aiModelRepository.deleteById(actor.tenantId(), id);
    }

    /**
     * {@code deleteByIds} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param ids 参数值，用于执行当前操作。
     */
    @Override
    public void deleteByIds(ActorContext actor, List<Long> ids) {
        aiModelRepository.deleteByIds(actor.tenantId(), ids);
    }

    /**
     * {@code getOptions} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param platformId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
