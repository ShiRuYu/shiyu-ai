package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRepository {

    @Resource
    private KnowledgeMapper knowledgeMapper;

    public KnowledgeBO findById(Long id) {
        KnowledgeDO d = knowledgeMapper.selectOneById(id);
        return d != null ? MapstructUtils.convert(d, KnowledgeBO.class) : null;
    }

    public KnowledgeBO findByCode(String code) {
        KnowledgeDO d = knowledgeMapper.selectOneByQuery(
                QueryWrapper.create().eq(KnowledgeDO::getCode, code));
        return d != null ? MapstructUtils.convert(d, KnowledgeBO.class) : null;
    }

    public List<KnowledgeBO> findAll() {
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeDO::getStatus, 1)), KnowledgeBO.class);
    }

    public List<KnowledgeBO> searchByName(String keyword, int topK) {
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeDO::getStatus, 1)
                        .like(KnowledgeDO::getName, keyword)
                        .orderBy(KnowledgeDO::getId, true).limit(0, topK)), KnowledgeBO.class);
    }

    public List<KnowledgeBO> page(int offset, int limit) {
        return page(offset, limit, null, null);
    }

    public List<KnowledgeBO> page(int offset, int limit, String category, String keyword) {
        QueryWrapper qw = QueryWrapper.create().eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) qw.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) qw.like(KnowledgeDO::getName, keyword);
        qw.orderBy(KnowledgeDO::getId, true).limit(offset, limit);
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(qw), KnowledgeBO.class);
    }

    public long count() { return count(null, null); }

    public long count(String category, String keyword) {
        QueryWrapper qw = QueryWrapper.create().eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) qw.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) qw.like(KnowledgeDO::getName, keyword);
        return knowledgeMapper.selectCountByQuery(qw);
    }

    public int insert(KnowledgeBO bo) {
        return knowledgeMapper.insert(MapstructUtils.convert(bo, KnowledgeDO.class));
    }

    public int update(KnowledgeBO bo) {
        return knowledgeMapper.update(MapstructUtils.convert(bo, KnowledgeDO.class));
    }

    public int deleteById(Long id) {
        return knowledgeMapper.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return findByCode(code) != null;
    }
}
