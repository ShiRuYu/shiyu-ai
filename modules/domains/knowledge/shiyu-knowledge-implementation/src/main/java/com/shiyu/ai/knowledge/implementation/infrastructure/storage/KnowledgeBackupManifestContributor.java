package com.shiyu.ai.knowledge.implementation.infrastructure.storage;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
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
 * 向 知识 Backup Manifest 所属的应用或基础设施注册必要的扩展能力。
 */
@Component
@RequiredArgsConstructor
public class KnowledgeBackupManifestContributor implements BackupManifestContributor {

    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final KnowledgeEnterpriseRepository repository;

    /**
     * 执行 知识 Backup Manifest 相关业务数据，并返回处理结果。
     *
     * @return 返回 知识 Backup Manifest 相关操作生成的结果数据。
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
