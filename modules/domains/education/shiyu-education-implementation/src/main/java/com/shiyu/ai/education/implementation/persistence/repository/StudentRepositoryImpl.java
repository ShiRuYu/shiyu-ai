package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.StudentBO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudentDO;
import com.shiyu.ai.education.implementation.persistence.mapper.StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StudentRepositoryImpl implements com.shiyu.ai.education.port.repository.StudentRepository {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public StudentBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(studentMapper.selectOneByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), StudentBO.class);
    }

    @Override
    public StudentBO selectByUserId(TenantId tenantId, Long userId) {
        return MapstructUtils.convert(studentMapper.selectOneByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("user_id", userId)), StudentBO.class);
    }

    @Override
    public PageData<StudentBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<StudentDO> page = studentMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value())
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), StudentBO.class), page.getTotalRow());
    }

    @Override
    public List<StudentBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(studentMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value())), StudentBO.class);
    }

    @Override
    public int insert(TenantId tenantId, StudentBO entity) {
        StudentDO dataObj = MapstructUtils.convert(entity, StudentDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(studentMapper.insert(dataObj), "insert student");
        entity.setId(dataObj.getId());
        return rows;
    }

    @Override
    public int update(TenantId tenantId, StudentBO entity) {
        StudentDO dataObj = MapstructUtils.convert(entity, StudentDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(studentMapper.update(dataObj), "update student");
    }

    @Override
    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(studentMapper.deleteByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), "delete student");
    }

}

