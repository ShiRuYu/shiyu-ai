package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.record.implementation.persistence.dataobject.ProfileDO;
import com.shiyu.ai.record.implementation.persistence.mapper.ProfileMapper;
import com.shiyu.ai.record.domain.model.ProfileBO;
import com.shiyu.ai.record.domain.enums.GenderEnum;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 人物数据仓储层
 */
@Component
public class ProfileRepositoryImpl implements com.shiyu.ai.record.port.repository.ProfileRepository {

    @Resource
    private ProfileMapper profileMapper;

    /**
     * 分页查询人物列表
     */
    public Pair<Long, List<ProfileBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String createBy) {
        QueryWrapper countWrapper = QueryWrapper.create()
                .eq(ProfileDO::getTenantId, tenantId.value())
                .eq(ProfileDO::getDelFlag, 0);
        if (createBy != null && !createBy.isBlank()) {
            countWrapper.eq(ProfileDO::getCreateBy, createBy);
        }
        long total = profileMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ProfileDO::getTenantId, tenantId.value())
                .eq(ProfileDO::getDelFlag, 0);
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
    public ProfileBO selectById(TenantId tenantId, Long id) {
        ProfileDO profileDO = profileMapper.selectOneByQuery(QueryWrapper.create()
                .eq(ProfileDO::getTenantId, tenantId.value())
                .eq(ProfileDO::getId, id));
        ProfileBO bo = MapstructUtils.convert(profileDO, ProfileBO.class);
        if (bo != null) {
            bo.setGenderLabel(GenderEnum.getLabelByCode(bo.getGender()));
        }
        return bo;
    }

    /**
     * 创建人物
     */
    public ProfileBO insert(TenantId tenantId, ProfileBO profileBO) {
        ProfileDO profileDO = MapstructUtils.convert(profileBO, ProfileDO.class);
        profileDO.setTenantId(tenantId.value());
        profileMapper.insertSelective(profileDO);
        profileBO.setId(profileDO.getId());
        profileBO.setGenderLabel(GenderEnum.getLabelByCode(profileBO.getGender()));
        return profileBO;
    }

    /**
     * 更新人物
     */
    public boolean update(TenantId tenantId, ProfileBO profileBO) {
        ProfileDO profileDO = MapstructUtils.convert(profileBO, ProfileDO.class);
        profileDO.setTenantId(tenantId.value());
        return profileMapper.updateByQuery(profileDO, QueryWrapper.create()
                .eq(ProfileDO::getTenantId, tenantId.value())
                .eq(ProfileDO::getId, profileBO.getId())) > 0;
    }

    /**
     * 删除人物
     */
    public boolean deleteById(TenantId tenantId, Long id) {
        return profileMapper.deleteByQuery(QueryWrapper.create()
                .eq(ProfileDO::getTenantId, tenantId.value())
                .eq(ProfileDO::getId, id)) > 0;
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
