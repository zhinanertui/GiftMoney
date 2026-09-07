package com.example.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Menu;
import com.example.demo.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class MenuController {

    private final MenuService menuService;

    /**
     * 全量菜单树（含按钮）
     */
    @GetMapping("/tree")
    public Result<List<Menu>> tree() {
        return Result.ok(menuService.tree());
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Menu> create(@RequestBody Menu menu) {
        menu.setId(null);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuService.save(menu);
        return Result.ok(menu);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<Menu> update(@PathVariable Long id, @RequestBody Menu menu) {
        Menu exists = menuService.getById(id);
        if (exists == null) {
            return Result.fail(404, "菜单不存在: " + id);
        }
        menu.setId(id);
        menuService.updateById(menu);
        return Result.ok(menuService.getById(id));
    }

    /**
     * 删除（有子菜单时禁止删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        long childCount = menuService.lambdaQuery().eq(Menu::getParentId, id).count();
        if (childCount > 0) {
            return Result.fail(400, "存在子菜单，请先删除子菜单");
        }
        menuService.removeById(id);
        return Result.ok();
    }
}
