package com.shiyu.ai.model.service.impl;

import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.dal.model.repository.AiPlatformRepository;
import com.shiyu.ai.model.service.AiPlatformService;
import com.shiyu.ai.dal.model.bo.AiPlatformBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private PlatformProperties platformProperties;

    @Override
    public Pair<Long, List<AiPlatformBO>> getPage(Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result = aiPlatformRepository.selectPage(pageNo, pageSize, name, code);
        result.getRight().forEach(this::fillApiKey);
        return result;
    }

    @Override
    public List<AiPlatformBO> getAllEnabled() {
        List<AiPlatformBO> list = aiPlatformRepository.selectAllEnabled();
        list.forEach(this::fillApiKey);
        return list;
    }

    @Override
    public AiPlatformBO getById(Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(id);
        fillApiKey(bo);
        return bo;
    }

    @Override
    public AiPlatformBO getByCode(String code) {
        AiPlatformBO bo = aiPlatformRepository.selectByCode(code);
        fillApiKey(bo);
        return bo;
    }

    @Override
    public AiPlatformBO getDefault() {
        AiPlatformBO bo = aiPlatformRepository.selectDefault();
        fillApiKey(bo);
        return bo;
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
    public List<IdNameOptionVO> getOptions() {
        return aiPlatformRepository.selectOptions();
    }

    @Override
    public AiPlatformBO setDefault(Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(id);
        if (bo == null) {
            throw new IllegalArgumentException("平台不存在: " + id);
        }
        aiPlatformRepository.clearDefaultExcept(id);
        bo.setIsDefault("Y");
        AiPlatformBO updated = aiPlatformRepository.update(bo);
        fillApiKey(updated);
        return updated;
    }

    private void fillApiKey(AiPlatformBO bo) {
        if (bo == null || StringUtils.isBlank(bo.getCode())) {
            return;
        }
        String code = bo.getCode().toUpperCase();
        String externalApiKey = switch (code) {
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
