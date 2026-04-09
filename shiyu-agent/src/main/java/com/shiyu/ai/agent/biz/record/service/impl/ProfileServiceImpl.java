package com.shiyu.ai.agent.biz.record.service.impl;

import com.shiyu.ai.agent.domain.bo.ProfileBO;
import com.shiyu.ai.agent.biz.record.repository.ProfileRepository;
import com.shiyu.ai.agent.biz.record.service.ProfileService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 人物服务实现
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private ProfileRepository profileRepository;

    @Override
    public Pair<Long, List<ProfileBO>> getPage(Integer pageNo, Integer pageSize, Long creatorId) {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        return profileRepository.selectPage(pageNo, pageSize, creatorId);
    }

    @Override
    public ProfileBO getById(Long id) {
        return profileRepository.selectById(id);
    }

    @Override
    public ProfileBO create(ProfileBO profileBO) {
        profileBO.setDelFlag(0);
        profileBO.setStatus("1");
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
