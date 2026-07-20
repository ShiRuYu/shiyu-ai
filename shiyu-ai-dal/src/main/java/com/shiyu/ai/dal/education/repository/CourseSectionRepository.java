package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.CourseSectionBO;
import com.shiyu.ai.dal.education.dataobject.CourseSectionDO;
import com.shiyu.ai.dal.education.mapper.CourseSectionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseSectionRepository {

    @Resource
    private CourseSectionMapper courseSectionMapper;

    public List<CourseSectionBO> selectByChapterIds(List<Long> chapterIds) {
        return MapstructUtils.convert(courseSectionMapper.selectListByQuery(
                QueryWrapper.create().in("chapter_id", chapterIds).orderBy("order_no", true)), CourseSectionBO.class);
    }
}
