package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询用户（经角色）可见的所有菜单（含按钮）
     */
    @Select("SELECT DISTINCT m.id, m.parent_id, m.menu_name, m.menu_type, m.route_path, m.component, m.icon, m.perms, m.sort " +
            "FROM tb_menu m " +
            "JOIN tb_role_menu rm ON m.id = rm.menu_id " +
            "JOIN tb_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.del_yn = 0 " +
            "ORDER BY m.sort, m.id")
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);
}
