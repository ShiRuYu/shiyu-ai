package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.bo.MenuBO;

import java.util.List;

/**
 * 鑿滃崟鏈嶅姟鎺ュ彛
 */
public interface MenuService {

    /**
     * 鑾峰彇瑙掕壊鏉冮檺鏍?by token
     *
     * @return 鏉冮檺鏍?
     */
    List<MenuBO> getMenuPermissionsTree();

    /**
     * 鑾峰彇鏉冮檺鏍?- 鑿滃崟
     *
     * @return 鏉冮檺鏍?
     */
    List<MenuBO> getMenuTree();

    /**
     * 鑾峰彇鏉冮檺鏍?all
     *
     * @return 鏉冮檺鏍?
     */
    List<MenuBO> getAllTree();

    /**
     * 鍒犻櫎鑿滃崟
     *
     * @param id 鑿滃崟 ID
     * @return 鏄惁鎴愬姛
     */
    boolean deleteMenu(Long id);

    /**
     * 鏂板鑿滃崟
     *
     * @param menuBO 鑿滃崟淇℃伅
     * @return 鏄惁鎴愬姛
     */
    boolean createMenu(MenuBO menuBO);

    /**
     * 淇敼鑿滃崟
     *
     * @param id      鑿滃崟 ID
     * @param menuBO  鑿滃崟淇℃伅
     * @return 鏄惁鎴愬姛
     */
    boolean updateMenu(Long id, MenuBO menuBO);

    /**
     * 鑾峰彇鎸夐挳鏉冮檺-by parentId
     *
     * @param parentId 鐖惰彍鍗?ID
     * @return 鎸夐挳鏉冮檺鍒楄〃
     */
    List<MenuBO> getButtonsByParentId(Long parentId);
    
    /**
     * 鏍规嵁鐢ㄦ埛 ID 鑾峰彇鑿滃崟鏍?
     *
     * @param userId 鐢ㄦ埛 ID
     * @return 鑿滃崟鏍?
     */
    List<MenuBO> getMenuTreeByUserId(Long userId);
    
    /**
     * 鏍规嵁鐢ㄦ埛 ID 鍜岀被鍨嬭幏鍙栬彍鍗曞垪琛?
     *
     * @param userId 鐢ㄦ埛 ID
     * @param type 鑿滃崟绫诲瀷锛圡ENU-鑿滃崟锛孋ATALOG-鐩綍锛孊UTTON-鎸夐挳锛?
     * @return 鑿滃崟鍒楄〃
     */
    List<MenuBO> getMenusByUserIdAndType(Long userId, String type);

    /**
     * 妫€鏌ヨ彍鍗曞悕绉版槸鍚﹀凡瀛樺湪
     *
     * @param name 鑿滃崟鍚嶇О
     * @param id   鑿滃崟 ID锛堢紪杈戞椂鎺掗櫎鑷韩锛?
     * @return true 琛ㄧず宸插瓨鍦?
     */
    boolean isMenuNameExists(String name, Long id);

    /**
     * 妫€鏌ヨ彍鍗曡矾寰勬槸鍚﹀凡瀛樺湪
     *
     * @param path 鑿滃崟璺緞
     * @param id   鑿滃崟 ID锛堢紪杈戞椂鎺掗櫎鑷韩锛?
     * @return true 琛ㄧず宸插瓨鍦?
     */
    boolean isMenuPathExists(String path, Long id);

    /**
     * 鑾峰彇鏍硅妭鐐硅彍鍗曪紙parentId 涓?null锛岀敤浜庢噿鍔犺浇鍒濆鍔犺浇锛?
     *
     * @return 鏍硅彍鍗曞垪琛紙骞抽摵锛?
     */
    List<MenuBO> getMenuRoots();

    /**
     * 鑾峰彇鎸囧畾鐖惰彍鍗曠殑瀛愯彍鍗曪紙鐢ㄤ簬鎳掑姞杞藉睍寮€锛?
     *
     * @param parentId 鐖惰彍鍗?ID
     * @return 瀛愯彍鍗曞垪琛紙骞抽摵锛?
     */
    List<MenuBO> getChildrenByParentId(Long parentId);

    /**
     * 鑾峰彇鐢ㄦ埛璺敱鑿滃崟锛圕ATALOG + MENU锛屾帓闄?BUTTON锛?
     * 鐢ㄤ簬鍓嶇鍔ㄦ€佽矾鐢辩敓鎴?
     *
     * @param userId 鐢ㄦ埛 ID
     * @return 璺敱鑿滃崟鏍?
     */
    List<MenuBO> getRouteMenusByUserId(Long userId);
}
