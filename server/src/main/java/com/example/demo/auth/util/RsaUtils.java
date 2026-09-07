package com.example.demo.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

/**
 * RSA 非对称加密工具：公钥加密 / 私钥解密，用于密码传输加密。
 * 说明：RSA 用于「传输」环节加密；密码「存储」环节用 BCrypt 加盐哈希，两者配合。
 */
public final class RsaUtils {

    private static final String ALGORITHM = "RSA";

    private RsaUtils() {
    }

    /**
     * 生成 2048 位 RSA 密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("生成 RSA 密钥对失败", e);
        }
    }

    /**
     * 公钥加密（Base64 输出）
     */
    public static String encrypt(String plainText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("RSA 公钥加密失败", e);
        }
    }

    /**
     * 私钥解密
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("RSA 私钥解密失败，密文无效", e);
        }
    }

    /**
     * 将公钥转成 PEM 字符串（前端 jsencrypt 直接使用）
     */
    public static String publicKeyToPem(PublicKey publicKey) {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    public static PublicKey parsePublicKey(byte[] encoded) {
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new IllegalStateException("解析公钥失败", e);
        }
    }

    public static PrivateKey parsePrivateKey(byte[] encoded) {
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new IllegalStateException("解析私钥失败", e);
        }
    }
}
