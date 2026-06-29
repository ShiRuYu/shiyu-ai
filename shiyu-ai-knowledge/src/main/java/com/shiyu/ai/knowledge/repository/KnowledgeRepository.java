package com.shiyu.ai.knowledge.repository;

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

    public List<KnowledgeDO> findBySubjectCode(String subjectCode) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getSubjectCode, subjectCode);
        qw.eq(KnowledgeDO::getStatus, 1);
        return knowledgeMapper.selectListByQuery(qw);
    }

    public List<KnowledgeDO> findByGrade(Integer grade) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getGrade, grade);
        qw.eq(KnowledgeDO::getStatus, 1);
        return knowledgeMapper.selectListByQuery(qw);
    }

    public List<KnowledgeDO> page(int offset, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getStatus, 1);
        qw.orderBy(KnowledgeDO::getId, true);
        qw.limit(offset, limit);
        return knowledgeMapper.selectListByQuery(qw);
    }

    public long count() {
        QueryWrapper qw = QueryWrapper.create();
        qw.eq(KnowledgeDO::getStatus, 1);
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
