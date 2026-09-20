package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/**
 * 表示 Base Biz 相关的领域事件或异常信息。
 */
public abstract class BaseBizException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 消息，表示当前对象中的对应属性。
     */
    private String message;

    /**
     * detailMessage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String detailMessage;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private Integer code;

    /**
     * {@code BaseBizException} 创建并初始化当前类型实例。
     */
    protected BaseBizException() {}

    /**
     * 执行 Base Biz 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    protected BaseBizException(String message) {
        this.message = message;
    }

    /**
     * 执行 Base Biz 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param code 用于定位或筛选目标业务对象的业务值。
     */
    protected BaseBizException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     * 查询 Base Biz 相关业务数据，并返回处理结果。
     *
     * @return 返回 Base Biz 相关操作生成的结果数据。
     */
    public String getDetailMessage() {
        return detailMessage;
    }

    /**
     * 更新或设置 Base Biz 相关业务数据，并返回处理结果。
     *
     * @param detailMessage 用于完成本次业务处理的 detailMessage 参数。
     * @return 返回 Base Biz 相关操作生成的结果数据。
     */
    public BaseBizException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    /**
     * 查询 Base Biz 相关业务数据，并返回处理结果。
     *
     * @return 返回 Base Biz 相关操作生成的结果数据。
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 更新或设置 Base Biz 相关业务数据，并返回处理结果。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @return 返回 Base Biz 相关操作生成的结果数据。
     */
    public BaseBizException setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 查询 Base Biz 相关业务数据，并返回处理结果。
     *
     * @return 返回 Base Biz 相关操作生成的结果数据。
     */
    public Integer getCode() {
        return code;
    }
}
