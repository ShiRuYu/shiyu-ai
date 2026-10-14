package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/** Student-facing course outline. */
public record CourseDetailResponse(CourseResponse course, List<Chapter> chapters) {
    /** Visible chapter and its sections. */
    public record Chapter(Long id, String name, Integer orderNo, List<Section> sections) {}
    /** Visible section within a course chapter. */
    public record Section(Long id, String name, Integer orderNo, Integer durationMin) {}
}
