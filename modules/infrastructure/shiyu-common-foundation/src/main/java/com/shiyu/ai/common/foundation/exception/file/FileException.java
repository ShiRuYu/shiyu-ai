package com.shiyu.ai.common.foundation.exception.file;

import com.shiyu.ai.common.foundation.exception.base.BaseException;

import java.io.Serial;

/**
 * 表示 文件 相关的领域事件或异常信息。
 */
public class FileException extends BaseException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args) {
        super("file", code, args, null);
    }
}
