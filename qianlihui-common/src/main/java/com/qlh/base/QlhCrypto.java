package com.qlh.base;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Accessors(chain = true)
@Data
public class QlhCrypto {

    private static final String CHARSET = "utf-8";
    private static final String KEY_SPEC = "AES";
    private static final String INSTANCE = "AES/ECB/PKCS5Padding";

    private String key;
    private String keySpec;
    private String instance;

    // 加密
    public String encrypt(String plainText) {
        return QlhException.runtime(() -> {
            byte[] raw = key.getBytes(CHARSET);
            SecretKeySpec spec = new SecretKeySpec(raw, KEY_SPEC);
            Cipher cipher = Cipher.getInstance(INSTANCE);//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
            return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
        });
    }

    // 解密
    public String decrypt(String plainText) {
        return QlhException.runtime(() -> {
            byte[] raw = key.getBytes(CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, KEY_SPEC);
            Cipher cipher = Cipher.getInstance(INSTANCE);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(new Base64().decode(plainText));
            return new String(original, CHARSET);
        });
    }
}
