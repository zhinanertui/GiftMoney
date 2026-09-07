package com.example.demo.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    /**
     * 查询角色已分配的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 为角色分配菜单（全量替换）
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
