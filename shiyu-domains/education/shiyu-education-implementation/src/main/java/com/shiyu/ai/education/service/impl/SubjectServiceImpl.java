package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.SubjectBO;
import com.shiyu.ai.education.port.repository.SubjectRepository;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.request.SubjectRequest;
import com.shiyu.ai.education.service.SubjectService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse getById(ActorContext actor, Long id) {
        SubjectBO bo = subjectRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    @Override
    public SubjectResponse getByCode(ActorContext actor, String code) {
        SubjectBO bo = subjectRepository.selectByCode(requireActor(actor).tenantId(), code);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    @Override
    public List<SubjectResponse> listByGradeLevel(ActorContext actor, String gradeLevel) {
        List<SubjectBO> boList = subjectRepository.selectByGradeLevel(requireActor(actor).tenantId(), gradeLevel);
        return MapstructUtils.convert(boList, SubjectResponse.class);
    }

    @Override
    public PageData<SubjectResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<SubjectBO> boPage = subjectRepository.selectPage(requireActor(actor).tenantId(), pageNum, pageSize);
        List<SubjectResponse> items = MapstructUtils.convert(boPage.getItems(), SubjectResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubjectResponse create(ActorContext actor, SubjectRequest request) {
        actor = requireActor(actor);
        SubjectBO bo = new SubjectBO();
        bo.setCode(request.getCode());
        bo.setName(request.getName());
        bo.setGradeLevel(request.getGradeLevel());
        bo.setIcon(request.getIcon());
        bo.setSortOrder(request.getSortOrder());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        subjectRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, SubjectRequest request) {
        actor = requireActor(actor);
        SubjectBO bo = subjectRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setCode(request.getCode());
            bo.setName(request.getName());
            bo.setGradeLevel(request.getGradeLevel());
            bo.setIcon(request.getIcon());
            bo.setSortOrder(request.getSortOrder());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            subjectRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        subjectRepository.deleteById(requireActor(actor).tenantId(), id);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return java.util.Objects.requireNonNull(actor, "actor is required");
    }
}
