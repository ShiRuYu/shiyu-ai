package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.repository.AiPlatformRepository;
import com.shiyu.ai.agent.biz.agent.service.AiPlatformService;
import com.shiyu.ai.agent.domain.bo.AiPlatformBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 平台服务实现层
 */
@Slf4j
@Service
public class AiPlatformServiceImpl implements AiPlatformService {

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    @Override
    public Pair<Long, List<AiPlatformBO>> getPage(Integer pageNo, Integer pageSize) {
        return aiPlatformRepository.selectPage(pageNo, pageSize);
    }

    @Override
    public List<AiPlatformBO> getAllEnabled() {
        return aiPlatformRepository.selectAllEnabled();
    }

    @Override
    public AiPlatformBO getById(Long id) {
        return aiPlatformRepository.selectById(id);
    }

    @Override
    public AiPlatformBO getByCode(String code) {
        return aiPlatformRepository.selectByCode(code);
    }

    @Override
    public AiPlatformBO getDefault() {
        return aiPlatformRepository.selectDefault();
    }

    @Override
    public AiPlatformBO create(AiPlatformBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiPlatformRepository.clearDefaultExcept(null);
        }
        return aiPlatformRepository.create(bo);
    }

    @Override
    public AiPlatformBO update(AiPlatformBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiPlatformRepository.clearDefaultExcept(bo.getId());
        }
        return aiPlatformRepository.update(bo);
    }

    @Override
    public void deleteById(Long id) {
        aiPlatformRepository.deleteById(id);
    }

    @Override
    public AiPlatformBO setDefault(Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(id);
        if (bo == null) {
            throw new IllegalArgumentException("平台不存在: " + id);
        }
        aiPlatformRepository.clearDefaultExcept(id);
        bo.setIsDefault("Y");
        return aiPlatformRepository.update(bo);
    }
}
