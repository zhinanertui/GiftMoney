package com.example.demo.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.demo.common.Result;
import com.example.demo.system.entity.UserRole;
import com.example.demo.system.mapper.UserRoleMapper;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class UserController {

    private final UserService userService;
    private final UserRoleMapper userRoleMapper;

    /**
     * 分页查询
     * GET /api/users?current=1&size=10&keyword=xx
     */
    @GetMapping
    public Result<IPage<User>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(current, size, keyword));
    }

    /**
     * 全部用户（不分页）
     * GET /api/users/all
     */
    @GetMapping("/all")
    public Result<List<User>> listAll() {
        return Result.ok(userService.list());
    }

    /**
     * 用户详情
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在: " + id);
        }
        return Result.ok(user);
    }

    /**
     * 新增用户
     * POST /api/users
     */
    @PostMapping
    public Result<User> create(@RequestBody CreateReq req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        return Result.ok(userService.create(user));
    }

    /**
     * 修改用户
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody UpdateReq req) {
        User user = new User();
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        return Result.ok(userService.update(id, user));
    }

    /**
     * 删除用户（逻辑删除）
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok();
    }

    /**
     * 查询用户角色
     * GET /api/users/{id}/role
     */
    @GetMapping("/{id}/role")
    public Result<Long> userRole(@PathVariable Long id) {
        UserRole ur = userRoleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, id));
        return Result.ok(ur == null ? null : ur.getRoleId());
    }

    /**
     * 设置用户角色
     * PUT /api/users/{id}/role  body: { "roleId": 2 }
     */
    @PutMapping("/{id}/role")
    public Result<Void> assignRole(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        userRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, id));
        if (roleId != null) {
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
        return Result.ok();
    }

    @Data
    public static class CreateReq {
        @NotBlank(message = "用户名不能为空")
        private String username;
        private String nickname;
        private String email;
        private Integer status;
    }

    @Data
    public static class UpdateReq {
        private String nickname;
        private String email;
        private Integer status;
    }
}
