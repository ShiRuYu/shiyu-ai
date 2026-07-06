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

    public List<KnowledgeDocumentDO> searchByKeyword(String keyword, int topK) {
        // 先取出全部，在内存中按标题匹配（小规模数据）
        // 后续可改为 MyBatis-Flex QueryWrapper like 查询
        return selectAll().stream()
                .filter(d -> d.getTitle() != null && d.getTitle().contains(keyword)
                        || d.getContent() != null && d.getContent().contains(keyword))
                .limit(topK)
                .toList();
    }

    public List<KnowledgeDocumentDO> selectByKnowledgeId(Long knowledgeId) {
        // 通过 ingestion 表关联查询，暂返回空
        // 后续可通过 knowledge_doc_relation 表关联
        return List.of();
    }

}
