package cn.org.wang.exam.common.exception;

/**
 * 接口信息服务异常类
 * 用于处理接口信息服务相关的异常
 *
 * @author Wang
 * @since 2026-03-21
 */
public class ApiInfoException extends ServiceRuntimeException {
    
    public ApiInfoException(String message) {
        super(message);
    }
    
    public ApiInfoException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}