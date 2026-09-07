package com.example.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Role;
import com.example.demo.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class RoleController {

    private final RoleService roleService;

    /**
     * 角色列表
     */
    @GetMapping
    public Result<List<Role>> list() {
        return Result.ok(roleService.lambdaQuery()
                .orderByAsc(Role::getId)
                .list());
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Role> detail(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        long exists = roleService.lambdaQuery().eq(Role::getRoleCode, role.getRoleCode()).count();
        if (exists > 0) {
            return Result.fail(400, "角色编码已存在: " + role.getRoleCode());
        }
        role.setId(null);
        roleService.save(role);
        return Result.ok(role);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Role role) {
        Role exists = roleService.getById(id);
        if (exists == null) {
            return Result.fail(404, "角色不存在: " + id);
        }
        role.setId(id);
        roleService.updateById(role);
        return Result.ok(roleService.getById(id));
    }

    /**
     * 删除（admin 内置角色不可删）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.fail(404, "角色不存在: " + id);
        }
        if ("admin".equals(role.getRoleCode())) {
            return Result.fail(400, "内置管理员角色不可删除");
        }
        roleService.removeById(id);
        return Result.ok();
    }

    /**
     * 查询角色已分配的菜单ID
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> roleMenus(@PathVariable Long id) {
        return Result.ok(roleService.getRoleMenuIds(id));
    }

    /**
     * 为角色分配菜单
     */
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignMenus(id, body.get("menuIds"));
        return Result.ok();
    }
}
