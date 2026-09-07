package com.example.demo.config;

import com.example.demo.auth.util.RsaUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;

/**
 * RSA 密钥对：应用启动时生成，供密码传输加密使用。
 * 每次重启密钥会更新，前端每次登录前都会重新拉取公钥，因此无影响。
 */
@Configuration
public class RsaConfig {

    @Bean
    public KeyPair rsaKeyPair() {
        return RsaUtils.generateKeyPair();
    }
}
