package com.shiyu.ai.auth.service;

import com.shiyu.ai.model.bo.RoleBO;
import com.shiyu.ai.model.vo.RolePageResponse;

import java.util.List;

/**
 * 瑙掕壊鏈嶅姟鎺ュ彛
 */
public interface RoleService {

    /**
     * 鑾峰彇瑙掕壊鍒楄〃 - 鍒嗛〉
     */
    RolePageResponse getRoleList(Number pageNo, Number pageSize, String name);

    /**
     * 鑾峰彇瑙掕壊鍒楄〃-all
     */
    List<RoleBO> getAllRoles(String status);

    /**
     * 淇敼瑙掕壊
     */
    boolean updateRole(Long id, RoleBO roleBO);

    /**
     * 鍒犻櫎瑙掕壊
     */
    boolean deleteRole(Long id);

    /**
     * 鍙栨秷鍒嗛厤瑙掕壊 - 鎵归噺锛堜粠褰撳墠宸ヤ綔绌洪棿绉婚櫎瑙掕壊锛?
     */
    boolean removeUserRoles(Long id, List<Long> userIds);

    /**
     * 鍒嗛厤瑙掕壊 - 鎵归噺锛堝湪褰撳墠宸ヤ綔绌洪棿鍒嗛厤瑙掕壊锛?
     */
    boolean assignUserRoles(Long id, List<Long> userIds);

    /**
     * 鏂板瑙掕壊
     */
    boolean createRole(RoleBO roleBO);
}
