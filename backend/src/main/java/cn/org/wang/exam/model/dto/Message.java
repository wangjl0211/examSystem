package cn.org.wang.exam.model.dto;

import cn.org.wang.exam.model.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/18 21:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description ="消息封装Dto")
public class Message {
    @Schema(description ="消息类型")
    private MessageType type;
    @Schema(description ="消息具体内容")
    private Object data;
}

