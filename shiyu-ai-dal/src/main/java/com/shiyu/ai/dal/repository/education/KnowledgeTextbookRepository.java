package com.shiyu.ai.dal.repository.education;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.KnowledgeTextbookDO;
import com.shiyu.ai.dal.mapper.education.KnowledgeTextbookMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class KnowledgeTextbookRepository {
    @Resource
    private KnowledgeTextbookMapper mapper;

    public void insert(KnowledgeTextbookDO kt) { mapper.insert(kt); }
    public void deleteById(Long id) { mapper.deleteById(id); }
    public void deleteByChapterId(Long cid) { mapper.deleteByQuery(QueryWrapper.create().eq("chapter_id", cid)); }
    public void deleteByKnowledgeIdAndChapterId(Long kid, Long cid) { mapper.deleteByQuery(QueryWrapper.create().eq("knowledge_id", kid).eq("chapter_id", cid)); }
    public List<KnowledgeTextbookDO> selectByChapterId(Long cid) { return mapper.selectListByQuery(QueryWrapper.create().eq("chapter_id", cid)); }
    public List<KnowledgeTextbookDO> selectByKnowledgeId(Long kid) { return mapper.selectListByQuery(QueryWrapper.create().eq("knowledge_id", kid)); }
    public List<KnowledgeTextbookDO> selectByTextbookId(Long tid) { return mapper.selectListByQuery(QueryWrapper.create().eq("textbook_id", tid)); }
}