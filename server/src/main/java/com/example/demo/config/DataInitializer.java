package com.example.demo.config;

import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 初始化：为没有密码的用户设置默认密码 123456（BCrypt 加盐哈希）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<User> users = userService.lambdaQuery().isNull(User::getPassword).list();
        if (users.isEmpty()) {
            return;
        }
        for (User user : users) {
            user.setPassword(passwordEncoder.encode("123456"));
            userService.updateById(user);
            log.info("已为用户 [{}] 设置默认密码 123456", user.getUsername());
        }
    }
}
