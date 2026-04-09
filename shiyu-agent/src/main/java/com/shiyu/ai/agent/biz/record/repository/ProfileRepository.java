package com.shiyu.ai.agent.biz.record.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.record.ProfileDO;
import com.shiyu.ai.agent.dal.mapper.record.ProfileMapper;
import com.shiyu.ai.agent.domain.bo.ProfileBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 人物数据仓储层
 */
@Component
public class ProfileRepository {

    @Resource
    private ProfileMapper profileMapper;

    /**
     * 分页查询人物列表
     */
    public Pair<Long, List<ProfileBO>> selectPage(Integer pageNo, Integer pageSize, Long creatorId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ProfileDO::getDelFlag, 0);
        
        if (creatorId != null) {
            queryWrapper.eq(ProfileDO::getCreatorId, creatorId);
        }
        
        queryWrapper.orderBy(ProfileDO::getCreateTime, false);
        
        long total = profileMapper.selectCountByQuery(queryWrapper);
        
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<ProfileDO> profileDOs = profileMapper.selectListByQuery(queryWrapper);
        List<ProfileBO> profileBOs = MapstructUtils.convert(profileDOs, ProfileBO.class);
        
        return Pair.of(total, profileBOs);
    }

    /**
     * 根据ID查询人物
     */
    public ProfileBO selectById(Long id) {
        ProfileDO profileDO = profileMapper.selectOneById(id);
        return MapstructUtils.convert(profileDO, ProfileBO.class);
    }

    /**
     * 创建人物
     */
    public ProfileBO insert(ProfileBO profileBO) {
        ProfileDO profileDO = MapstructUtils.convert(profileBO, ProfileDO.class);
        profileMapper.insert(profileDO);
        profileBO.setId(profileDO.getId());
        return profileBO;
    }

    /**
     * 更新人物
     */
    public boolean update(ProfileBO profileBO) {
        ProfileDO profileDO = MapstructUtils.convert(profileBO, ProfileDO.class);
        return profileMapper.update(profileDO) > 0;
    }

    /**
     * 删除人物
     */
    public boolean deleteById(Long id) {
        return profileMapper.deleteById(id) > 0;
    }
}
