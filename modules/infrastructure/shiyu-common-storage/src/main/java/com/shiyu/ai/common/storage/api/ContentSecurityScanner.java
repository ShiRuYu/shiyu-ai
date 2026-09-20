package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

/**
 * 定义 Content 安全 Scanner 相关的协作契约和调用边界。
 */
public interface ContentSecurityScanner {

    /**
     * 校验内容securityscanner。
     *
     * @param fileName fileName 参数。
     * @param contentType contentType 参数。
     * @param content content 参数。
     */
    void validate(String fileName, String contentType, byte[] content);
}
