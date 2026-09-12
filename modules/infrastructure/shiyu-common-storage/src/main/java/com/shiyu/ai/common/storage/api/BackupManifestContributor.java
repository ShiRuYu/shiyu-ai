package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

/**
 * BackupManifestContributor 接口，定义基础设施模块的能力边界。
 */
@FunctionalInterface
public interface BackupManifestContributor {

    /**
     * 处理contribute。
     *
     * @return 处理结果。
     */
    String contribute();
}
