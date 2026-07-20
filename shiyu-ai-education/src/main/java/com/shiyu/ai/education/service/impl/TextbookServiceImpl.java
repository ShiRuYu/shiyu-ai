package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.TextbookBO;
import com.shiyu.ai.dal.education.repository.TextbookRepository;
import com.shiyu.ai.education.dto.TextbookResponse;
import com.shiyu.ai.education.request.TextbookRequest;
import com.shiyu.ai.education.service.TextbookService;
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
    public PageData<TextbookResponse> page(int pageNum, int pageSize) {
        PageData<TextbookBO> boPage = textbookRepository.selectPage(pageNum, pageSize);
        List<TextbookResponse> items = MapstructUtils.convert(boPage.getItems(), TextbookResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    private final TextbookRepository textbookRepository;

    @Override
    public TextbookResponse getById(Long id) {
        TextbookBO bo = textbookRepository.selectById(id);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }


    @Override
    public List<TextbookResponse> listBySubjectAndGrade(String subjectCode, Integer grade) {
        List<TextbookBO> boList = textbookRepository.selectBySubjectAndGrade(subjectCode, grade);
        return MapstructUtils.convert(boList, TextbookResponse.class);
    }

    public List<TextbookResponse> listAll() {
        List<TextbookBO> boList = textbookRepository.selectAll();
        return MapstructUtils.convert(boList, TextbookResponse.class);
    }




    @Override
    @Transactional(rollbackFor = Exception.class)
    public TextbookResponse create(TextbookRequest request) {
        TextbookBO bo = new TextbookBO();
        bo.setName(request.getName());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setPublisher(request.getPublisher());
        textbookRepository.insert(bo);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TextbookRequest request) {
        TextbookBO bo = textbookRepository.selectById(request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setPublisher(request.getPublisher());
            textbookRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        textbookRepository.deleteById(id);
    }
}
