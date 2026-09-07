package com.example.demo.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.entity.Menu;

import java.util.List;

public interface MenuService extends IService<Menu> {

    /**
     * 全量菜单树（含按钮，供菜单管理）
     */
    List<Menu> tree();

    /**
     * 用户可见菜单树（目录+菜单，不含按钮，供前端动态菜单）
     */
    List<Menu> userMenuTree(Long userId);

    /**
     * 用户按钮权限标识列表
     */
    List<String> userPerms(Long userId);
}
