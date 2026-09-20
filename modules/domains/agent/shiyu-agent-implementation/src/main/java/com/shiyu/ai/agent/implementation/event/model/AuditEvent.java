package com.shiyu.ai.agent.implementation.event.model;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 表示 Audit 相关的领域事件或异常信息。
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
     * 执行 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     * @param action 用于完成本次业务处理的 action 参数。
     * @param targetType 用于完成本次业务处理的 targetType 参数。
     * @param targetId 用于定位target的标识。
     * @param detail 用于完成本次业务处理的 detail 参数。
     * @param ip 用于完成本次业务处理的 ip 参数。
     * @param result 用于完成本次业务处理的 result 参数。
     * @param errorMsg 用于完成本次业务处理的 errorMsg 参数。
     * @param durationMs 用于完成本次业务处理的 durationMs 参数。
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
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getAction() {
        return action;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getDetail() {
        return detail;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getIp() {
        return ip;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getResult() {
        return result;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 查询 Audit 相关业务数据，并返回处理结果。
     *
     * @return 返回 Audit 相关操作生成的结果数据。
     */
    public long getDurationMs() {
        return durationMs;
    }
}
