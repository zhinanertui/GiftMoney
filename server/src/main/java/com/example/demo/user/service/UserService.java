package com.example.demo.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.user.entity.User;

public interface UserService extends IService<User> {

    /**
     * 分页查询（按用户名/昵称模糊搜索）
     */
    IPage<User> page(long current, long size, String keyword);

    /**
     * 新增用户
     */
    User create(User user);

    /**
     * 修改用户
     */
    User update(Long id, User user);
}
