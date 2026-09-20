package com.shiyu.ai.common.core.exception.base;

import com.shiyu.ai.common.core.utils.MessageUtils;
import com.shiyu.ai.common.core.utils.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 表示 Base 相关的领域事件或异常信息。
 */
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
     * 执行 Base 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param module 用于完成本次业务处理的 module 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param args 用于完成本次业务处理的 args 参数。
     * @param defaultMessage 用于完成本次业务处理的 defaultMessage 参数。
     */
    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 执行 Base 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param module 用于完成本次业务处理的 module 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param args 用于完成本次业务处理的 args 参数。
     */
    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    /**
     * 执行 Base 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param module 用于完成本次业务处理的 module 参数。
     * @param defaultMessage 用于完成本次业务处理的 defaultMessage 参数。
     */
    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    /**
     * 执行 Base 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param args 用于完成本次业务处理的 args 参数。
     */
    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    /**
     * 执行 Base 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param defaultMessage 用于完成本次业务处理的 defaultMessage 参数。
     */
    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    /**
     * 查询 Base 相关业务数据，并返回处理结果。
     *
     * @return 返回 Base 相关操作生成的结果数据。
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
