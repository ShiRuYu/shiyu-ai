package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDate;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ReviewTaskDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ReviewTaskMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReviewTaskRepositoryImpl implements com.shiyu.ai.education.port.repository.ReviewTaskRepository {

    @Resource
    private ReviewTaskMapper reviewTaskMapper;

    public ReviewTaskBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(reviewTaskMapper.selectOneByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), ReviewTaskBO.class);
    }

    public List<ReviewTaskBO> selectTodayTasks(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .eq("review_date", LocalDate.now())
                        .orderBy("review_round")
        ), ReviewTaskBO.class);
    }

    public List<ReviewTaskBO> selectByStudentAndStatus(TenantId tenantId, Long studentId, Integer status) {
        return MapstructUtils.convert(reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .eq("status", status)
                        .orderBy("review_date")
        ), ReviewTaskBO.class);
    }

    public List<ReviewTaskBO> selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId) {
        return MapstructUtils.convert(reviewTaskMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value()).eq("student_id", studentId)
                        .eq("knowledge_id", knowledgeId)
                        .orderBy("review_round")
        ), ReviewTaskBO.class);
    }

    public int insert(TenantId tenantId, ReviewTaskBO entity) {
        ReviewTaskDO dataObj = MapstructUtils.convert(entity, ReviewTaskDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(reviewTaskMapper.insert(dataObj), "insert review task");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, ReviewTaskBO entity) {
        ReviewTaskDO dataObj = MapstructUtils.convert(entity, ReviewTaskDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(reviewTaskMapper.updateByQuery(dataObj, QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", entity.getId())), "update review task");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(reviewTaskMapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)), "delete review task");
    }

}

