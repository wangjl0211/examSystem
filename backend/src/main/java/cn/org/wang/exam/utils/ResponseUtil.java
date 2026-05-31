package cn.org.wang.exam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.org.wang.exam.common.result.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * 响应体封装工具类
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/25 18:42
 */
@Component
public class ResponseUtil {

    @Resource
    private ObjectMapper objectMapper;

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public void response(HttpServletResponse response, Result<?> result) {
        String s = objectMapper.writeValueAsString(result);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(s);
        writer.flush();
        writer.close();
    }

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public void response(HttpServletResponse response, Result<?> result, Integer status) {
        String s = objectMapper.writeValueAsString(result);
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(s);
        writer.flush();
        writer.close();
    }

    public static <T> Result<T> success() {
        return Result.success();
    }

    public static <T> Result<T> success(T data) {
        return Result.success("操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    public static <T> Result<T> error(String message) {
        return Result.failed(message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(null);
        return result;
    }
}

