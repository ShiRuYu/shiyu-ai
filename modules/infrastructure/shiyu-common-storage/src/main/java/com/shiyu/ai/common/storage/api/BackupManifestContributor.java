package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

/**
 * 向 Backup Manifest 所属的应用或基础设施注册必要的扩展能力。
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
