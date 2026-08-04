package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRepository implements com.shiyu.ai.knowledge.port.repository.KnowledgeRepository {

    @Resource
    private KnowledgeMapper knowledgeMapper;

    public KnowledgeBO findById(Long id) {
        KnowledgeDO d = knowledgeMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDO::getId, id)
                .eq(KnowledgeDO::getDelFlag, 0));
        return d != null ? MapstructUtils.convert(d, KnowledgeBO.class) : null;
    }

    public KnowledgeBO findByCode(String code) {
        KnowledgeDO d = knowledgeMapper.selectOneByQuery(
                QueryWrapper.create().eq(KnowledgeDO::getCode, code)
                        .eq(KnowledgeDO::getDelFlag, 0));
        return d != null ? MapstructUtils.convert(d, KnowledgeBO.class) : null;
    }

    public List<KnowledgeBO> findAll() {
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeDO::getStatus, 1)
                        .eq(KnowledgeDO::getDelFlag, 0)), KnowledgeBO.class);
    }

    public List<KnowledgeBO> searchByName(String keyword, int topK) {
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeDO::getStatus, 1)
                        .eq(KnowledgeDO::getDelFlag, 0)
                        .like(KnowledgeDO::getName, keyword)
                        .orderBy(KnowledgeDO::getId, true).limit(0, topK)), KnowledgeBO.class);
    }

    public List<KnowledgeBO> page(int offset, int limit) {
        return page(offset, limit, null, null);
    }

    public List<KnowledgeBO> page(int offset, int limit, String category, String keyword) {
        QueryWrapper qw = QueryWrapper.create().eq(KnowledgeDO::getStatus, 1)
                .eq(KnowledgeDO::getDelFlag, 0);
        if (category != null && !category.isBlank()) qw.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) qw.like(KnowledgeDO::getName, keyword);
        qw.orderBy(KnowledgeDO::getId, true).limit(offset, limit);
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(qw), KnowledgeBO.class);
    }

    public long count() { return count(null, null); }

    public long count(String category, String keyword) {
        QueryWrapper qw = QueryWrapper.create().eq(KnowledgeDO::getStatus, 1)
                .eq(KnowledgeDO::getDelFlag, 0);
        if (category != null && !category.isBlank()) qw.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) qw.like(KnowledgeDO::getName, keyword);
        return knowledgeMapper.selectCountByQuery(qw);
    }

    public int insert(KnowledgeBO bo) {
        KnowledgeDO dataObject = MapstructUtils.convert(bo, KnowledgeDO.class);
        int rows = knowledgeMapper.insert(dataObject);
        bo.setId(dataObject.getId());
        return rows;
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

    public boolean existsBySpaceAndCode(Long spaceId, String code) {
        return knowledgeMapper.selectCountByQuery(QueryWrapper.create()
                .eq(KnowledgeDO::getSpaceId, spaceId)
                .eq(KnowledgeDO::getCode, code)
                .eq(KnowledgeDO::getDelFlag, 0)) > 0;
    }

    public List<KnowledgeBO> findBySpace(Long spaceId) {
        return MapstructUtils.convert(knowledgeMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeDO::getSpaceId, spaceId)
                        .eq(KnowledgeDO::getStatus, 1)
                        .eq(KnowledgeDO::getDelFlag, 0)), KnowledgeBO.class);
    }

    public PageData<KnowledgeBO> pageBySpace(Long spaceId, int pageNum, int pageSize,
                                              String keyword, String category) {
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeDO::getSpaceId, spaceId)
                .eq(KnowledgeDO::getStatus, 1)
                .eq(KnowledgeDO::getDelFlag, 0);
        if (keyword != null && !keyword.isBlank()) {
            query.like(KnowledgeDO::getName, keyword);
        }
        if (category != null && !category.isBlank()) {
            query.eq(KnowledgeDO::getCategory, category);
        }
        var page = knowledgeMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeBO.class),
                page.getTotalRow());
    }

    public int deleteByIdAndSpace(Long id, Long spaceId) {
        return knowledgeMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeDO::getId, id)
                .eq(KnowledgeDO::getSpaceId, spaceId)
                .eq(KnowledgeDO::getDelFlag, 0));
    }

    public void assignDefaultSpace(Long spaceId) {
        List<KnowledgeDO> records = knowledgeMapper.selectListByQuery(
                QueryWrapper.create().isNull(KnowledgeDO::getSpaceId));
        for (KnowledgeDO record : records) {
            record.setSpaceId(spaceId);
            knowledgeMapper.update(record);
        }
    }
}
