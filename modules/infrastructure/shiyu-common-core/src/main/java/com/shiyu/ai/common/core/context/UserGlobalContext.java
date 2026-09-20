package com.shiyu.ai.common.core.context;

import com.shiyu.ai.common.core.context.model.UserContext;

/**
 * 实现 用户 Global 相关的业务处理、协作逻辑或基础设施能力。
 */
public class UserGlobalContext {
    private static final ThreadLocal<UserContext> USER_HOLDER = new InheritableThreadLocal<>();

    /**
     * 更新或设置 用户 Global 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param user 当前操作涉及的用户标识。
     */
    public static void set(UserContext user) {
        USER_HOLDER.set(user);
    }

    /**
     * 查询 用户 Global 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Global 相关操作生成的结果数据。
     */
    public static UserContext get() {
        return USER_HOLDER.get();
    }

    /**
     * 删除或移除 用户 Global 相关业务操作，并维护必要的状态和协作关系。
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
