package com.shiyu.ai.model.service.impl;

import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.service.AiPlatformService;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.application.assembler.AiPlatformAssembler;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 骞冲彴鏈嶅姟瀹炵幇灞?
 */
@Slf4j
@Service
public class AiPlatformServiceImpl implements AiPlatformService {

    @Override
    public Pair<Long, List<AiPlatformResponse>> pageResponse(Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result = getPageBO(pageNo, pageSize, name, code);
        return Pair.of(result.getLeft(), result.getRight().stream().map(AiPlatformAssembler::toResponse).toList());
    }
    @Override
    public List<AiPlatformResponse> enabledResponse() { return getAllEnabledBO().stream().map(AiPlatformAssembler::toResponse).toList(); }
    @Override
    public AiPlatformResponse detailResponse(Long id) { return AiPlatformAssembler.toResponse(getByIdBO(id)); }
    @Override
    public AiPlatformResponse codeResponse(String code) { return AiPlatformAssembler.toResponse(getByCodeBO(code)); }
    @Override
    public AiPlatformResponse defaultResponse() { return AiPlatformAssembler.toResponse(getDefaultBO()); }
    @Override
    public AiPlatformResponse createResponse(AiPlatformRequest request) { return AiPlatformAssembler.toResponse(createBO(AiPlatformAssembler.toBO(request))); }
    @Override
    public AiPlatformResponse updateResponse(Long id, AiPlatformRequest request) {
        AiPlatformBO bo = AiPlatformAssembler.toBO(request); bo.setId(id);
        return AiPlatformAssembler.toResponse(updateBO(bo));
    }
    @Override
    public AiPlatformResponse setDefaultResponse(Long id) { return AiPlatformAssembler.toResponse(setDefaultBO(id)); }

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    @Resource
    private PlatformProperties platformProperties;

    private Pair<Long, List<AiPlatformBO>> getPageBO(Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result = aiPlatformRepository.selectPage(pageNo, pageSize, name, code);
        result.getRight().forEach(this::fillApiKey);
        return result;
    }

    private List<AiPlatformBO> getAllEnabledBO() {
        List<AiPlatformBO> list = aiPlatformRepository.selectAllEnabled();
        list.forEach(this::fillApiKey);
        return list;
    }

    private AiPlatformBO getByIdBO(Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(id);
        fillApiKey(bo);
        return bo;
    }

    private AiPlatformBO getByCodeBO(String code) {
        AiPlatformBO bo = aiPlatformRepository.selectByCode(code);
        fillApiKey(bo);
        return bo;
    }

    private AiPlatformBO getDefaultBO() {
        AiPlatformBO bo = aiPlatformRepository.selectDefault();
        fillApiKey(bo);
        return bo;
    }

    private AiPlatformBO createBO(AiPlatformBO bo) {
        if ("Y".equals(bo.getIsDefault())) {
            aiPlatformRepository.clearDefaultExcept(null);
        }
        return aiPlatformRepository.create(bo);
    }

    private AiPlatformBO updateBO(AiPlatformBO bo) {
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

    private AiPlatformBO setDefaultBO(Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(id);
        if (bo == null) {
            throw new IllegalArgumentException("骞冲彴涓嶅瓨鍦? " + id);
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
