package com.shiyu.ai.auth.service;

import com.shiyu.ai.model.bo.DictBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 瀛楀吀鏈嶅姟灞?
 */
public interface DictService {

    /**
     * 鍒嗛〉鏌ヨ瀛楀吀鍒楄〃
     *
     * @param pageNo 椤电爜
     * @param pageSize   姣忛〉鏁伴噺
     * @return 瀛楀吀鍒楄〃
     */
    Pair<Long, List<DictBO>> getAll(Number pageNo, Number pageSize);

    /**
     * 鏍规嵁ID鏌ヨ瀛楀吀
     *
     * @param id 瀛楀吀ID
     * @return 瀛楀吀淇℃伅
     */
    DictBO getById(Long id);

    /**
     * 鏍规嵁瀛楀吀绫诲瀷鏌ヨ瀛楀吀鍒楄〃
     *
     * @param dictType 瀛楀吀绫诲瀷
     * @return 瀛楀吀鍒楄〃
     */
    List<DictBO> getByDictType(String dictType);

    /**
     * 鍒涘缓瀛楀吀
     *
     * @param dictBO 瀛楀吀淇℃伅
     * @return 鍒涘缓鍚庣殑瀛楀吀淇℃伅
     */
    DictBO create(DictBO dictBO);

    /**
     * 鏇存柊瀛楀吀
     *
     * @param dictBO 瀛楀吀淇℃伅
     * @return 鏇存柊鍚庣殑瀛楀吀淇℃伅
     */
    DictBO update(DictBO dictBO);

    /**
     * 鍒犻櫎瀛楀吀
     *
     * @param id 瀛楀吀ID
     */
    void deleteById(Long id);

    /**
     * 鎵归噺鍒犻櫎瀛楀吀
     *
     * @param ids 瀛楀吀ID鍒楄〃
     */
    void deleteByIds(List<Long> ids);
}
