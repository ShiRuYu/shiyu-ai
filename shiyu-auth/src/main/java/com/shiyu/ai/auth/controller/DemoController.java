package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.SysUserDO;
import com.shiyu.ai.auth.domain.bo.SysDeptBO;
import com.shiyu.ai.auth.domain.bo.SysMenuBO;
import com.shiyu.ai.auth.domain.bo.SysPostBO;
import com.shiyu.ai.auth.domain.bo.SysRoleBO;
import com.shiyu.ai.auth.domain.bo.SysTenantBO;
import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.mapper.SysUserMapper;
import com.shiyu.ai.auth.service.SysDeptService;
import com.shiyu.ai.auth.service.SysMenuService;
import com.shiyu.ai.auth.service.SysPostService;
import com.shiyu.ai.auth.service.SysRoleService;
import com.shiyu.ai.auth.service.SysTenantService;
import com.shiyu.ai.auth.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Demo
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/demo")
public class DemoController {
    private final SysUserMapper sysUserMapper;
    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;
    private final SysRoleService sysRoleService;
    private final SysPostService sysPostService;
    private final SysMenuService sysMenuService;
    private final SysTenantService sysTenantService;

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

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<Pair<Long, List<SysUserBO>>> getUsers(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysUserService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public Result<SysUserBO> getUser(@PathVariable Long userId) {
        return Result.success(sysUserService.getById(userId));
    }

    /**
     * 创建用户
     */
    @PostMapping("/users")
    public Result<SysUserBO> createUser(@RequestBody SysUserBO sysUserBO) {
        return Result.success(sysUserService.create(sysUserBO));
    }

    /**
     * 更新用户
     */
    @PutMapping("/users/{userId}")
    public Result<SysUserBO> updateUser(@PathVariable Long userId, @RequestBody SysUserBO sysUserBO) {
        sysUserBO.setUserId(userId);
        return Result.success(sysUserService.update(sysUserBO));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        sysUserService.deleteById(userId);
        return Result.success();
    }

    // ==================== 部门管理 ====================

    /**
     * 获取部门列表
     */
    @GetMapping("/depts")
    public Result<Pair<Long, List<SysDeptBO>>> getDepts(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysDeptService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/depts/{deptId}")
    public Result<SysDeptBO> getDept(@PathVariable Long deptId) {
        return Result.success(sysDeptService.getById(deptId));
    }

    /**
     * 创建部门
     */
    @PostMapping("/depts")
    public Result<SysDeptBO> createDept(@RequestBody SysDeptBO sysDeptBO) {
        return Result.success(sysDeptService.create(sysDeptBO));
    }

    /**
     * 更新部门
     */
    @PutMapping("/depts/{deptId}")
    public Result<SysDeptBO> updateDept(@PathVariable Long deptId, @RequestBody SysDeptBO sysDeptBO) {
        sysDeptBO.setDeptId(deptId);
        return Result.success(sysDeptService.update(sysDeptBO));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/depts/{deptId}")
    public Result<Void> deleteDept(@PathVariable Long deptId) {
        sysDeptService.deleteById(deptId);
        return Result.success();
    }

    // ==================== 角色管理 ====================

    /**
     * 获取角色列表
     */
    @GetMapping("/roles")
    public Result<Pair<Long, List<SysRoleBO>>> getRoles(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysRoleService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/roles/{roleId}")
    public Result<SysRoleBO> getRole(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getById(roleId));
    }

    /**
     * 创建角色
     */
    @PostMapping("/roles")
    public Result<SysRoleBO> createRole(@RequestBody SysRoleBO sysRoleBO) {
        return Result.success(sysRoleService.create(sysRoleBO));
    }

    /**
     * 更新角色
     */
    @PutMapping("/roles/{roleId}")
    public Result<SysRoleBO> updateRole(@PathVariable Long roleId, @RequestBody SysRoleBO sysRoleBO) {
        sysRoleBO.setRoleId(roleId);
        return Result.success(sysRoleService.update(sysRoleBO));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        sysRoleService.deleteById(roleId);
        return Result.success();
    }

    // ==================== 岗位管理 ====================

    /**
     * 获取岗位列表
     */
    @GetMapping("/posts")
    public Result<Pair<Long, List<SysPostBO>>> getPosts(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysPostService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/posts/{postId}")
    public Result<SysPostBO> getPost(@PathVariable Long postId) {
        return Result.success(sysPostService.getById(postId));
    }

    /**
     * 创建岗位
     */
    @PostMapping("/posts")
    public Result<SysPostBO> createPost(@RequestBody SysPostBO sysPostBO) {
        return Result.success(sysPostService.create(sysPostBO));
    }

    /**
     * 更新岗位
     */
    @PutMapping("/posts/{postId}")
    public Result<SysPostBO> updatePost(@PathVariable Long postId, @RequestBody SysPostBO sysPostBO) {
        sysPostBO.setPostId(postId);
        return Result.success(sysPostService.update(sysPostBO));
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        sysPostService.deleteById(postId);
        return Result.success();
    }

    // ==================== 菜单管理 ====================

    /**
     * 获取菜单列表
     */
    @GetMapping("/menus")
    public Result<Pair<Long, List<SysMenuBO>>> getMenus(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysMenuService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/menus/{menuId}")
    public Result<SysMenuBO> getMenu(@PathVariable Long menuId) {
        return Result.success(sysMenuService.getById(menuId));
    }

    /**
     * 创建菜单
     */
    @PostMapping("/menus")
    public Result<SysMenuBO> createMenu(@RequestBody SysMenuBO sysMenuBO) {
        return Result.success(sysMenuService.create(sysMenuBO));
    }

    /**
     * 更新菜单
     */
    @PutMapping("/menus/{menuId}")
    public Result<SysMenuBO> updateMenu(@PathVariable Long menuId, @RequestBody SysMenuBO sysMenuBO) {
        sysMenuBO.setMenuId(menuId);
        return Result.success(sysMenuService.update(sysMenuBO));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/menus/{menuId}")
    public Result<Void> deleteMenu(@PathVariable Long menuId) {
        sysMenuService.deleteById(menuId);
        return Result.success();
    }

    // ==================== 租户管理 ====================

    /**
     * 获取租户列表
     */
    @GetMapping("/tenants")
    public Result<Pair<Long, List<SysTenantBO>>> getTenants(@RequestParam(defaultValue = "1") Number pageNumber,
                                                             @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysTenantService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取租户详情
     */
    @GetMapping("/tenants/{id}")
    public Result<SysTenantBO> getTenant(@PathVariable Long id) {
        return Result.success(sysTenantService.getById(id));
    }

    /**
     * 创建租户
     */
    @PostMapping("/tenants")
    public Result<SysTenantBO> createTenant(@RequestBody SysTenantBO sysTenantBO) {
        return Result.success(sysTenantService.create(sysTenantBO));
    }

    /**
     * 更新租户
     */
    @PutMapping("/tenants/{id}")
    public Result<SysTenantBO> updateTenant(@PathVariable Long id, @RequestBody SysTenantBO sysTenantBO) {
        sysTenantBO.setId(id);
        return Result.success(sysTenantService.update(sysTenantBO));
    }

    /**
     * 删除租户
     */
    @DeleteMapping("/tenants/{id}")
    public Result<Void> deleteTenant(@PathVariable Long id) {
        sysTenantService.deleteById(id);
        return Result.success();
    }
}
