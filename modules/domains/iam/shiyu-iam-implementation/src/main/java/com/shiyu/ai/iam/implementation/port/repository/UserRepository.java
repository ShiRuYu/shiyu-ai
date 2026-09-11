package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface UserRepository {
    Pair<Long, List<UserBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String username);

    UserBO selectByUsername(String username);

    UserBO selectById(Long id);

    UserBO insert(UserBO userBO);

    boolean update(UserBO userBO);

    boolean deleteById(Long id);

    List<RoleBO> selectRolesByUserId(Long userId);

    UserBO selectByEmail(String email);

    UserBO selectActiveUserByUsername(String username);

    boolean isUserInScope(Long userId, TenantId currentTenantId);
}
