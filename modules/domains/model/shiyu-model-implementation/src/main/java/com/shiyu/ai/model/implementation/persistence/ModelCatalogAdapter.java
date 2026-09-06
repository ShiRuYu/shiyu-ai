package com.shiyu.ai.model.implementation.persistence;

import com.shiyu.ai.model.implementation.persistence.mapper.AiModelMapper;
import com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.model.port.ModelCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapts the Model-owned repositories to its public read-only contract. */
@Component
@RequiredArgsConstructor
public class ModelCatalogAdapter implements ModelCatalogPort {
    private final AiPlatformMapper platformMapper;
    private final AiModelMapper modelMapper;

    @Override
    public long countEnabledPlatforms() {
        return platformMapper.selectCountByQuery(QueryWrapper.create()
                .eq("status", 1).eq("del_flag", 0));
    }

    @Override
    public long countEnabledModels() {
        return modelMapper.selectCountByQuery(QueryWrapper.create()
                .eq("status", 1).eq("del_flag", 0));
    }
}
