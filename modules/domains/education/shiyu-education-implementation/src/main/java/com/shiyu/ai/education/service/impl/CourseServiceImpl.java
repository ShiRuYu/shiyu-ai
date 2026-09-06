package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.CourseBO;
import com.shiyu.ai.education.port.repository.CourseRepository;
import com.shiyu.ai.education.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.dto.CourseProgressResponse;
import com.shiyu.ai.education.dto.CourseResponse;
import com.shiyu.ai.education.request.CourseRequest;
import com.shiyu.ai.education.service.CourseService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseResponse getById(ActorContext actor, Long id) {
        CourseBO bo = courseRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    @Override
    public List<CourseResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<CourseBO> boList = courseRepository.selectBySubjectCode(requireActor(actor).tenantId(), subjectCode);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    @Override
    public List<CourseResponse> listByGrade(ActorContext actor, Integer grade) {
        List<CourseBO> boList = courseRepository.selectByGrade(requireActor(actor).tenantId(), grade);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    @Override
    public PageData<CourseResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<CourseBO> boPage = courseRepository.selectPage(requireActor(actor).tenantId(), pageNum, pageSize);
        List<CourseResponse> items = MapstructUtils.convert(boPage.getItems(), CourseResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    public CourseProgressResponse getProgress(ActorContext actor, Long courseId, Long studentId) {
        requireActor(actor);
        return new CourseProgressResponse(courseId, null, 0, 0, 0.0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponse create(ActorContext actor, CourseRequest request) {
        actor = requireActor(actor);
        CourseBO bo = new CourseBO();
        bo.setName(request.getName());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setDescription(request.getDescription());
        bo.setCoverUrl(request.getCoverUrl());
        bo.setTextbookId(request.getTextbookId());
        bo.setTeacherId(request.getTeacherId());
        bo.setTotalHours(request.getTotalHours());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        courseRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, CourseRequest request) {
        actor = requireActor(actor);
        CourseBO bo = courseRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setDescription(request.getDescription());
            bo.setCoverUrl(request.getCoverUrl());
            bo.setTextbookId(request.getTextbookId());
            bo.setTeacherId(request.getTeacherId());
            bo.setTotalHours(request.getTotalHours());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            courseRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        courseRepository.deleteById(requireActor(actor).tenantId(), id);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return java.util.Objects.requireNonNull(actor, "actor is required");
    }

}
