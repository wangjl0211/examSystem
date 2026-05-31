package cn.org.wang.exam.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常拦截器
 * 增强：添加兜底异常处理，修复DuplicateKeyException解析脆弱问题
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/29 16:10
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 兜底异常处理 - 防止未捕获异常泄漏堆栈信息
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.failed("系统繁忙，请稍后重试");
    }

    /**
     * 处理自定义服务异常拦截处理
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(ServiceRuntimeException.class)
    public Result<String> handleServiceRuntimeException(ServiceRuntimeException e) {
        String message = e.getMessage();
        log.error("接口调用异常: {}", message);
        return Result.failed(message);
    }

    /**
     * 处理参数校验异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e.getClass());
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.failed(message);
    }

    /**
     * 处理唯一约束异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("重复");
    }

    /**
     * 处理请求参数无法解析异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("请求参数无法解析");
    }

    /**
     * 处理请求参数缺失异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed(e.getParameterName() + "为必填项");
    }

    /**
     * 处理主键冲突异常
     * 修复P2问题：防御性解析，避免数组越界
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("主键冲突: {}", e.getMessage());
        // 防御性解析，避免不同MySQL版本导致的数组越界
        String message = e.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            try {
                String[] parts = message.split("'");
                if (parts.length >= 2) {
                    return Result.failed(parts[1] + " 已存在");
                }
            } catch (Exception ex) {
                log.warn("解析DuplicateKeyException失败", ex);
            }
        }
        return Result.failed("数据重复，请检查后重试");
    }

    /**
     * 处理无权限访问异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("Access Denied - 请求路径: {}, 原因: {}", request.getRequestURI(), e.getMessage());
        return Result.failed("你没有该资源的访问权限");
    }


    /**
     * 处理文件太大异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handlerMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("文件太大，最大上传5MB");
    }

    /**
     * 处理文件获取不到异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<String> handlerMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("没有获取到文件");
    }


    /**
     * 处理约束违反异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed(e.getMessage());
    }
}
