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
    public CourseResponse getById(Long id) {
        CourseBO bo = courseRepository.selectById(id);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    @Override
    public List<CourseResponse> listBySubjectCode(String subjectCode) {
        List<CourseBO> boList = courseRepository.selectBySubjectCode(subjectCode);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    @Override
    public List<CourseResponse> listByGrade(Integer grade) {
        List<CourseBO> boList = courseRepository.selectByGrade(grade);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    @Override
    public PageData<CourseResponse> page(int pageNum, int pageSize) {
        PageData<CourseBO> boPage = courseRepository.selectPage(pageNum, pageSize);
        List<CourseResponse> items = MapstructUtils.convert(boPage.getItems(), CourseResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    public CourseProgressResponse getProgress(Long courseId, Long studentId) {
        return new CourseProgressResponse(courseId, null, 0, 0, 0.0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponse create(CourseRequest request) {
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
        courseRepository.insert(bo);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CourseRequest request) {
        CourseBO bo = courseRepository.selectById(request.getId());
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
            courseRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

}
