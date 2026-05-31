package cn.org.wang.exam.model.form.reply;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 14:09
 */
@Data
public class ReplyForm {
    @NotNull(message = "讨论id都不能为空")
    private Integer discussionId;

    private Integer parentId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}

