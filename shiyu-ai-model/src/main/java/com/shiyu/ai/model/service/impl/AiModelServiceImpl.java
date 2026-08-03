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

/**
 * AI 妯″瀷鏈嶅姟瀹炵幇灞?
 */
@Slf4j
@Service
public class AiModelServiceImpl implements AiModelService {

    @Override
    public Pair<Long, List<AiModelResponse>> pageResponse(Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result = getPageBO(platformId, pageNo, pageSize);
        return Pair.of(result.getLeft(), result.getRight().stream().map(AiModelAssembler::toResponse).toList());
    }

    @Override
    public List<AiModelResponse> byPlatformResponse(Long platformId) {
        return getByPlatformIdBO(platformId).stream().map(AiModelAssembler::toResponse).toList();
    }

    @Override
    public List<AiModelResponse> byPlatformCodeResponse(String platformCode) {
        return getByPlatformCodeBO(platformCode).stream().map(AiModelAssembler::toResponse).toList();
    }

    @Override
    public AiModelResponse detailResponse(Long id) { return AiModelAssembler.toResponse(getByIdBO(id)); }

    @Override
    public AiModelResponse defaultResponse(Long platformId) { return AiModelAssembler.toResponse(getDefaultByPlatformIdBO(platformId)); }

    @Override
    public AiModelResponse createResponse(AiModelRequest request) { return AiModelAssembler.toResponse(createBO(AiModelAssembler.toBO(request))); }

    @Override
    public AiModelResponse updateResponse(Long id, AiModelRequest request) {
        AiModelBO bo = AiModelAssembler.toBO(request); bo.setId(id);
        return AiModelAssembler.toResponse(updateBO(bo));
    }

    @Override
    public AiModelResponse setDefaultResponse(Long id) { return AiModelAssembler.toResponse(setDefaultBO(id)); }

    @Resource
    private AiModelRepository aiModelRepository;

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    private Pair<Long, List<AiModelBO>> getPageBO(Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result = aiModelRepository.selectPage(platformId, pageNo, pageSize);
        fillPlatformName(result.getRight());
        return result;
    }

    private List<AiModelBO> getByPlatformIdBO(Long platformId) {
        List<AiModelBO> list = aiModelRepository.selectByPlatformId(platformId);
        fillPlatformName(list);
        return list;
    }

    private List<AiModelBO> getByPlatformCodeBO(String platformCode) {
        AiPlatformBO platform = aiPlatformRepository.selectByCode(platformCode);
        if (platform == null) {
            return List.of();
        }
        return getByPlatformIdBO(platform.getId());
    }

    private AiModelBO getByIdBO(Long id) {
        AiModelBO bo = aiModelRepository.selectById(id);
        if (bo != null) {
            fillPlatformName(bo);
        }
        return bo;
    }

    private AiModelBO getDefaultByPlatformIdBO(Long platformId) {
        AiModelBO bo = aiModelRepository.selectDefaultByPlatformId(platformId);
        if (bo != null) {
            fillPlatformName(bo);
        }
        return bo;
    }

    private AiModelBO createBO(AiModelBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(bo.getPlatformId(), null);
        }
        AiModelBO created = aiModelRepository.create(bo);
        fillPlatformName(created);
        return created;
    }

    private AiModelBO updateBO(AiModelBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(bo.getPlatformId(), bo.getId());
        }
        AiModelBO updated = aiModelRepository.update(bo);
        fillPlatformName(updated);
        return updated;
    }

    @Override
    public void deleteById(Long id) {
        aiModelRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        aiModelRepository.deleteByIds(ids);
    }

    @Override
    public List<IdNameOptionVO> getOptions(Long platformId) {
        return aiModelRepository.selectOptions(platformId);
    }

    private AiModelBO setDefaultBO(Long id) {
        AiModelBO bo = aiModelRepository.selectById(id);
        if (bo == null) {
            throw new IllegalArgumentException("妯″瀷涓嶅瓨鍦? " + id);
        }
        aiModelRepository.clearDefaultExcept(bo.getPlatformId(), id);
        bo.setIsDefault("Y");
        AiModelBO updated = aiModelRepository.update(bo);
        fillPlatformName(updated);
        return updated;
    }

    private void fillPlatformName(List<AiModelBO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(this::fillPlatformName);
    }

    private void fillPlatformName(AiModelBO bo) {
        if (bo == null || bo.getPlatformId() == null) {
            return;
        }
        AiPlatformBO platform = aiPlatformRepository.selectById(bo.getPlatformId());
        if (platform != null) {
            bo.setPlatformName(platform.getName());
        }
    }
}
