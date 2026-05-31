package cn.org.wang.exam.model.form.discussion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 讨论入参
 *
 * @author Wang
 * @version 1.0
 * @since 2026/4/3 9:32
 */
@Data
public class DiscussionForm {
    @NotNull(message = "接收课程id不能为空")
    private Integer subjectId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;

}

