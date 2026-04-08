 package com.shiyu.ai.agent.dal.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.UserRoleDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户角色关联表 数据层
 */
@Mapper
public interface UserRoleMapper extends BaseMapperFlex<UserRoleDO> {

    /**
     * 根据用户 ID 查询角色列表
     * @param userId 用户 ID
     * @return 角色列表
     */
    default List<RoleDO> selectRolesByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(
                        "r.id",
                        "r.code",
                        "r.name",
                        "r.enable",
                        "r.del_flag",
                        "r.create_time",
                        "r.update_time"
                )
                .from("role").as("r")
                .innerJoin("user_role").as("ur").on("r.id = ur.role_id")
                .where("ur.user_id = ?", userId)
                .and("r.enable = 1")
                .and("r.del_flag = 0");
        
        return selectListByQueryAs(queryWrapper, RoleDO.class);
    }

    /**
     * 为用户分配角色
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @return 影响行数
     */
    default int insertUserRole(Long userId, Long roleId) {
        UserRoleDO userRole = new UserRoleDO();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return insert(userRole);
    }

    /**
     * 取消用户的角色
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @return 影响行数
     */
    default int deleteUserRole(Long userId, Long roleId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(UserRoleDO::getUserId).eq(userId)
                .and(UserRoleDO::getRoleId).eq(roleId);
        return deleteByQuery(queryWrapper);
    }

    /**
     * 删除用户的所有角色
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int deleteRolesByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(UserRoleDO::getUserId).eq(userId);
        return deleteByQuery(queryWrapper);
    }
}
