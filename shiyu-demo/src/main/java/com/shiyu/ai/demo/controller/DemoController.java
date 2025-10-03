package com.shiyu.ai.demo.controller;

import com.shiyu.ai.common.core.domain.LoginUser;
import com.shiyu.ai.common.core.domain.Result;
import com.shiyu.ai.common.core.service.BaseContext;
import com.shiyu.ai.demo.domain.SysUser;
import com.shiyu.ai.demo.mapper.DemoMapper;
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
    private final DemoMapper demoMapper;

    /**
     * 插入一条数据
     * @return
     */
    @GetMapping("put")
    public Result<Integer> insert(){
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1974039142799511553L);
        BaseContext.set(BaseContext.MapKey.LOGIN_USER.name(),loginUser);
        SysUser sysUser = new SysUser();
        sysUser.setUserName("admin");
        sysUser.setNickName("admin1");
        sysUser.setPassword("123456");
        int insert = demoMapper.insert(sysUser);
        return Result.success(insert);
    }

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping("get")
    public Result<List<SysUser>> get(){
        List<SysUser> sysUsers = demoMapper.selectList();
        return Result.success(sysUsers);
    }
}
