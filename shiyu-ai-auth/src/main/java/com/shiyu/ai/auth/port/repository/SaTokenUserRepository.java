package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.UserBO;
import java.time.LocalDateTime;

public interface SaTokenUserRepository {
    UserBO selectById(Long userId);
    void updateExtInfo(UserBO user);
}
