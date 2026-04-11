package com.grain.system.common.util;

import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * AES-256 加解密工具（用于身份证、银行卡等敏感字段）
 */
@Slf4j
@Component
public class AesUtil {

    // 实际项目中密钥从配置文件或环境变量读取，不入库
    private static final String DEFAULT_KEY = "grain-system-aes-key-2026012345";

    private final AES aes;

    public AesUtil(@Value("${aes.key:grain-system-aes-key-2026012345}") String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        // 确保32字节
        byte[] keyArr = new byte[32];
        System.arraycopy(keyBytes, 0, keyArr, 0, Math.min(keyBytes.length, 32));
        this.aes = new AES(keyArr);
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        return aes.encryptHex(plainText);
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) return null;
        return aes.decryptStr(cipherText);
    }

    /**
     * 身份证脱敏：保留前3位和后4位
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 银行卡脱敏：保留前4位和后4位
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) return bankCard;
        return bankCard.substring(0, 4) + " **** **** " + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 手机号脱敏：保留前3位和后4位
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
