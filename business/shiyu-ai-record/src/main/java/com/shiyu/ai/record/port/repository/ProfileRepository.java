package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.record.domain.enums.GenderEnum;
import com.shiyu.ai.record.domain.model.ProfileBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface ProfileRepository {
    Pair<Long, List<ProfileBO>> selectPage(Number pageNo, Number pageSize, String createBy);
    ProfileBO selectById(Long id);
    ProfileBO insert(ProfileBO profileBO);
    boolean update(ProfileBO profileBO);
    boolean deleteById(Long id);
}
