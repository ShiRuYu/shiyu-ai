package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface MenuRepository {
    List<MenuBO> selectAll();
    List<MenuBO> selectAllByType(String type);
    List<MenuBO> selectAllExcludingType(String type);
    MenuBO selectById(Long id);
    MenuBO insert(MenuBO menuBO);
    boolean update(MenuBO menuBO);
    boolean deleteById(Long id);
    Pair<Long, List<MenuBO>> selectPage(Number pageNo, Number pageSize, String name, String code, String type, Integer status);
    List<MenuBO> selectMenusByUserId(Long userId, String excludeType);
    boolean existsByName(String name, Long excludeId);
    boolean existsByPath(String path, Long excludeId);
    List<MenuBO> selectByParentId(Long parentId);
    List<MenuBO> selectByParentIdAndType(Long parentId, String type);
}
