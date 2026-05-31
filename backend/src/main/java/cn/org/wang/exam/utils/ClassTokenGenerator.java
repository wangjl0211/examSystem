package cn.org.wang.exam.utils;

import java.security.SecureRandom;

/**
 * 课程代码工具类
 *
 * @Author Wang
 * @Version
 * @Date 2026/3/28 1:53 PM
 */
public class ClassTokenGenerator {

    private ClassTokenGenerator() {
    }

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成课程口令
     * @param length
     * @return
     */
    public static String generateClassToken(int length) {
        StringBuilder tokenBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            tokenBuilder.append(CHAR_POOL.charAt(SECURE_RANDOM.nextInt(CHAR_POOL.length())));
        }
        return tokenBuilder.toString();
    }
}