package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.QuestionDO;
import com.shiyu.ai.education.implementation.persistence.mapper.QuestionMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class QuestionRepositoryImpl implements com.shiyu.ai.education.port.repository.QuestionRepository {

    @Resource
    private QuestionMapper questionMapper;

    public QuestionBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(questionMapper.selectOneByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), QuestionBO.class);
    }

    public PageData<QuestionBO> selectPage(TenantId tenantId, int pageNum, int pageSize) {
        com.mybatisflex.core.paginate.Page<QuestionDO> page = questionMapper.paginate(
                pageNum, pageSize,
                QueryWrapper.create().eq("tenant_id", tenantId.value()).orderBy("id", false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), QuestionBO.class), page.getTotalRow());
    }

    public List<QuestionBO> selectBySubjectAndGrade(TenantId tenantId, String subjectCode, Integer grade) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("subject_code", subjectCode).eq("grade", grade)), QuestionBO.class);
    }

    public List<QuestionBO> selectByDifficulty(TenantId tenantId, Integer difficulty) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("difficulty", difficulty)), QuestionBO.class);
    }

    public List<QuestionBO> selectByType(TenantId tenantId, String type) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("type", type)), QuestionBO.class);
    }

    public QuestionBO selectByCode(TenantId tenantId, String code) {
        return MapstructUtils.convert(questionMapper.selectOneByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("code", code)), QuestionBO.class);
    }

    public void incrementUsedCount(TenantId tenantId, Long id) {
        QuestionDO dataObj = questionMapper.selectOneByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id));
        if (dataObj == null) {
            throw new IllegalStateException("increment question usage target not found");
        }
        Long usedCount = dataObj.getUsedCount();
        dataObj.setUsedCount(usedCount != null ? usedCount + 1 : 1L);
        EducationWriteGuard.require(questionMapper.updateByQuery(dataObj, QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), "increment question usage");
    }

    public List<QuestionBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(questionMapper.selectListByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value())), QuestionBO.class);
    }

    public int insert(TenantId tenantId, QuestionBO entity) {
        QuestionDO dataObj = MapstructUtils.convert(entity, QuestionDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(questionMapper.insert(dataObj), "insert question");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, QuestionBO entity) {
        QuestionDO dataObj = MapstructUtils.convert(entity, QuestionDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(questionMapper.updateByQuery(dataObj, QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", entity.getId())), "update question");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(questionMapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), "delete question");
    }
}

