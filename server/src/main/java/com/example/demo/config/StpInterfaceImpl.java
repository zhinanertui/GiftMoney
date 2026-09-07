package com.example.demo.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.system.entity.Role;
import com.example.demo.system.entity.UserRole;
import com.example.demo.system.mapper.RoleMapper;
import com.example.demo.system.mapper.UserRoleMapper;
import com.example.demo.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限接口实现：提供用户的角色编码列表与按钮权限标识列表，
 * 使 @SaCheckRole / @SaCheckPermission 注解生效。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final MenuService menuService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return menuService.userPerms(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                .stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }
}
