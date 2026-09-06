package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.KnowledgeTextbookDO;
import com.shiyu.ai.education.implementation.persistence.mapper.KnowledgeTextbookMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeTextbookRepositoryImpl implements com.shiyu.ai.education.port.repository.KnowledgeTextbookRepository {

    @Resource
    private KnowledgeTextbookMapper mapper;

    public void insert(TenantId tenantId, KnowledgeTextbookBO kt) {
        KnowledgeTextbookDO dataObj = MapstructUtils.convert(kt, KnowledgeTextbookDO.class);
        dataObj.setTenantId(tenantId.value());
        EducationWriteGuard.require(mapper.insert(dataObj), "insert knowledge textbook");
    }

    public void deleteById(TenantId tenantId, Long id) {
        EducationWriteGuard.require(mapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeTextbookDO::getTenantId, tenantId.value()).eq(KnowledgeTextbookDO::getId, id)),
                "delete knowledge textbook");
    }

    public void deleteByChapterId(TenantId tenantId, Long cid) {
        mapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeTextbookDO::getTenantId, tenantId.value()).eq("chapter_id", cid));
    }

    public void deleteByKnowledgeIdAndChapterId(TenantId tenantId, Long kid, Long cid) {
        mapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeTextbookDO::getTenantId, tenantId.value()).eq("knowledge_id", kid).eq("chapter_id", cid));
    }

    public List<KnowledgeTextbookBO> selectByChapterId(TenantId tenantId, Long cid) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeTextbookDO::getTenantId, tenantId.value()).eq("chapter_id", cid)), KnowledgeTextbookBO.class);
    }

    public List<KnowledgeTextbookBO> selectByKnowledgeId(TenantId tenantId, Long kid) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeTextbookDO::getTenantId, tenantId.value()).eq("knowledge_id", kid)), KnowledgeTextbookBO.class);
    }

    public List<KnowledgeTextbookBO> selectByTextbookId(TenantId tenantId, Long tid) {
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeTextbookDO::getTenantId, tenantId.value()).eq("textbook_id", tid)), KnowledgeTextbookBO.class);
    }
}

