package com.shiyu.ai.common.core.exception.file;

import java.io.Serial;

/** 文件名称超长限制异常类 */
public class FileNameLengthLimitExceededException extends FileException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength) {
        super("upload.filename.exceed.length", new Object[] {defaultFileNameLength});
    }
}
