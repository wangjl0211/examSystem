package cn.org.wang.exam.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 * @author Wang
 * @version 1.0
 * @since 2026/4/18 21:46
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    // 讨论
    DISCUSSION,
    // 作业
    TASK,
    ;
}
