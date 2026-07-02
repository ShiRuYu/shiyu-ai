package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.CourseSectionDO;
import com.shiyu.ai.dal.mapper.education.CourseSectionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseSectionRepository {

    @Resource
    private CourseSectionMapper courseSectionMapper;

    public List<CourseSectionDO> selectByChapterIds(List<Long> chapterIds) {
        return courseSectionMapper.selectListByQuery(
                QueryWrapper.create().in("chapter_id", chapterIds).orderBy("order_no", true));
    }
}
