package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.CourseSectionBO;
import java.util.List;

public interface CourseSectionRepository {
    List<CourseSectionBO> selectByChapterIds(List<Long> chapterIds);
}
