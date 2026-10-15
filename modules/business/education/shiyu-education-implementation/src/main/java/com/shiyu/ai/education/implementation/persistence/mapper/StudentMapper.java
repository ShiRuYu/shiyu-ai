package com.shiyu.ai.education.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.education.implementation.persistence.dataobject.StudentDO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 负责 学生 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface StudentMapper extends BaseMapperFlex<StudentDO> {
    /** 锁定当前租户的学生记录，直至所属事务结束。 */
    @Select("SELECT id FROM edu_student WHERE tenant_id = #{tenantId} AND id = #{id} FOR UPDATE")
    Long lockForStudentWrite(@Param("tenantId") long tenantId, @Param("id") long id);
}
