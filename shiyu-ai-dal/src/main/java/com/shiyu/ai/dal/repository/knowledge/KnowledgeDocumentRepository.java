package com.shiyu.ai.dal.repository.knowledge;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocumentDO;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeDocumentMapper;
import com.shiyu.ai.dal.mapper.knowledge.KnowledgeDocRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentRepository {

    @Resource
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Resource
    private KnowledgeDocRelationMapper knowledgeDocRelationMapper;

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

    public List<KnowledgeDocumentDO> searchByKeyword(String keyword, int topK) {
        return selectAll().stream()
                .filter(d -> d.getTitle() != null && d.getTitle().contains(keyword)
                        || d.getContent() != null && d.getContent().contains(keyword))
                .limit(topK)
                .toList();
    }

    public List<KnowledgeDocumentDO> selectByKnowledgeId(Long knowledgeId) {
        // Query via knowledge_doc_relation table
        List<Long> docIds = knowledgeDocRelationMapper.selectListByQuery(
                QueryWrapper.create().eq("knowledge_id", knowledgeId))
                .stream()
                .map(KnowledgeDocRelationDO::getDocId)
                .toList();
        if (docIds.isEmpty()) return List.of();
        return knowledgeDocumentMapper.selectListByQuery(
                QueryWrapper.create().in("id", docIds));
    }
}