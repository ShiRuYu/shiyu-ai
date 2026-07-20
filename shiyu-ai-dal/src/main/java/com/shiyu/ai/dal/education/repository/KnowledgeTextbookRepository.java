package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.KnowledgeTextbookBO;
import com.shiyu.ai.dal.education.dataobject.KnowledgeTextbookDO;
import com.shiyu.ai.dal.education.mapper.KnowledgeTextbookMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeTextbookRepository {

    @Resource
    private KnowledgeTextbookMapper mapper;

    public void insert(KnowledgeTextbookBO kt) {
        KnowledgeTextbookDO dataObj = MapstructUtils.convert(kt, KnowledgeTextbookDO.class);
        mapper.insert(dataObj);
    }

    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    public void deleteByChapterId(Long cid) {
        mapper.deleteByQuery(QueryWrapper.create().eq("chapter_id", cid));
    }

    public void deleteByKnowledgeIdAndChapterId(Long kid, Long cid) {
        mapper.deleteByQuery(QueryWrapper.create().eq("knowledge_id", kid).eq("chapter_id", cid));
    }

    public List<KnowledgeTextbookBO> selectByChapterId(Long cid) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("chapter_id", cid)), KnowledgeTextbookBO.class);
    }

    public List<KnowledgeTextbookBO> selectByKnowledgeId(Long kid) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("knowledge_id", kid)), KnowledgeTextbookBO.class);
    }

    public List<KnowledgeTextbookBO> selectByTextbookId(Long tid) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq("textbook_id", tid)), KnowledgeTextbookBO.class);
    }
}
