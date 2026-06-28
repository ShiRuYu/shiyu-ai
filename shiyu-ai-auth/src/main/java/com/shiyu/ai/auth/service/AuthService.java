package com.shiyu.ai.auth.service;

import com.shiyu.ai.model.vo.LoginResponseVO;
import com.shiyu.ai.model.vo.WorkspaceContextVO;

import java.util.List;
import java.util.Map;

/**
 * 璁よ瘉鏈嶅姟
 * 鎻愪緵鐢ㄦ埛鐧诲綍銆佺櫥鍑虹瓑璁よ瘉鍔熻兘
 */
public interface AuthService {
    
    /**
     * 鐢ㄦ埛鐧诲綍
     * @param username 鐢ㄦ埛鍚?
     * @param password 瀵嗙爜
     * @return 鐧诲綍鍝嶅簲锛堝寘鍚敤鎴蜂俊鎭拰璁块棶浠ょ墝锛?
     */
    LoginResponseVO login(String username, String password);

    /**
     * 鐢ㄦ埛鐧诲綍锛堝甫瑙掕壊閫夋嫨锛?
     * @param username 鐢ㄦ埛鍚?
     * @param password 瀵嗙爜
     * @param roleId 褰撳墠瑙掕壊ID锛堜笉浼犲垯榛樿浣跨敤绗竴涓鑹诧級
     * @return 鐧诲綍鍝嶅簲锛堝寘鍚敤鎴蜂俊鎭拰璁块棶浠ょ墝锛?
     */
    LoginResponseVO login(String username, String password, Long roleId);
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鐮侊紙閫氳繃鐢ㄦ埛鍚嶏級
     * @param username 鐢ㄦ埛鍚?
     * @return 鏉冮檺鐮佸垪琛?
     */
    List<String> getAuthCodes(String username);
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鐮侊紙閫氳繃鐢ㄦ埛 ID锛?
     * @param userId 鐢ㄦ埛 ID
     * @return 鏉冮檺鐮佸垪琛?
     */
    List<String> getAuthCodesByUserId(Long userId);
    
    /**
     * 鍒锋柊璁块棶浠ょ墝
     * @param refreshToken 鍒锋柊浠ょ墝
     * @return 鏂扮殑璁块棶浠ょ墝
     */
    String refreshToken(String refreshToken);
    
    /**
     * 鐢ㄦ埛鐧诲嚭
     * @param refreshToken 鍒锋柊浠ょ墝
     */
    void logout(String refreshToken);

    /**
     * 鍒囨崲褰撳墠瑙掕壊
     * @param userId 鐢ㄦ埛 ID
     * @param roleId 鐩爣瑙掕壊 ID
     * @return 鏄惁鎴愬姛
     */
    boolean switchCurrentRole(Long userId, Long roleId);

    /**
     * 鍒囨崲褰撳墠绉熸埛
     */
    boolean switchCurrentTenant(Long userId, Long tenantId);

    /**
     * 鍒囨崲褰撳墠宸ヤ綔绌洪棿
     */
    boolean switchCurrentWorkspace(Long userId, Long workspaceId);

    /**
     * 鑾峰彇鐢ㄦ埛褰撳墠绉熸埛涓嬬殑宸ヤ綔绌洪棿鍒楄〃
     */
    List<WorkspaceContextVO> getUserWorkspaces(Long userId);

    /**
     * 鑾峰彇鐢ㄦ埛鎵€鏈夌鎴峰垪琛?
     */
    List<Map<String, Object>> getUserTenants(Long userId);
}
