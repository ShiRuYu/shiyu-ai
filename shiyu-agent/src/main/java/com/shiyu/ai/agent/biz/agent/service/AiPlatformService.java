package com.shiyu.ai.agent.biz.agent.service;

import com.shiyu.ai.agent.domain.bo.AiPlatformBO;
import com.shiyu.ai.agent.domain.vo.IdNameOptionVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AI 平台服务层
 */
public interface AiPlatformService {

    /**
     * 分页查询平台列表
     */
    Pair<Long, List<AiPlatformBO>> getPage(Number pageNo, Number pageSize, String name, String code);

    /**
     * 查询所有启用的平台
     */
    List<AiPlatformBO> getAllEnabled();

    /**
     * 根据 ID 查询平台详情
     */
    AiPlatformBO getById(Long id);

    /**
     * 根据编码查询平台
     */
    AiPlatformBO getByCode(String code);

    /**
     * 获取默认平台
     */
    AiPlatformBO getDefault();

    /**
     * 创建平台
     */
    AiPlatformBO create(AiPlatformBO bo);

    /**
     * 更新平台
     */
    AiPlatformBO update(AiPlatformBO bo);

    /**
     * 删除平台
     */
    void deleteById(Long id);

    /**
     * 查询启用的平台下拉选项（id + name）
     */
    List<IdNameOptionVO> getOptions();

    /**
     * 设置为默认平台
     */
    AiPlatformBO setDefault(Long id);
}
