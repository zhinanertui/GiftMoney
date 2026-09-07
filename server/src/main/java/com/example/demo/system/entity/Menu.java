package com.example.demo.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单表 tb_menu
 */
@Data
@TableName("tb_menu")
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父菜单ID，0=顶级 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 类型 1目录 2菜单 3按钮 */
    private Integer menuType;

    /** 路由地址 */
    private String routePath;

    /** 前端组件名 */
    private String component;

    /** 图标 */
    private String icon;

    /** 权限标识（按钮） */
    private String perms;

    /** 排序 */
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer delYn;

    /** 子菜单（树结构用，非表字段） */
    @TableField(exist = false)
    private List<Menu> children = new ArrayList<>();
}
