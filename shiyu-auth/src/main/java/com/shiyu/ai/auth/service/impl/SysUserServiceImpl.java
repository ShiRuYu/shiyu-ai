package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.domain.vo.SysUserVO;
import com.shiyu.ai.auth.repository.SysUserRepository;
import com.shiyu.ai.auth.service.SysUserService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现层
 *
 * @author shiyu-ai
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Resource
    private SysUserRepository sysUserRepository;

    @Override
    public Pair<Long, List<SysUserVO>> getAll(Number pageNumber, Number pageSize) {
        Pair<Long, List<SysUserBO>> result = sysUserRepository.getAll(pageNumber, pageSize);
        List<SysUserVO> voList = MapstructUtils.convert(result.getRight(), SysUserVO.class);
        return Pair.of(result.getLeft(), voList);
    }

    @Override
    public SysUserVO getById(Long userId) {
        SysUserBO sysUserBO = sysUserRepository.getById(userId);
        return convertToVO(sysUserBO);
    }

    @Override
    public SysUserVO create(SysUserBO sysUserBO) {
        SysUserBO created = sysUserRepository.create(sysUserBO);
        return convertToVO(created);
    }

    @Override
    public SysUserVO update(SysUserBO sysUserBO) {
        SysUserBO updated = sysUserRepository.update(sysUserBO);
        return convertToVO(updated);
    }

    /**
     * 将 BO 对象转换为 VO 对象
     *
     * @param sysUserBO 业务对象
     * @return 视图对象
     */
    private SysUserVO convertToVO(SysUserBO sysUserBO) {
        return MapstructUtils.convert(sysUserBO, SysUserVO.class);
    }

    @Override
    public void deleteById(Long userId) {
        sysUserRepository.deleteById(userId);
    }
}
