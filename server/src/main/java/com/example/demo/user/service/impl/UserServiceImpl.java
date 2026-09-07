package com.example.demo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public IPage<User> page(long current, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    public User create(User user) {
        this.save(user);
        return user;
    }

    @Override
    public User update(Long id, User user) {
        User exists = this.getById(id);
        if (exists == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        user.setId(id);
        this.updateById(user);
        return this.getById(id);
    }
}
