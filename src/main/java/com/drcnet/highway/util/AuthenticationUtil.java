package com.drcnet.highway.util;

import com.drcnet.highway.constants.TipsConsts;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/6 16:52
 * @Description:
 */
public class AuthenticationUtil {

    /**
     * 生成伪随机字符串（大写）
     *
     * @param len 字符串长度
     */
    public static String generateRandomString(int len) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char charAt = (char) (random.nextInt(26) + 65);
            builder.append(charAt);
        }
        return builder.toString();
    }

    /**
     * 生成加密盐，固定15位
     */
    public static String generateSalt() {
        return generateRandomString(15);
    }

    /**
     * 密码加密
     *
     * @param password 密码原文
     * @param salt     盐
     */
    public static String encryptPassword(String password, String salt) {
        Assert.notNull(password, TipsConsts.PASSWORD_NOT_NULL);
        Assert.notNull(salt, TipsConsts.SALT_NOT_NULL);
        String originMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
        return DigestUtils.md5DigestAsHex((originMd5 + salt).getBytes());
    }


}
