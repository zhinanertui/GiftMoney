package com.example.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** RSA 公钥加密后的密文（Base64） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
