package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/** 面向学生的课程大纲。 */
public record CourseDetailResponse(CourseResponse course, List<Chapter> chapters) {
    /** 可见的章节及其小节。 */
    public record Chapter(Long id, String name, Integer orderNo, List<Section> sections) {}
    /** 课程章节中可见的小节。 */
    public record Section(Long id, String name, Integer orderNo, Integer durationMin) {}
}
