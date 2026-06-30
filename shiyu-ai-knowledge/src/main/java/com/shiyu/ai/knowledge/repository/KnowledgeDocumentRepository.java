package com.shiyu.ai.knowledge.repository;

import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocumentDO;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeDocumentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentRepository {

    @Resource
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    public KnowledgeDocumentDO selectById(Long id) {
        return knowledgeDocumentMapper.selectOneById(id);
    }

    public List<KnowledgeDocumentDO> selectAll() {
        return knowledgeDocumentMapper.selectAll();
    }

    public int insert(KnowledgeDocumentDO doc) {
        return knowledgeDocumentMapper.insert(doc);
    }

    public int update(KnowledgeDocumentDO doc) {
        return knowledgeDocumentMapper.update(doc);
    }

    public int deleteById(Long id) {
        return knowledgeDocumentMapper.deleteById(id);
    }
}
