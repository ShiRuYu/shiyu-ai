package com.shiyu.ai.auth;

import com.shiyu.ai.auth.request.UserRequest;
import com.shiyu.ai.auth.service.impl.UserServiceImpl;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTenantGuardTest {

    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(7L), new UserId(11L), false);

    @Test
    void createRejectsMissingTargetTenantBeforePersistence() {
        UserServiceImpl service = new UserServiceImpl(null, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> service.createUser(ACTOR, new UserRequest(), null, null));
    }

    @Test
    void updateRejectsMissingTargetTenantBeforePersistence() {
        UserServiceImpl service = new UserServiceImpl(null, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> service.updateUser(ACTOR, 42L, new UserRequest(), null, null));
    }
}
