package com.shiyu.ai.model.implementation.persistence;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.model.contract.api.ModelCatalogPort;
import com.shiyu.ai.model.implementation.persistence.mapper.AiModelMapper;
import com.shiyu.ai.model.implementation.persistence.mapper.AiPlatformMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 将 模型 Catalog 在不同层之间进行适配、转换或组装。
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
     * 执行 模型 Catalog 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Catalog 相关操作生成的结果数据。
     */
    @Override
    public long countEnabledPlatforms() {
        return platformMapper.selectCountByQuery(
                QueryWrapper.create().eq("status", 1).eq("del_flag", 0));
    }

    /**
     * 执行 模型 Catalog 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Catalog 相关操作生成的结果数据。
     */
    @Override
    public long countEnabledModels() {
        return modelMapper.selectCountByQuery(
                QueryWrapper.create().eq("status", 1).eq("del_flag", 0));
    }
}
