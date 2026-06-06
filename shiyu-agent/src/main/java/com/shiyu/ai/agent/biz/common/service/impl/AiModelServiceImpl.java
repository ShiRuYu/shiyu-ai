package com.shiyu.ai.agent.biz.common.service.impl;

import com.shiyu.ai.agent.biz.common.repository.AiModelRepository;
import com.shiyu.ai.agent.biz.common.service.AiModelService;
import com.shiyu.ai.agent.domain.bo.AiModelBO;
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

    @Override
    public Pair<Long, List<AiModelBO>> getPage(Long platformId, Integer pageNo, Integer pageSize) {
        return aiModelRepository.selectPage(platformId, pageNo, pageSize);
    }

    @Override
    public List<AiModelBO> getByPlatformId(Long platformId) {
        return aiModelRepository.selectByPlatformId(platformId);
    }

    @Override
    public AiModelBO getById(Long id) {
        return aiModelRepository.selectById(id);
    }

    @Override
    public AiModelBO getDefaultByPlatformId(Long platformId) {
        return aiModelRepository.selectDefaultByPlatformId(platformId);
    }

    @Override
    public AiModelBO create(AiModelBO bo) {
        // 如果设置为默认，先清除该平台下其他默认
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(bo.getPlatformId(), null);
        }
        return aiModelRepository.create(bo);
    }

    @Override
    public AiModelBO update(AiModelBO bo) {
        // 如果设置为默认，先清除该平台下其他默认
        if ("Y".equals(bo.getIsDefault())) {
            aiModelRepository.clearDefaultExcept(bo.getPlatformId(), bo.getId());
        }
        return aiModelRepository.update(bo);
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
    public AiModelBO setDefault(Long id) {
        AiModelBO bo = aiModelRepository.selectById(id);
        if (bo == null) {
            throw new IllegalArgumentException("模型不存在: " + id);
        }
        // 清除该平台下其他默认
        aiModelRepository.clearDefaultExcept(bo.getPlatformId(), id);
        // 设置当前为默认
        bo.setIsDefault("Y");
        return aiModelRepository.update(bo);
    }
}
