package com.shiyu.ai.agent.biz.record.service;

import com.shiyu.ai.agent.domain.bo.ProfileBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 人物服务接口
 */
public interface ProfileService {

    /**
     * 分页查询人物列表
     */
    Pair<Long, List<ProfileBO>> getPage(Number pageNo, Number pageSize, String createBy);

    /**
     * 根据ID查询人物
     */
    ProfileBO getById(Long id);

    /**
     * 创建人物
     */
    ProfileBO create(ProfileBO profileBO);

    /**
     * 更新人物
     */
    boolean update(ProfileBO profileBO);

    /**
     * 删除人物
     */
    boolean delete(Long id);
}
