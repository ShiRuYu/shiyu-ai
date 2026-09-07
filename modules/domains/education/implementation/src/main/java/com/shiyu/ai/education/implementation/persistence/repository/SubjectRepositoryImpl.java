package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.SubjectBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.SubjectDO;
import com.shiyu.ai.education.implementation.persistence.mapper.SubjectMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SubjectRepositoryImpl implements com.shiyu.ai.education.port.repository.SubjectRepository {

    @Resource
    private SubjectMapper subjectMapper;

    public SubjectBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(subjectMapper.selectOneByQuery(QueryWrapper.create()
                .eq(SubjectDO::getTenantId, tenantId.value()).eq(SubjectDO::getId, id)), SubjectBO.class);
    }

    public SubjectBO selectByCode(TenantId tenantId, String code) {
        return MapstructUtils.convert(subjectMapper.selectOneByQuery(
                QueryWrapper.create().eq(SubjectDO::getTenantId, tenantId.value()).eq(SubjectDO::getCode, code)), SubjectBO.class);
    }

    public PageData<SubjectBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<SubjectDO> page = subjectMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq(SubjectDO::getTenantId, tenantId.value())
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), SubjectBO.class), page.getTotalRow());
    }

    public List<SubjectBO> selectByGradeLevel(TenantId tenantId, String gradeLevel) {
        return MapstructUtils.convert(subjectMapper.selectListByQuery(
                QueryWrapper.create().eq(SubjectDO::getTenantId, tenantId.value())
                        .eq("grade_level", gradeLevel)
        ), SubjectBO.class);
    }
    public List<SubjectBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(subjectMapper.selectListByQuery(QueryWrapper.create()
                .eq(SubjectDO::getTenantId, tenantId.value())), SubjectBO.class);
    }

    public int insert(TenantId tenantId, SubjectBO entity) {
        SubjectDO dataObj = MapstructUtils.convert(entity, SubjectDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(subjectMapper.insert(dataObj), "insert subject");
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(TenantId tenantId, SubjectBO entity) {
        SubjectDO dataObj = MapstructUtils.convert(entity, SubjectDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(subjectMapper.updateByQuery(dataObj, QueryWrapper.create()
                .eq(SubjectDO::getTenantId, tenantId.value()).eq(SubjectDO::getId, entity.getId())), "update subject");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(subjectMapper.deleteByQuery(QueryWrapper.create()
                .eq(SubjectDO::getTenantId, tenantId.value()).eq(SubjectDO::getId, id)), "delete subject");
    }

}

