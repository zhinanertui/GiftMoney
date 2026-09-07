package com.example.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    /** RSA 公钥加密后的新密码（Base64） */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
