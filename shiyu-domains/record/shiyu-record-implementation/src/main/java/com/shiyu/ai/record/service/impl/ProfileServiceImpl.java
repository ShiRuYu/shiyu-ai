package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.record.domain.model.ProfileBO;
import com.shiyu.ai.record.port.repository.ProfileRepository;
import com.shiyu.ai.record.request.ProfileRequest;
import com.shiyu.ai.record.service.ProfileService;
import com.shiyu.ai.record.vo.ProfileVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
    @Override public Pair<Long, List<ProfileVO>> pageView(ActorContext actor, Number n, Number s, String c) { actor=requireActor(actor); if(n==null||n.intValue()<1)n=1; if(s==null||s.intValue()<1)s=10; var p=profileRepository.selectPage(actor.tenantId(),n,s,c); return Pair.of(p.getLeft(),MapstructUtils.convert(p.getRight(),ProfileVO.class)); }
    @Override public ProfileVO detailView(ActorContext actor, Long id) { return MapstructUtils.convert(profileRepository.selectById(requireActor(actor).tenantId(),id),ProfileVO.class); }
    @Override public ProfileVO create(ActorContext actor, ProfileRequest r) { actor=requireActor(actor); ProfileBO b=new ProfileBO(); b.setName(r.getName()); b.setAvatar(r.getAvatar()); b.setDelFlag(0); b.setStatus(1); return MapstructUtils.convert(profileRepository.insert(actor.tenantId(),b),ProfileVO.class); }
    @Override public boolean update(ActorContext actor, Long id, ProfileRequest r) { actor=requireActor(actor); ProfileBO b=profileRepository.selectById(actor.tenantId(),id); if(b==null)return false; b.setName(r.getName()); b.setAvatar(r.getAvatar()); return profileRepository.update(actor.tenantId(),b); }
    @Override public boolean delete(ActorContext actor, Long id) { return profileRepository.deleteById(requireActor(actor).tenantId(),id); }
    private static ActorContext requireActor(ActorContext actor) { return Objects.requireNonNull(actor,"actor is required"); }
}
