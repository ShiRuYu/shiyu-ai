package com.shiyu.ai.agent.biz.record.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.biz.auth.util.TenantWorkspaceHelper;
import com.shiyu.ai.agent.dal.dataobject.record.ProfileDO;
import com.shiyu.ai.agent.dal.mapper.record.ProfileMapper;
import com.shiyu.ai.agent.domain.bo.ProfileBO;
import com.shiyu.ai.common.core.enums.GenderEnum;
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
    public Pair<Long, List<ProfileBO>> selectPage(Number pageNo, Number pageSize, String createBy) {
        QueryWrapper countWrapper = QueryWrapper.create()
                .eq(ProfileDO::getDelFlag, 0);
        TenantWorkspaceHelper.applyWorkspaceFilter(countWrapper);
        if (createBy != null && !createBy.isBlank()) {
            countWrapper.eq(ProfileDO::getCreateBy, createBy);
        }
        long total = profileMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ProfileDO::getDelFlag, 0);
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        if (createBy != null && !createBy.isBlank()) {
            queryWrapper.eq(ProfileDO::getCreateBy, createBy);
        }
        queryWrapper.orderBy(ProfileDO::getCreateTime, false);
        
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<ProfileDO> profileDOs = profileMapper.selectListByQuery(queryWrapper);
        List<ProfileBO> profileBOs = MapstructUtils.convert(profileDOs, ProfileBO.class);
        fillGenderLabel(profileBOs);
        
        return Pair.of(total, profileBOs);
    }

    /**
     * 根据ID查询人物
     */
    public ProfileBO selectById(Long id) {
        ProfileDO profileDO = profileMapper.selectOneById(id);
        ProfileBO bo = MapstructUtils.convert(profileDO, ProfileBO.class);
        if (bo != null) {
            bo.setGenderLabel(GenderEnum.getLabelByCode(bo.getGender()));
        }
        return bo;
    }

    /**
     * 创建人物
     */
    public ProfileBO insert(ProfileBO profileBO) {
        ProfileDO profileDO = MapstructUtils.convert(profileBO, ProfileDO.class);
        profileMapper.insertSelective(profileDO);
        profileBO.setId(profileDO.getId());
        profileBO.setGenderLabel(GenderEnum.getLabelByCode(profileBO.getGender()));
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

    /**
     * 填充性别标签
     */
    private void fillGenderLabel(List<ProfileBO> list) {
        if (list == null) {
            return;
        }
        for (ProfileBO bo : list) {
            bo.setGenderLabel(GenderEnum.getLabelByCode(bo.getGender()));
        }
    }
}
