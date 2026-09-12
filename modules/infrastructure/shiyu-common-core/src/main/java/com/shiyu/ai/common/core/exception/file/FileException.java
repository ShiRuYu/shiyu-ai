package com.shiyu.ai.common.core.exception.file;

import com.shiyu.ai.common.core.exception.base.BaseException;

import java.io.Serial;

/** 文件信息异常类 */
public class FileException extends BaseException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args) {
        super("file", code, args, null);
    }
}
