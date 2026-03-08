package com.shiyu.ai.auth.security.config;

import com.shiyu.ai.auth.domain.SysUserDO;
import com.shiyu.ai.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitTestData implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查是否已存在 admin 用户
        SysUserDO existUser = sysUserMapper.selectOneByQuery(
                new com.mybatisflex.core.query.QueryWrapper()
                        .eq(SysUserDO::getUserName, "admin")
        );

        if (existUser == null) {
            // 创建默认管理员账户
            SysUserDO adminUser = new SysUserDO();
            adminUser.setUserId(1L);
            adminUser.setUserName("admin");
            adminUser.setNickName("管理员");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setUserType("sys_user");
            adminUser.setStatus("1"); // 1 正常
            adminUser.setDelFlag("0"); // 1 删除
            adminUser.setSex("0");
            adminUser.setEmail("admin@shiyu.ai");
            adminUser.setPhone("13800138000");
            
            sysUserMapper.insert(adminUser);
            log.info("初始化默认管理员账户：用户名=admin, 密码=123456");
        }
    }
}
