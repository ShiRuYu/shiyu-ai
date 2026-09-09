package com.shiyu.ai.education.implementation.web.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.implementation.domain.model.TextbookBO;

@AutoMapper(target = TextbookBO.class)
public record TextbookResponse(
        Long id,
        String name,
        String subjectCode,
        Integer grade,
        String publisher,
        String isbn
) {}

