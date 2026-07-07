package com.shiyu.ai.dal.repository.knowledge;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRepository {

    @Resource
    private KnowledgeMapper knowledgeMapper;

    public KnowledgeDO findById(Long id) {
        return knowledgeMapper.selectOneById(id);
    }

    public KnowledgeDO findByCode(String code) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getCode, code);
        return knowledgeMapper.selectOneByQuery(qw);
    }

    public List<KnowledgeDO> findAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getStatus, 1);
        return knowledgeMapper.selectListByQuery(qw);
    }

    public List<KnowledgeDO> searchByName(String keyword, int topK) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getStatus, 1);
        qw.like(KnowledgeDO::getName, keyword);
        qw.orderBy(KnowledgeDO::getId, true);
        qw.limit(0, topK);
        return knowledgeMapper.selectListByQuery(qw);
    }

    public List<KnowledgeDO> page(int offset, int limit) {
        return page(offset, limit, null, null);
    }

    public List<KnowledgeDO> page(int offset, int limit, String category, String keyword) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) {
            qw.eq(KnowledgeDO::getCategory, category);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(KnowledgeDO::getName, keyword);
        }
        qw.orderBy(KnowledgeDO::getId, true);
        qw.limit(offset, limit);
        return knowledgeMapper.selectListByQuery(qw);
    }

    public long count() {
        return count(null, null);
    }

    public long count(String category, String keyword) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) {
            qw.eq(KnowledgeDO::getCategory, category);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like(KnowledgeDO::getName, keyword);
        }
        return knowledgeMapper.selectCountByQuery(qw);
    }

    public int insert(KnowledgeDO knowledgeDO) {
        return knowledgeMapper.insert(knowledgeDO);
    }

    public int update(KnowledgeDO knowledgeDO) {
        return knowledgeMapper.update(knowledgeDO);
    }

    public int deleteById(Long id) {
        return knowledgeMapper.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return findByCode(code) != null;
    }
}
