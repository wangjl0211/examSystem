package cn.org.wang.exam.model.form.like;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/16 22:16
 */
@Data
public class LikeForm {
    @NotNull(message = "讨论id不能为空")
    private Integer discussionId;

    @NotNull(message = "回复id不能为空")
    private Integer replyId;

    public Integer getDiscussionId() { return discussionId; }
    public void setDiscussionId(Integer discussionId) { this.discussionId = discussionId; }
    public Integer getReplyId() { return replyId; }
    public void setReplyId(Integer replyId) { this.replyId = replyId; }
}

