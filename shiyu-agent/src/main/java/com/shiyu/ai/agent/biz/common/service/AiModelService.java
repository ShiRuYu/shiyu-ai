package com.shiyu.ai.agent.biz.common.service;

import com.shiyu.ai.agent.domain.bo.AiModelBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AI 模型服务层
 */
public interface AiModelService {

    /**
     * 分页查询模型列表
     *
     * @param platformId 平台ID（可选）
     * @param pageNo     页码
     * @param pageSize   每页数量
     */
    Pair<Long, List<AiModelBO>> getPage(Long platformId, Integer pageNo, Integer pageSize);

    /**
     * 查询指定平台下所有启用的模型
     */
    List<AiModelBO> getByPlatformId(Long platformId);

    /**
     * 根据 ID 查询模型详情
     */
    AiModelBO getById(Long id);

    /**
     * 获取平台的默认模型
     */
    AiModelBO getDefaultByPlatformId(Long platformId);

    /**
     * 创建模型
     */
    AiModelBO create(AiModelBO bo);

    /**
     * 更新模型
     */
    AiModelBO update(AiModelBO bo);

    /**
     * 删除模型
     */
    void deleteById(Long id);

    /**
     * 批量删除模型
     */
    void deleteByIds(List<Long> ids);

    /**
     * 设置为平台默认模型
     */
    AiModelBO setDefault(Long id);
}
