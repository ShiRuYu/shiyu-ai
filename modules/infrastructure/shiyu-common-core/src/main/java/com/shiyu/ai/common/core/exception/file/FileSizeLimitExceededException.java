package com.shiyu.ai.common.core.exception.file;

import java.io.Serial;

/**
 * 表示 文件 Size Limit Exceeded 相关的领域事件或异常信息。
 */
public class FileSizeLimitExceededException extends FileException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public FileSizeLimitExceededException(long defaultMaxSize) {
        super("upload.exceed.maxSize", new Object[] {defaultMaxSize});
    }
}
