package com.shiyu.ai.model.implementation.persistence;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.model.contract.api.ModelCatalogPort;
import com.shiyu.ai.model.implementation.persistence.mapper.AiModelMapper;
import com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 将模型目录查询适配到模型目录端口。
 */
@Component
@RequiredArgsConstructor
public class ModelCatalogAdapter implements ModelCatalogPort {
    /**
     * platformMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiPlatformMapper platformMapper;
    /**
     * modelMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiModelMapper modelMapper;

    /**
     * {@code countEnabledPlatforms} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long countEnabledPlatforms() {
        return platformMapper.selectCountByQuery(
                QueryWrapper.create().eq("status", 1).eq("del_flag", 0));
    }

    /**
     * {@code countEnabledModels} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long countEnabledModels() {
        return modelMapper.selectCountByQuery(
                QueryWrapper.create().eq("status", 1).eq("del_flag", 0));
    }
}
