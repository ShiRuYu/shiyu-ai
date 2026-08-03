package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.domain.model.TextbookBO;

@AutoMapper(target = TextbookBO.class)
public record TextbookResponse(
        Long id,
        String name,
        String subjectCode,
        Integer grade,
        String publisher,
        String isbn
) {}
