package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocumentMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentRepository {

    @Resource
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Resource
    private KnowledgeDocRelationMapper knowledgeDocRelationMapper;

    public KnowledgeDocumentBO selectById(Long id) {
        KnowledgeDocumentDO d = knowledgeDocumentMapper.selectOneById(id);
        return d != null ? MapstructUtils.convert(d, KnowledgeDocumentBO.class) : null;
    }

    public List<KnowledgeDocumentBO> selectAll() {
        return MapstructUtils.convert(knowledgeDocumentMapper.selectAll(), KnowledgeDocumentBO.class);
    }

    public int insert(KnowledgeDocumentBO bo) {
        return knowledgeDocumentMapper.insert(MapstructUtils.convert(bo, KnowledgeDocumentDO.class));
    }

    public int update(KnowledgeDocumentBO bo) {
        return knowledgeDocumentMapper.update(MapstructUtils.convert(bo, KnowledgeDocumentDO.class));
    }

    public int deleteById(Long id) {
        return knowledgeDocumentMapper.deleteById(id);
    }

    public List<KnowledgeDocumentBO> searchByKeyword(String keyword, int topK) {
        return selectAll().stream()
                .filter(d -> d.getTitle() != null && d.getTitle().contains(keyword)
                        || d.getContent() != null && d.getContent().contains(keyword))
                .limit(topK)
                .toList();
    }

    public List<KnowledgeDocumentBO> selectByKnowledgeId(Long knowledgeId) {
        List<Long> docIds = knowledgeDocRelationMapper.selectListByQuery(
                QueryWrapper.create().eq("knowledge_id", knowledgeId))
                .stream().map(KnowledgeDocRelationDO::getDocId).toList();
        if (docIds.isEmpty()) return List.of();
        return MapstructUtils.convert(knowledgeDocumentMapper.selectListByQuery(
                QueryWrapper.create().in("id", docIds)), KnowledgeDocumentBO.class);
    }
}
