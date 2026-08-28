package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.record.domain.model.TagBO;
import com.shiyu.ai.record.port.repository.TagRepository;
import com.shiyu.ai.record.request.TagRequest;
import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.record.vo.TagVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    @Override public Pair<Long,List<TagVO>> pageView(ActorContext actor,Number n,Number s,String name){actor=requireActor(actor);if(n==null||n.intValue()<1)n=1;if(s==null||s.intValue()<1)s=10;var p=tagRepository.selectPage(actor.tenantId(),n,s,name);return Pair.of(p.getLeft(),MapstructUtils.convert(p.getRight(),TagVO.class));}
    @Override public List<TagVO> allView(ActorContext actor){return MapstructUtils.convert(tagRepository.selectAll(requireActor(actor).tenantId()),TagVO.class);}
    @Override public TagVO detailView(ActorContext actor,Long id){return MapstructUtils.convert(tagRepository.selectById(requireActor(actor).tenantId(),id),TagVO.class);}
    @Override public TagVO create(ActorContext actor,TagRequest r){TagBO b=new TagBO();b.setName(r.getName());return MapstructUtils.convert(tagRepository.insert(requireActor(actor).tenantId(),b),TagVO.class);}
    @Override public boolean update(ActorContext actor,Long id,TagRequest r){actor=requireActor(actor);TagBO b=tagRepository.selectById(actor.tenantId(),id);if(b==null)return false;b.setName(r.getName());return tagRepository.update(actor.tenantId(),b);}
    @Override public boolean delete(ActorContext actor,Long id){return tagRepository.deleteById(requireActor(actor).tenantId(),id);}
    private static ActorContext requireActor(ActorContext actor){return Objects.requireNonNull(actor,"actor is required");}
}
