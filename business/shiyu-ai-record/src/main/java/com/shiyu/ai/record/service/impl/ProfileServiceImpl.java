package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.record.domain.model.ProfileBO;
import com.shiyu.ai.record.port.repository.ProfileRepository;
import com.shiyu.ai.record.service.ProfileService;
import com.shiyu.ai.record.request.ProfileRequest;
import com.shiyu.ai.record.vo.ProfileVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 浜虹墿鏈嶅姟瀹炵幇
 */
@Service
public class ProfileServiceImpl implements ProfileService {
    @Override public Pair<Long, List<ProfileVO>> pageView(Number n, Number s, String c) { var p=getPage(n,s,c); return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), ProfileVO.class)); }
    @Override public ProfileVO detailView(Long id) { return MapstructUtils.convert(getById(id), ProfileVO.class); }
    @Override public ProfileVO create(ProfileRequest r) { ProfileBO b=new ProfileBO(); b.setName(r.getName()); b.setAvatar(r.getAvatar()); return MapstructUtils.convert(create(b), ProfileVO.class); }
    @Override public boolean update(Long id, ProfileRequest r) { ProfileBO b=getById(id); if(b==null)return false; b.setName(r.getName()); b.setAvatar(r.getAvatar()); return update(b); }

    @Resource
    private ProfileRepository profileRepository;

    private Pair<Long, List<ProfileBO>> getPage(Number pageNo, Number pageSize, String createBy) {
        if (pageNo == null || pageNo.intValue() < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize.intValue() < 1) {
            pageSize = 10;
        }
        return profileRepository.selectPage(pageNo, pageSize, createBy);
    }

    private ProfileBO getById(Long id) {
        return profileRepository.selectById(id);
    }

    private ProfileBO create(ProfileBO profileBO) {
        profileBO.setDelFlag(0);
        profileBO.setStatus(1);
        return profileRepository.insert(profileBO);
    }

    private boolean update(ProfileBO profileBO) {
        return profileRepository.update(profileBO);
    }

    @Override
    public boolean delete(Long id) {
        return profileRepository.deleteById(id);
    }
}
