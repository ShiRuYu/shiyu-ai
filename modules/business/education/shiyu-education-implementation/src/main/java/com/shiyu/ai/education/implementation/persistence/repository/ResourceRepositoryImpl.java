package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.ResourceBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ResourceDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ResourceMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code ResourceRepositoryImpl} 实现教育模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class ResourceRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository.ResourceRepository {

    /**
     * 资源映射器，表示当前对象中的对应属性。
     */
    @Resource private ResourceMapper resourceMapper;

    public ResourceBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(
                resourceMapper.selectOneByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                ResourceBO.class);
    }

    public List<ResourceBO> selectBySubjectCode(TenantId tenantId, String subjectCode) {
        return MapstructUtils.convert(
                resourceMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("subject_code", subjectCode)
                                .eq("status", 1)),
                ResourceBO.class);
    }

    public List<ResourceBO> selectByType(TenantId tenantId, String type) {
        return MapstructUtils.convert(
                resourceMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("type", type)
                                .eq("status", 1)),
                ResourceBO.class);
    }

    public PageData<ResourceBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<ResourceDO> page =
                resourceMapper.paginate(
                        pageNum,
                        pageSize,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("status", 1)
                                .orderBy("id"));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), ResourceBO.class), page.getTotalRow());
    }

    public List<ResourceBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(
                resourceMapper.selectListByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("status", 1)),
                ResourceBO.class);
    }

    public int insert(TenantId tenantId, ResourceBO entity) {
        ResourceDO dataObj = MapstructUtils.convert(entity, ResourceDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(resourceMapper.insert(dataObj), "insert resource");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, ResourceBO entity) {
        ResourceDO dataObj = MapstructUtils.convert(entity, ResourceDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(
                resourceMapper.updateByQuery(
                        dataObj,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("id", entity.getId())),
                "update resource");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(
                resourceMapper.deleteByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                "delete resource");
    }
}
