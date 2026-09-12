package com.shiyu.ai.knowledge.implementation.infrastructure.storage;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 为知识模块生成备份清单中的索引和文档条目。
 */
@Component
@RequiredArgsConstructor
public class KnowledgeBackupManifestContributor implements BackupManifestContributor {

    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final KnowledgeEnterpriseRepository repository;

    /**
     * {@code contribute} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String contribute() {
        StringBuilder manifest = new StringBuilder();
        for (KnowledgeSpaceBO space : repository.findAllActiveSpaces()) {
            manifest.append("knowledge.activeIndex.")
                    .append(space.getTenantId())
                    .append('.')
                    .append(space.getId())
                    .append('=')
                    .append(
                            space.getActiveIndexVersion() == null
                                    ? ""
                                    : space.getActiveIndexVersion())
                    .append('\n');
        }
        return manifest.toString();
    }
}
