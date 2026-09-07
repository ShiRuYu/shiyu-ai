package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.TextbookBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.TextbookDO;
import com.shiyu.ai.education.implementation.persistence.mapper.TextbookMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class TextbookRepositoryImpl implements com.shiyu.ai.education.port.repository.TextbookRepository {

    @Resource
    private TextbookMapper textbookMapper;

    public TextbookBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(textbookMapper.selectOneByQuery(QueryWrapper.create()
                .eq(TextbookDO::getTenantId, tenantId.value()).eq(TextbookDO::getId, id)), TextbookBO.class);
    }

    public PageData<TextbookBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<TextbookDO> page = textbookMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq(TextbookDO::getTenantId, tenantId.value())
                        .orderBy("id", false)
        );
        return new PageData<>(MapstructUtils.convert(page.getRecords(), TextbookBO.class), page.getTotalRow());
    }

    public List<TextbookBO> selectBySubjectAndGrade(TenantId tenantId, String subjectCode, Integer grade) {
        return MapstructUtils.convert(textbookMapper.selectListByQuery(
                QueryWrapper.create().eq(TextbookDO::getTenantId, tenantId.value())
                        .eq("subject_code", subjectCode)
                        .eq("grade", grade)
        ), TextbookBO.class);
    }
    public List<TextbookBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(textbookMapper.selectListByQuery(QueryWrapper.create()
                .eq(TextbookDO::getTenantId, tenantId.value())), TextbookBO.class);
    }

    public int insert(TenantId tenantId, TextbookBO entity) {
        TextbookDO dataObj = MapstructUtils.convert(entity, TextbookDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(textbookMapper.insert(dataObj), "insert textbook");
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(TenantId tenantId, TextbookBO entity) {
        TextbookDO dataObj = MapstructUtils.convert(entity, TextbookDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(textbookMapper.updateByQuery(dataObj, QueryWrapper.create()
                .eq(TextbookDO::getTenantId, tenantId.value()).eq(TextbookDO::getId, entity.getId())), "update textbook");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(textbookMapper.deleteByQuery(QueryWrapper.create()
                .eq(TextbookDO::getTenantId, tenantId.value()).eq(TextbookDO::getId, id)), "delete textbook");
    }

}

