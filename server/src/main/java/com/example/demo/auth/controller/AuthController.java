package com.example.demo.auth.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.auth.dto.LoginReq;
import com.example.demo.auth.dto.RegisterReq;
import com.example.demo.auth.dto.ResetPasswordReq;
import com.example.demo.auth.util.RsaUtils;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Role;
import com.example.demo.system.entity.UserRole;
import com.example.demo.system.mapper.RoleMapper;
import com.example.demo.system.mapper.UserRoleMapper;
import com.example.demo.system.service.MenuService;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：公钥下发、登录、注册、找回密码、登出、当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KeyPair rsaKeyPair;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final MenuService menuService;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 获取 RSA 公钥（PEM 格式，前端用于加密密码）
     * GET /api/auth/public-key
     */
    @GetMapping("/public-key")
    public Result<String> publicKey() {
        return Result.ok(RsaUtils.publicKeyToPem(rsaKeyPair.getPublic()));
    }

    /**
     * 登录：私钥解密 → BCrypt 校验 → 签发 token
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginReq req) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, req.getUsername())
                .one();
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        String plainPassword = decryptPassword(req.getPassword());
        if (user.getPassword() == null || !passwordEncoder.matches(plainPassword, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        Map<String, Object> data = new HashMap<>();
        data.put("tokenName", tokenInfo.getTokenName());
        data.put("tokenValue", tokenInfo.getTokenValue());
        data.put("user", buildUserVo(user));
        return Result.ok(data);
    }

    /**
     * 注册（仅账号密码，邮箱选填用于找回）
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterReq req) {
        String username = req.getUsername().trim();
        if (userService.lambdaQuery().eq(User::getUsername, username).count() > 0) {
            throw new IllegalArgumentException("该用户名已被注册");
        }

        String plainPassword = decryptPassword(req.getPassword());

        User user = new User();
        user.setUsername(username);
        user.setNickname(username);
        user.setEmail(StringUtils.hasText(req.getEmail()) ? req.getEmail().trim() : null);
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(plainPassword));
        userService.save(user);

        // 默认分配「租户」角色，避免新用户无任何权限导致空白页
        Role tenantRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "tenant"));
        if (tenantRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(tenantRole.getId());
            userRoleMapper.insert(ur);
        }

        // 注册后直接登录
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        Map<String, Object> data = new HashMap<>();
        data.put("tokenName", tokenInfo.getTokenName());
        data.put("tokenValue", tokenInfo.getTokenValue());
        data.put("user", buildUserVo(user));
        return Result.ok(data);
    }

    /**
     * 忘记密码：用户名 + 邮箱匹配后重置密码
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, req.getUsername().trim())
                .one();
        if (user == null) {
            throw new IllegalArgumentException("用户名或邮箱不匹配");
        }
        if (!StringUtils.hasText(user.getEmail()) || !user.getEmail().equalsIgnoreCase(req.getEmail().trim())) {
            throw new IllegalArgumentException("用户名或邮箱不匹配");
        }

        String plainPassword = decryptPassword(req.getNewPassword());
        user.setPassword(passwordEncoder.encode(plainPassword));
        userService.updateById(user);
        return Result.ok();
    }

    /**
     * 登出
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    /**
     * 当前登录用户
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Map<String, Object> vo = buildUserVo(user);
        vo.put("roles", StpUtil.getRoleList());
        return Result.ok(vo);
    }

    /**
     * 当前用户菜单树 + 按钮权限 + 角色（供前端动态菜单）
     * GET /api/auth/menus
     */
    @GetMapping("/menus")
    public Result<Map<String, Object>> menus() {
        long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> data = new HashMap<>();
        data.put("menus", menuService.userMenuTree(userId));
        data.put("perms", menuService.userPerms(userId));
        data.put("roles", StpUtil.getRoleList());
        return Result.ok(data);
    }

    /**
     * RSA 私钥解密密码密文
     */
    private String decryptPassword(String cipher) {
        try {
            return RsaUtils.decrypt(cipher, rsaKeyPair.getPrivate());
        } catch (Exception e) {
            throw new IllegalArgumentException("密码解密失败，请刷新页面重试");
        }
    }

    private Map<String, Object> buildUserVo(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("username", user.getUsername());
        vo.put("nickname", user.getNickname());
        vo.put("email", user.getEmail());
        return vo;
    }
}
