package com.shiyu.ai.model.service.impl;

import com.shiyu.ai.model.port.repository.AiModelRepository;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.service.AiModelService;
import com.shiyu.ai.model.api.request.AiModelRequest;
import com.shiyu.ai.model.api.response.AiModelResponse;
import com.shiyu.ai.model.application.assembler.AiModelAssembler;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * AI 妯″瀷鏈嶅姟瀹炵幇灞?
 */
@Slf4j
@Service
public class AiModelServiceImpl implements AiModelService {

    @Override
    public Pair<Long, List<AiModelResponse>> pageResponse(ActorContext actor, Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result = getPageBO(actor, platformId, pageNo, pageSize);
        return Pair.of(result.getLeft(), result.getRight().stream().map(AiModelAssembler::toResponse).toList());
    }

    @Override
    public List<AiModelResponse> byPlatformResponse(ActorContext actor, Long platformId) {
        return getByPlatformIdBO(actor, platformId).stream().map(AiModelAssembler::toResponse).toList();
    }

    @Override
    public List<AiModelResponse> byPlatformCodeResponse(ActorContext actor, String platformCode) {
        return getByPlatformCodeBO(actor, platformCode).stream().map(AiModelAssembler::toResponse).toList();
    }

    @Override
    public AiModelResponse detailResponse(ActorContext actor, Long id) { return AiModelAssembler.toResponse(getByIdBO(actor, id)); }

    @Override
    public AiModelResponse defaultResponse(ActorContext actor, Long platformId) { return AiModelAssembler.toResponse(getDefaultByPlatformIdBO(actor, platformId)); }

    @Override
    public AiModelResponse createResponse(ActorContext actor, AiModelRequest request) { return AiModelAssembler.toResponse(createBO(actor, AiModelAssembler.toBO(request))); }

    @Override
    public AiModelResponse updateResponse(ActorContext actor, Long id, AiModelRequest request) {
        AiModelBO bo = AiModelAssembler.toBO(request); bo.setId(id);
        return AiModelAssembler.toResponse(updateBO(actor, bo));
    }

    @Override
    public AiModelResponse setDefaultResponse(ActorContext actor, Long id) { return AiModelAssembler.toResponse(setDefaultBO(actor, id)); }

    @Resource
    private AiModelRepository aiModelRepository;

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    private Pair<Long, List<AiModelBO>> getPageBO(ActorContext actor, Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result = aiModelRepository.selectPage(actor.tenantId(), platformId, pageNo, pageSize);
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

    @Override
    public void deleteById(ActorContext actor, Long id) {
        aiModelRepository.deleteById(actor.tenantId(), id);
    }

    @Override
    public void deleteByIds(ActorContext actor, List<Long> ids) {
        aiModelRepository.deleteByIds(actor.tenantId(), ids);
    }

    @Override
    public List<IdNameOptionVO> getOptions(ActorContext actor, Long platformId) {
        return aiModelRepository.selectOptions(actor.tenantId(), platformId);
    }

    private AiModelBO setDefaultBO(ActorContext actor, Long id) {
        AiModelBO bo = aiModelRepository.selectById(actor.tenantId(), id);
        if (bo == null) {
            throw new IllegalArgumentException("妯″瀷涓嶅瓨鍦? " + id);
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
        AiPlatformBO platform = aiPlatformRepository.selectById(actor.tenantId(), bo.getPlatformId());
        if (platform != null) {
            bo.setPlatformName(platform.getName());
        }
    }
}
