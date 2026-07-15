package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.SubjectBO;
import com.shiyu.ai.dal.dataobject.education.SubjectDO;
import com.shiyu.ai.dal.mapper.education.SubjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SubjectRepository {

    @Resource
    private SubjectMapper subjectMapper;

    public SubjectBO selectById(Long id) {
        return MapstructUtils.convert(subjectMapper.selectOneById(id), SubjectBO.class);
    }

    public SubjectBO selectByCode(String code) {
        return MapstructUtils.convert(subjectMapper.selectOneById(code), SubjectBO.class);
    }

    public PageData<SubjectBO> selectPage(int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<SubjectDO> page = subjectMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), SubjectBO.class), page.getTotalRow());
    }

    public List<SubjectBO> selectByGradeLevel(String gradeLevel) {
        return MapstructUtils.convert(subjectMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("grade_level", gradeLevel)
        ), SubjectBO.class);
    }
    public List<SubjectBO> selectAll() {
        return MapstructUtils.convert(subjectMapper.selectListByQuery(QueryWrapper.create()), SubjectBO.class);
    }

    public int insert(SubjectBO entity) {
        SubjectDO dataObj = MapstructUtils.convert(entity, SubjectDO.class);
        return subjectMapper.insert(dataObj);
    }


    public int update(SubjectBO entity) {
        SubjectDO dataObj = MapstructUtils.convert(entity, SubjectDO.class);
        return subjectMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return subjectMapper.deleteById(id);
    }

}
