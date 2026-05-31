package cn.org.wang.exam.common.exception;

/**
 * 说明：
 * 自定义服务异常类
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/3/20 11:15 AM
 */
public class ServiceRuntimeException extends RuntimeException {
    /**
     * 自定义服务异常类构造器
     *
     * @param msg
     */
    public ServiceRuntimeException(String msg) {
        super(msg);
    }
}
