package com.shiyu.ai.auth.service;

import com.shiyu.ai.model.bo.UserBO;
import com.shiyu.ai.model.vo.UserPageResponse;

/**
 * 鐢ㄦ埛鏈嶅姟鎺ュ彛
 */
public interface UserService {

    /**
     * 鑾峰彇鐢ㄦ埛璇︽儏
     *
     * @param userId 鐢ㄦ埛 ID
     * @return 鐢ㄦ埛淇℃伅
     */
    UserBO getUserDetail(Long userId);

    /**
     * 鑾峰彇鐢ㄦ埛鍒楄〃 - 鍒嗛〉
     *
     * @param username 鐢ㄦ埛鍚嶏紙鍙€夛級
     * @param pageNo   椤电爜
     * @param pageSize 姣忛〉澶у皬
     * @return 鍒嗛〉鏁版嵁
     */
    UserPageResponse getUserList(String username, Number pageNo, Number pageSize);

    /**
     * 鍒犻櫎鐢ㄦ埛
     *
     * @param userId 鐢ㄦ埛 ID
     * @return 鏄惁鎴愬姛
     */
    boolean deleteUser(Long userId);

    /**
     * 淇敼鐢ㄦ埛
     *
     * @param userId  鐢ㄦ埛 ID
     * @param userBO  鐢ㄦ埛淇℃伅
     * @return 鏄惁鎴愬姛
     */
    boolean updateUser(Long userId, UserBO userBO);

    /**
     * 閲嶇疆鐢ㄦ埛瀵嗙爜
     *
     * @param userId  鐢ㄦ埛 ID
     * @param password 鏂板瘑鐮?
     * @return 鏄惁鎴愬姛
     */
    boolean resetUserPassword(Long userId, String password);

    /**
     * 鏂板鐢ㄦ埛
     *
     * @param userBO 鐢ㄦ埛淇℃伅
     * @return 鐢ㄦ埛 ID
     */
    Long createUser(UserBO userBO);

    /**
     * 淇敼瀵嗙爜锛堟牎楠屾棫瀵嗙爜锛?
     *
     * @param userId      鐢ㄦ埛 ID
     * @param oldPassword 鏃у瘑鐮?
     * @param newPassword 鏂板瘑鐮?
     * @return 鏄惁鎴愬姛
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
