package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.repository.AiModelRepository;
import com.shiyu.ai.agent.biz.agent.repository.AiPlatformRepository;
import com.shiyu.ai.agent.biz.agent.service.AiModelService;
import com.shiyu.ai.agent.domain.bo.AiModelBO;
import com.shiyu.ai.agent.domain.bo.AiPlatformBO;
import com.shiyu.ai.agent.domain.vo.IdNameOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 模型服务实现层
 */
@Slf4j
@Service
public class AiModelServiceImpl implements AiModelService {

    @Resource
    private AiModelRepository aiModelRepository;

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    @Override
    public Pair<Long, List<AiModelBO>> getPage(Long platformId, Number pageNo, Number pageSize) {
        Pair<Long, List<AiModelBO>> result = aiModelRepository.selectPage(platformId, pageNo, pageSize);
        fillPlatformName(result.getRight());
        return result;
    }

    @Override
    public List<AiModelBO> getByPlatformId(Long platformId) {
        List<AiModelBO> list = aiModelRepository.selectByPlatformId(platformId);
        fillPlatformName(list);
        return list;
    }

    @Override
    public AiModelBO getById(Long id) {
        AiModelBO bo = aiModelRepository.selectById(id);
        if (bo != null) {
            fillPlatformName(bo);
        }
        return bo;
    }

    @Override
    public AiModelBO getDefaultByPlatformId(Long platformId) {
        AiModelBO bo = aiModelRepository.selectDefaultByPlatformId(platformId);
        if (bo != null) {
            fillPlatformName(bo);
        }
        return bo;
    }

    @Override
    public AiModelBO create(AiModelBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(bo.getPlatformId(), null);
        }
        AiModelBO created = aiModelRepository.create(bo);
        fillPlatformName(created);
        return created;
    }

    @Override
    public AiModelBO update(AiModelBO bo) {
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

    @Override
    public AiModelBO setDefault(Long id) {
        AiModelBO bo = aiModelRepository.selectById(id);
        if (bo == null) {
            throw new IllegalArgumentException("模型不存在: " + id);
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
