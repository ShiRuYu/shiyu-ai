package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.SysUserDO;
import com.shiyu.ai.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Demo
 */
@RestController
@RequiredArgsConstructor
public class DemoController {
    private final SysUserMapper sysUserMapper;

    /**
     * 插入一条数据
     * @return
     */
    @GetMapping("put")
    public Result<Integer> insert(){
        SysUserDO sysUser = new SysUserDO();
        sysUser.setUserName("admin");
        sysUser.setNickName("admin1");
        sysUser.setPassword("123456");
        int insert = sysUserMapper.insert(sysUser);
        return Result.success(insert);
    }

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping("get")
    public Result<List<SysUserDO>> get(){
        List<SysUserDO> sysUsers = sysUserMapper.selectAll();
        return Result.success(sysUsers);
    }
}
