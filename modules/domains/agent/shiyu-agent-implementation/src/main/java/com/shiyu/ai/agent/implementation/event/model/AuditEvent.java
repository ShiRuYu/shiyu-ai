package com.shiyu.ai.agent.implementation.event.model;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 审计事件
 *
 * <p>当关键操作发生时发布（登录、Agent 执行、模型调用、知识检索、CRUD 等）， 由 {@code AuditService} 异步消费后写入 {@code audit_log} 表。
 */
public class AuditEvent extends DomainEvent {

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private final TenantId tenantId;
    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private final Long userId;
    /**
     * action 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String action;
    /**
     * 目标类型，表示当前对象中的对应属性。
     */
    private final String targetType;
    /**
     * 目标标识，表示当前对象中的对应属性。
     */
    private final String targetId;
    /**
     * detail 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String detail;
    /**
     * ip 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String ip;
    /**
     * 结果，表示当前对象中的对应属性。
     */
    private final String result;
    /**
     * errorMsg 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String errorMsg;
    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long durationMs;

    /**
     * {@code AuditEvent} 创建并初始化当前类型实例。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     * @param action 参数值，用于执行当前操作。
     * @param targetType 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param detail 参数值，用于执行当前操作。
     * @param ip 参数值，用于执行当前操作。
     * @param result 参数值，用于执行当前操作。
     * @param errorMsg 参数值，用于执行当前操作。
     * @param durationMs 参数值，用于执行当前操作。
     */
    public AuditEvent(
            TenantId tenantId,
            Long userId,
            String action,
            String targetType,
            String targetId,
            String detail,
            String ip,
            String result,
            String errorMsg,
            long durationMs) {
        super("AUDIT");
        this.tenantId = tenantId;
        this.userId = userId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
        this.ip = ip;
        this.result = result;
        this.errorMsg = errorMsg;
        this.durationMs = durationMs;
    }

    /**
     * {@code getTenantId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * {@code getUserId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * {@code getAction} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getAction() {
        return action;
    }

    /**
     * {@code getTargetType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * {@code getTargetId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * {@code getDetail} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDetail() {
        return detail;
    }

    /**
     * {@code getIp} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getIp() {
        return ip;
    }

    /**
     * {@code getResult} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getResult() {
        return result;
    }

    /**
     * {@code getErrorMsg} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * {@code getDurationMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getDurationMs() {
        return durationMs;
    }
}
