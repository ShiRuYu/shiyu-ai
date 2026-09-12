package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.WrongQuestionDO;
import com.shiyu.ai.education.implementation.persistence.mapper.WrongQuestionMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code WrongQuestionRepositoryImpl} 实现教育模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class WrongQuestionRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .WrongQuestionRepository {

    /**
     * wrongQuestionMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private WrongQuestionMapper wrongQuestionMapper;

    public WrongQuestionBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(
                wrongQuestionMapper.selectOneByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                WrongQuestionBO.class);
    }

    public List<WrongQuestionBO> selectByStudentId(TenantId tenantId, Long studentId) {
        return MapstructUtils.convert(
                wrongQuestionMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("student_id", studentId)),
                WrongQuestionBO.class);
    }

    public WrongQuestionBO selectByStudentAndQuestion(
            TenantId tenantId, Long studentId, Long questionId) {
        return MapstructUtils.convert(
                wrongQuestionMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("student_id", studentId)
                                .eq("question_id", questionId)),
                WrongQuestionBO.class);
    }

    public int insert(TenantId tenantId, WrongQuestionBO entity) {
        WrongQuestionDO dataObj = MapstructUtils.convert(entity, WrongQuestionDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows =
                EducationWriteGuard.require(
                        wrongQuestionMapper.insert(dataObj), "insert wrong question");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, WrongQuestionBO entity) {
        WrongQuestionDO dataObj = MapstructUtils.convert(entity, WrongQuestionDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(
                wrongQuestionMapper.updateByQuery(
                        dataObj,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("id", entity.getId())),
                "update wrong question");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(
                wrongQuestionMapper.deleteByQuery(
                        QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("id", id)),
                "delete wrong question");
    }
}
