package com.shiyu.ai.dal.util;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;

import java.util.List;

public final class TenantWorkspaceHelper {

    private TenantWorkspaceHelper() {}

    public static <T extends QueryWrapper> T applyWorkspaceFilter(T qw) {
        LoginUser user = LoginContextHolder.getContext();
        if (user == null) return qw;
        if (user.isSuperAdmin() && user.getCurrentWorkspaceId() == null) {
            return qw;
        }
        List<Long> wsIds = user.getWorkspaceIds();
        if (wsIds == null || wsIds.isEmpty()) {
            qw.eq("workspace_id", -1);
            return qw;
        }
        if (user.getCurrentWorkspaceId() != null) {
            qw.eq("workspace_id", user.getCurrentWorkspaceId());
        } else {
            qw.in("workspace_id", wsIds.toArray());
        }
        return qw;
    }
}
