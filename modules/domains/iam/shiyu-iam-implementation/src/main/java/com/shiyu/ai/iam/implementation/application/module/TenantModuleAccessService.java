package com.shiyu.ai.iam.implementation.application.module;

import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import com.shiyu.ai.iam.contract.module.TenantModuleAccessProvisioning;
import com.shiyu.ai.iam.contract.module.TenantModuleAccessPort;
import com.shiyu.ai.iam.implementation.port.repository.TenantModuleAccessRepository;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/** IAM 租户模块授权应用服务，统一校验模块标识后委托租户隔离仓储。 */
@Component
public final class TenantModuleAccessService
        implements TenantModuleAccessPort, TenantModuleAccessProvisioning {

    private final TenantModuleAccessRepository repository;
    private final List<BusinessModuleDescriptor> moduleDescriptors;

    /**
     * 创建租户模块授权服务。
     *
     * @param repository 租户模块授权仓储。
     */
    public TenantModuleAccessService(TenantModuleAccessRepository repository) {
        this(repository, List.of());
    }

    /**
     * 创建租户模块授权服务。
     *
     * @param repository 租户模块授权仓储
     * @param moduleDescriptors 当前进程已装配的业务模块描述
     */
    @Autowired
    public TenantModuleAccessService(
            TenantModuleAccessRepository repository,
            List<BusinessModuleDescriptor> moduleDescriptors) {
        this.repository = repository;
        this.moduleDescriptors = List.copyOf(moduleDescriptors);
    }

    @Override
    public boolean isEnabled(TenantId tenantId, String moduleId) {
        if (tenantId == null || moduleId == null || moduleId.isBlank()) {
            throw new IllegalArgumentException("tenantId and moduleId are required");
        }
        return repository.isEnabled(tenantId, moduleId.trim());
    }

    @Override
    public void initializeTenantDefaults(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        repository.initializeTenantDefaults(
                tenantId, moduleDescriptors.stream().map(BusinessModuleDescriptor::id).toList());
    }
}
