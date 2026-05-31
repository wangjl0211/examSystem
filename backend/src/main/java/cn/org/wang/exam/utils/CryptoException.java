package cn.org.wang.exam.utils;

/**
 * 加密相关的专用异常类
 * @Author Wang
 * @Version
 * @Date 2026/2/18 10:00 AM
 */
public class CryptoException extends RuntimeException {
    public CryptoException() {
        super();
    }

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }

    protected CryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}