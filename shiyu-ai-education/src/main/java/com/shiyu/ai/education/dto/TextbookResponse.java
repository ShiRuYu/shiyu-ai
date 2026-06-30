package com.shiyu.ai.education.dto;

public record TextbookResponse(
        Long id,
        String name,
        String subjectCode,
        Integer grade,
        String publisher,
        String isbn
) {}
