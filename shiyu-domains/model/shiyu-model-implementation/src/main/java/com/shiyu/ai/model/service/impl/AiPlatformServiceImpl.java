package com.shiyu.ai.model.service.impl;

import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.service.AiPlatformService;
import com.shiyu.ai.model.api.request.AiPlatformRequest;
import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.application.assembler.AiPlatformAssembler;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.domain.model.PlatformAdapterType;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * AI 骞冲彴鏈嶅姟瀹炵幇灞?
 */
@Slf4j
@Service
public class AiPlatformServiceImpl implements AiPlatformService {

    @Override
    public Pair<Long, List<AiPlatformResponse>> pageResponse(ActorContext actor, Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result = getPageBO(actor, pageNo, pageSize, name, code);
        return Pair.of(result.getLeft(), result.getRight().stream().map(AiPlatformAssembler::toResponse).toList());
    }
    @Override
    public List<AiPlatformResponse> enabledResponse(ActorContext actor) { return getAllEnabledBO(actor).stream().map(AiPlatformAssembler::toResponse).toList(); }
    @Override
    public AiPlatformResponse detailResponse(ActorContext actor, Long id) { return AiPlatformAssembler.toResponse(getByIdBO(actor, id)); }
    @Override
    public AiPlatformResponse codeResponse(ActorContext actor, String code) { return AiPlatformAssembler.toResponse(getByCodeBO(actor, code)); }
    @Override
    public AiPlatformResponse defaultResponse(ActorContext actor) { return AiPlatformAssembler.toResponse(getDefaultBO(actor)); }
    @Override
    public AiPlatformResponse createResponse(ActorContext actor, AiPlatformRequest request) { return AiPlatformAssembler.toResponse(createBO(actor, AiPlatformAssembler.toBO(request))); }
    @Override
    public AiPlatformResponse updateResponse(ActorContext actor, Long id, AiPlatformRequest request) {
        AiPlatformBO bo = AiPlatformAssembler.toBO(request); bo.setId(id);
        return AiPlatformAssembler.toResponse(updateBO(actor, bo));
    }
    @Override
    public AiPlatformResponse setDefaultResponse(ActorContext actor, Long id) { return AiPlatformAssembler.toResponse(setDefaultBO(actor, id)); }

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    @Resource
    private PlatformProperties platformProperties;

    private Pair<Long, List<AiPlatformBO>> getPageBO(ActorContext actor, Number pageNo, Number pageSize, String name, String code) {
        Pair<Long, List<AiPlatformBO>> result = aiPlatformRepository.selectPage(actor.tenantId(), pageNo, pageSize, name, code);
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
        if (StringUtils.isBlank(bo.getAdapterType())) {
            AiPlatformBO existing = aiPlatformRepository.selectById(actor.tenantId(), bo.getId());
            if (existing == null) {
                throw new IllegalArgumentException("平台不存在: " + bo.getId());
            }
            bo.setAdapterType(PlatformAdapterType.parse(existing.getAdapterType()).name());
        } else {
            bo.setAdapterType(PlatformAdapterType.parse(bo.getAdapterType()).name());
        }
        if ("Y".equals(bo.getIsDefault())) {
            aiPlatformRepository.clearDefaultExcept(actor.tenantId(), bo.getId());
        }
        return aiPlatformRepository.update(actor.tenantId(), bo);
    }

    @Override
    public void deleteById(ActorContext actor, Long id) {
        aiPlatformRepository.deleteById(actor.tenantId(), id);
    }

    @Override
    public List<IdNameOptionVO> getOptions(ActorContext actor) {
        return aiPlatformRepository.selectOptions(actor.tenantId());
    }

    private AiPlatformBO setDefaultBO(ActorContext actor, Long id) {
        AiPlatformBO bo = aiPlatformRepository.selectById(actor.tenantId(), id);
        if (bo == null) {
            throw new IllegalArgumentException("骞冲彴涓嶅瓨鍦? " + id);
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
