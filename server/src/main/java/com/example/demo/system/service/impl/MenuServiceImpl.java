package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.Menu;
import com.example.demo.system.mapper.MenuMapper;
import com.example.demo.system.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<Menu> tree() {
        List<Menu> all = this.lambdaQuery()
                .orderByAsc(Menu::getSort)
                .orderByAsc(Menu::getId)
                .list();
        return buildTree(all, 0L);
    }

    @Override
    public List<Menu> userMenuTree(Long userId) {
        List<Menu> menus = baseMapper.selectMenusByUserId(userId).stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() != 3)
                .collect(Collectors.toList());
        return buildTree(menus, 0L);
    }

    @Override
    public List<String> userPerms(Long userId) {
        return baseMapper.selectMenusByUserId(userId).stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() == 3)
                .map(Menu::getPerms)
                .filter(p -> p != null && !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 构建树（parentId 表示父节点）
     */
    private List<Menu> buildTree(List<Menu> list, Long parentId) {
        List<Menu> result = new ArrayList<>();
        for (Menu menu : list) {
            Long pid = menu.getParentId() == null ? 0L : menu.getParentId();
            if (pid.equals(parentId)) {
                menu.setChildren(buildTree(list, menu.getId()));
                result.add(menu);
            }
        }
        return result;
    }
}
