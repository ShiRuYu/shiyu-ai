package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.CourseSectionBO;
import com.shiyu.ai.dal.dataobject.education.CourseSectionDO;
import com.shiyu.ai.dal.mapper.education.CourseSectionMapper;
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
