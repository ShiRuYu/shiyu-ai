package com.shiyu.ai.common.core.exception.base;

import com.shiyu.ai.common.core.utils.MessageUtils;
import com.shiyu.ai.common.core.utils.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/** 基础异常 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuppressWarnings("serial")
public class BaseException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 所属模块 */
    private String module;

    /** 错误码 */
    private String code;

    /** 错误码对应的参数 */
    private Object[] args;

    /** 错误消息 */
    private String defaultMessage;

    /**
     * {@code BaseException} 创建并初始化当前类型实例。
     *
     * @param module 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param args 参数值，用于执行当前操作。
     * @param defaultMessage 参数值，用于执行当前操作。
     */
    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    /**
     * {@code BaseException} 创建并初始化当前类型实例。
     *
     * @param module 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param args 参数值，用于执行当前操作。
     */
    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    /**
     * {@code BaseException} 创建并初始化当前类型实例。
     *
     * @param module 参数值，用于执行当前操作。
     * @param defaultMessage 参数值，用于执行当前操作。
     */
    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    /**
     * {@code BaseException} 创建并初始化当前类型实例。
     *
     * @param code 参数值，用于执行当前操作。
     * @param args 参数值，用于执行当前操作。
     */
    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    /**
     * {@code BaseException} 创建并初始化当前类型实例。
     *
     * @param defaultMessage 参数值，用于执行当前操作。
     */
    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    /**
     * {@code getMessage} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getMessage() {
        String message = null;
        if (!StringUtils.isEmpty(code)) {
            message = MessageUtils.message(code, args);
        }
        if (message == null) {
            message = defaultMessage;
        }
        return message;
    }
}
