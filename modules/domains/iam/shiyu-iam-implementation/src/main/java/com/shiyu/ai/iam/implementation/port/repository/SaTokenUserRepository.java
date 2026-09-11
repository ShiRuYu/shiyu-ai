package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;

public interface SaTokenUserRepository {
    UserBO selectById(Long userId);

    void updateExtInfo(UserBO user);
}
