package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.Role;
import com.example.demo.system.entity.RoleMenu;
import com.example.demo.system.mapper.RoleMapper;
import com.example.demo.system.mapper.RoleMenuMapper;
import com.example.demo.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleMenu>()
                                .eq(RoleMenu::getRoleId, roleId))
                .stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除旧关联
        roleMenuMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId));
        // 插入新关联
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
