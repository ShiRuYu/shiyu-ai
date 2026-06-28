package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.repository.RoleRepository;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.repository.UserWorkspaceRoleRepository;
import com.shiyu.ai.model.bo.RoleBO;
import com.shiyu.ai.model.vo.RolePageResponse;
import com.shiyu.ai.model.vo.RoleVO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 瑙掕壊鏈嶅姟瀹炵幇绫?
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserWorkspaceRoleRepository userWorkspaceRoleRepository;

    public RoleServiceImpl(RoleRepository roleRepository, UserWorkspaceRoleRepository userWorkspaceRoleRepository) {
        this.roleRepository = roleRepository;
        this.userWorkspaceRoleRepository = userWorkspaceRoleRepository;
    }

    @Override
    public RolePageResponse getRoleList(Number pageNo, Number pageSize, String name) {
        log.info("鑾峰彇瑙掕壊鍒楄〃锛宲ageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        Pair<Long, List<RoleBO>> result = roleRepository.selectPage(pageNo, pageSize, name);
        List<RoleBO> roleBOs = result.getRight();
        
        // 涓烘瘡涓鑹插～鍏呮潈闄愯彍鍗旾D鍒楄〃
        for (RoleBO roleBO : roleBOs) {
            List<Long> menuIds = roleRepository.selectMenuIdsByRoleId(roleBO.getId());
            roleBO.setPermissions(menuIds);
        }
        
        List<RoleVO> roleVOs = MapstructUtils.convert(roleBOs, RoleVO.class);
        
        RolePageResponse response = new RolePageResponse();
        response.setItems(roleVOs);
        response.setTotal(result.getLeft());
        
        return response;
    }

    @Override
    public List<RoleBO> getAllRoles(String status) {
        log.info("鑾峰彇鎵€鏈夎鑹诧紝status: {}", status);
        return roleRepository.selectAll(status);
    }

    @Override
    public boolean updateRole(Long id, RoleBO roleBO) {
        log.info("淇敼瑙掕壊锛宨d: {}", id);
        
        RoleBO existingRole = roleRepository.selectById(id);
        if (existingRole == null) {
            return false;
        }
        
        // 淇濆瓨瑙掕壊鍩烘湰淇℃伅
        roleBO.setId(id);
        boolean success = roleRepository.update(roleBO);
        
        // 濡傛灉鎻愪緵浜唒ermissions锛屽垯鏇存柊瑙掕壊-鑿滃崟鍏宠仈
        if (success && roleBO.getPermissions() != null) {
            // 鍏堝垹闄ゆ棫鐨勫叧鑱?
            roleRepository.deleteRoleMenus(id);
            // 鍐嶆彃鍏ユ柊鐨勫叧鑱?
            roleRepository.insertRoleMenus(id, roleBO.getPermissions());
        }
        
        return success;
    }

    @Override
    public boolean deleteRole(Long id) {
        log.info("鍒犻櫎瑙掕壊锛宨d: {}", id);
        
        // 鍏堝垹闄よ鑹?鑿滃崟鍏宠仈
        roleRepository.deleteRoleMenus(id);
        
        // 鍐嶅垹闄よ鑹?
        return roleRepository.deleteById(id);
    }

    @Override
    public boolean removeUserRoles(Long roleId, List<Long> userIds) {
        Long workspaceId = LoginContextHolder.getCurrentWorkspaceId();
        log.info("鍙栨秷鍒嗛厤瑙掕壊锛宺oleId: {}, userIds: {}, workspaceId: {}", roleId, userIds, workspaceId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        Long tenantId = LoginContextHolder.getTenantId();
        for (Long userId : userIds) {
            com.mybatisflex.core.query.QueryWrapper qw = new com.mybatisflex.core.query.QueryWrapper();
            qw.eq(UserWorkspaceRoleDO::getUserId, userId)
               .eq(UserWorkspaceRoleDO::getRoleId, roleId)
               .eq(UserWorkspaceRoleDO::getWorkspaceId, workspaceId);
            if (tenantId != null) {
                qw.eq(UserWorkspaceRoleDO::getTenantId, tenantId);
            }
            userWorkspaceRoleRepository.deleteByQuery(qw);
        }
        return true;
    }

    @Override
    public boolean assignUserRoles(Long roleId, List<Long> userIds) {
        Long workspaceId = LoginContextHolder.getCurrentWorkspaceId();
        log.info("鍒嗛厤瑙掕壊锛宺oleId: {}, userIds: {}, workspaceId: {}", roleId, userIds, workspaceId);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        Long tenantId = LoginContextHolder.getTenantId();
        for (Long userId : userIds) {
            UserWorkspaceRoleDO uwr = new UserWorkspaceRoleDO();
            uwr.setUserId(userId);
            uwr.setWorkspaceId(workspaceId);
            uwr.setRoleId(roleId);
            uwr.setTenantId(tenantId);
            userWorkspaceRoleRepository.insert(uwr);
        }
        return true;
    }

    @Override
    public boolean createRole(RoleBO roleBO) {
        log.info("鏂板瑙掕壊");
        
        // 淇濆瓨瑙掕壊鍩烘湰淇℃伅
        RoleBO savedRole = roleRepository.insert(roleBO);
        
        // 濡傛灉鎻愪緵浜唒ermissions锛屽垯淇濆瓨瑙掕壊-鑿滃崟鍏宠仈
        if (roleBO.getPermissions() != null && !roleBO.getPermissions().isEmpty()) {
            roleRepository.insertRoleMenus(savedRole.getId(), roleBO.getPermissions());
        }
        
        return true;
    }
}
