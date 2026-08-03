package com.shiyu.ai.knowledge.storage;

import com.shiyu.ai.common.storage.BackupManifestContributor;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adds Knowledge's active index versions to the generic Storage backup manifest. */
@Component
@RequiredArgsConstructor
public class KnowledgeBackupManifestContributor implements BackupManifestContributor {

    private final KnowledgeEnterpriseRepository repository;

    @Override
    public String contribute() {
        StringBuilder manifest = new StringBuilder();
        for (KnowledgeSpaceDO space : repository.findAllActiveSpaces()) {
            manifest.append("knowledge.activeIndex.")
                    .append(space.getTenantId()).append('.')
                    .append(space.getId()).append('=')
                    .append(space.getActiveIndexVersion() == null ? "" : space.getActiveIndexVersion())
                    .append('\n');
        }
        return manifest.toString();
    }
}
