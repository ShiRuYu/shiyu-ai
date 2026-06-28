package com.shiyu.ai.auth.service;

import com.shiyu.ai.model.bo.WorkspaceBO;

import java.util.List;

/**
 * 宸ヤ綔绌洪棿鏈嶅姟鎺ュ彛
 */
public interface WorkspaceService {

    /**
     * 鑾峰彇宸ヤ綔绌洪棿鍒楄〃锛堟爲褰級
     *
     * @param name 宸ヤ綔绌洪棿鍚嶇О锛堝彲閫夛紝鐢ㄤ簬杩囨护锛?
     * @return 宸ヤ綔绌洪棿鏍戝舰鍒楄〃
     */
    List<WorkspaceBO> getWorkspaceList(String name);

    /**
     * 鏍规嵁 ID 鑾峰彇宸ヤ綔绌洪棿
     *
     * @param id 宸ヤ綔绌洪棿 ID
     * @return 宸ヤ綔绌洪棿淇℃伅
     */
    WorkspaceBO getById(Long id);

    /**
     * 鏂板宸ヤ綔绌洪棿
     *
     * @param workspaceBO 宸ヤ綔绌洪棿淇℃伅
     * @return 鏄惁鎴愬姛
     */
    boolean createWorkspace(WorkspaceBO workspaceBO);

    /**
     * 淇敼宸ヤ綔绌洪棿
     *
     * @param id          宸ヤ綔绌洪棿 ID
     * @param workspaceBO 宸ヤ綔绌洪棿淇℃伅
     * @return 鏄惁鎴愬姛
     */
    boolean updateWorkspace(Long id, WorkspaceBO workspaceBO);

    /**
     * 鍒犻櫎宸ヤ綔绌洪棿
     *
     * @param id 宸ヤ綔绌洪棿 ID
     * @return 鏄惁鎴愬姛
     */
    boolean deleteWorkspace(Long id);
}
