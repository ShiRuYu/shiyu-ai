package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/**
 * 表示 Service 相关的领域事件或异常信息。
 */
public final class ServiceException extends BaseBizException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public ServiceException() {}

    /**
     * 执行 Service 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * 执行 Service 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param code 用于定位或筛选目标业务对象的业务值。
     */
    public ServiceException(String message, Integer code) {
        super(message, code);
    }

    /**
     * 更新或设置 Service 相关业务数据，并返回处理结果。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @return 返回 Service 相关操作生成的结果数据。
     */
    @Override
    public ServiceException setMessage(String message) {
        return (ServiceException) super.setMessage(message);
    }

    /**
     * 更新或设置 Service 相关业务数据，并返回处理结果。
     *
     * @param detailMessage 用于完成本次业务处理的 detailMessage 参数。
     * @return 返回 Service 相关操作生成的结果数据。
     */
    @Override
    public ServiceException setDetailMessage(String detailMessage) {
        return (ServiceException) super.setDetailMessage(detailMessage);
    }
}
