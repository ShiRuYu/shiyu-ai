package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.dal.record.bo.ProfileBO;
import com.shiyu.ai.dal.record.repository.ProfileRepository;
import com.shiyu.ai.record.service.ProfileService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 浜虹墿鏈嶅姟瀹炵幇
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private ProfileRepository profileRepository;

    @Override
    public Pair<Long, List<ProfileBO>> getPage(Number pageNo, Number pageSize, String createBy) {
        if (pageNo == null || pageNo.intValue() < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize.intValue() < 1) {
            pageSize = 10;
        }
        return profileRepository.selectPage(pageNo, pageSize, createBy);
    }

    @Override
    public ProfileBO getById(Long id) {
        return profileRepository.selectById(id);
    }

    @Override
    public ProfileBO create(ProfileBO profileBO) {
        profileBO.setDelFlag(0);
        profileBO.setStatus(1);
        return profileRepository.insert(profileBO);
    }

    @Override
    public boolean update(ProfileBO profileBO) {
        return profileRepository.update(profileBO);
    }

    @Override
    public boolean delete(Long id) {
        return profileRepository.deleteById(id);
    }
}
