package com.shiyu.ai.iam.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantModuleAccessDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.TenantModuleAccessMapper;
import com.shiyu.ai.iam.implementation.port.repository.TenantModuleAccessRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Collection;

/** 租户模块授权仓储实现，始终在当前 TenantScope 内读取模块状态。 */
@Component
public final class TenantModuleAccessRepositoryImpl implements TenantModuleAccessRepository {

    @Resource
    private TenantModuleAccessMapper mapper;

    @Override
    public boolean isEnabled(TenantId tenantId, String moduleId) {
        if (tenantId == null || moduleId == null || moduleId.isBlank()) {
            throw new IllegalArgumentException("tenantId and moduleId are required");
        }
        TenantScope.requireMatches(tenantId);
        TenantModuleAccessDO access = mapper.selectOneByQuery(
                QueryWrapper.create()
                        .where(TenantModuleAccessDO::getTenantId).eq(tenantId.value())
                        .and(TenantModuleAccessDO::getModuleId).eq(moduleId.trim())
                        .and(TenantModuleAccessDO::getStatus).eq(1));
        return access != null && Objects.equals(access.getStatus(), 1);
    }

    @Override
    public void initializeTenantDefaults(TenantId tenantId, Collection<String> moduleIds) {
        TenantScope.requireMatches(tenantId);
        if (moduleIds == null) {
            return;
        }
        for (String moduleId : moduleIds) {
            if (moduleId == null || moduleId.isBlank()) {
                continue;
            }
            String normalizedModuleId = moduleId.trim();
            TenantModuleAccessDO existing = mapper.selectOneByQuery(
                    QueryWrapper.create()
                            .where(TenantModuleAccessDO::getTenantId).eq(tenantId.value())
                            .and(TenantModuleAccessDO::getModuleId).eq(normalizedModuleId));
            if (existing != null) {
                continue;
            }
            TenantModuleAccessDO access = new TenantModuleAccessDO();
            access.setTenantId(tenantId.value());
            access.setModuleId(normalizedModuleId);
            access.setStatus(1);
            mapper.insert(access);
        }
    }
}
