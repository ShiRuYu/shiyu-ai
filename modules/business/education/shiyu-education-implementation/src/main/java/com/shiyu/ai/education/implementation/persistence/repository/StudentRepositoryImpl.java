package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudentDO;
import com.shiyu.ai.education.implementation.persistence.mapper.StudentMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 学生 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class StudentRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository {

    /**
     * 学生映射器，表示当前对象中的对应属性。
     */
    @Resource private StudentMapper studentMapper;

    @Override
    public StudentBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(
                studentMapper.selectOneByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                StudentBO.class);
    }

    @Override
    public StudentBO selectByUserId(TenantId tenantId, Long userId) {
        return MapstructUtils.convert(
                studentMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("user_id", userId)),
                StudentBO.class);
    }

    @Override
    public PageData<StudentBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<StudentDO> page =
                studentMapper.paginate(
                        pageNum,
                        pageSize,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .orderBy("id", false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), StudentBO.class), page.getTotalRow());
    }

    @Override
    public List<StudentBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(
                studentMapper.selectListByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value())),
                StudentBO.class);
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
        return EducationWriteGuard.require(
                studentMapper.deleteByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                "delete student");
    }
}
