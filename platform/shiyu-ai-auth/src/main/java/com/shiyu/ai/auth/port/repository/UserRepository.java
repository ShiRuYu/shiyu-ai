package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface UserRepository {
    Pair<Long, List<UserBO>> selectPage(Number pageNo, Number pageSize, String username);
    UserBO selectByUsername(String username);
    UserBO selectById(Long id);
    UserBO insert(UserBO userBO);
    boolean update(UserBO userBO);
    boolean deleteById(Long id);
    List<RoleBO> selectRolesByUserId(Long userId);
    UserBO selectByEmail(String email);
    UserBO selectActiveUserByUsername(String username);
    boolean isUserInScope(Long userId, Long currentTenantId);
}
