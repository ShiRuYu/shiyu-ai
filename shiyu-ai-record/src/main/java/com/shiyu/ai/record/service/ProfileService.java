package com.shiyu.ai.record.service;

import com.shiyu.ai.model.bo.ProfileBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 浜虹墿鏈嶅姟鎺ュ彛
 */
public interface ProfileService {

    /**
     * 鍒嗛〉鏌ヨ浜虹墿鍒楄〃
     */
    Pair<Long, List<ProfileBO>> getPage(Number pageNo, Number pageSize, String createBy);

    /**
     * 鏍规嵁ID鏌ヨ浜虹墿
     */
    ProfileBO getById(Long id);

    /**
     * 鍒涘缓浜虹墿
     */
    ProfileBO create(ProfileBO profileBO);

    /**
     * 鏇存柊浜虹墿
     */
    boolean update(ProfileBO profileBO);

    /**
     * 鍒犻櫎浜虹墿
     */
    boolean delete(Long id);
}
