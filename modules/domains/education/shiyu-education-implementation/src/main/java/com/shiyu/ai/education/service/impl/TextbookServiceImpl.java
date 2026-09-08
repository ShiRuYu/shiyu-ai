package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.TextbookBO;
import com.shiyu.ai.education.port.repository.TextbookRepository;
import com.shiyu.ai.education.dto.TextbookResponse;
import com.shiyu.ai.education.request.TextbookRequest;
import com.shiyu.ai.education.service.TextbookService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextbookServiceImpl implements TextbookService {

    @Override
    public PageData<TextbookResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<TextbookBO> boPage = textbookRepository.selectPage(requireActor(actor).tenantId(), pageNum, pageSize);
        List<TextbookResponse> items = MapstructUtils.convert(boPage.getItems(), TextbookResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    private final TextbookRepository textbookRepository;

    @Override
    public TextbookResponse getById(ActorContext actor, Long id) {
        TextbookBO bo = textbookRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }


    @Override
    public List<TextbookResponse> listBySubjectAndGrade(ActorContext actor, String subjectCode, Integer grade) {
        List<TextbookBO> boList = textbookRepository.selectBySubjectAndGrade(requireActor(actor).tenantId(), subjectCode, grade);
        return MapstructUtils.convert(boList, TextbookResponse.class);
    }

    public List<TextbookResponse> listAll(ActorContext actor) {
        List<TextbookBO> boList = textbookRepository.selectAll(requireActor(actor).tenantId());
        return MapstructUtils.convert(boList, TextbookResponse.class);
    }




    @Override
    @Transactional(rollbackFor = Exception.class)
    public TextbookResponse create(ActorContext actor, TextbookRequest request) {
        actor = requireActor(actor);
        TextbookBO bo = new TextbookBO();
        bo.setName(request.getName());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setPublisher(request.getPublisher());
        bo.setIsbn(request.getIsbn());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        textbookRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, TextbookRequest request) {
        actor = requireActor(actor);
        TextbookBO bo = textbookRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setPublisher(request.getPublisher());
            bo.setIsbn(request.getIsbn());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            textbookRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        textbookRepository.deleteById(requireActor(actor).tenantId(), id);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return java.util.Objects.requireNonNull(actor, "actor is required");
    }
}
