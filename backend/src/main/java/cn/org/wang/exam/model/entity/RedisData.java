package cn.org.wang.exam.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Wang
 * @Version
 * @Date 2026/6/9 11:21 PM
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
