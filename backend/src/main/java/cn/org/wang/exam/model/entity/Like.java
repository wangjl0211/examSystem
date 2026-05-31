package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.org.wang.exam.common.base.BaseSerializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/16 22:10
 */
@Data
@TableName("t_like")
@Schema(description ="点赞实体类")
@EqualsAndHashCode(callSuper = true)
public class Like extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="讨论id")
    private Integer discussionId;

    @Schema(description ="用户id")
    private Integer userId;

    @Schema(description ="回复id")
    private Integer replyId;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getDiscussionId() { return discussionId; }
    public void setDiscussionId(Integer discussionId) { this.discussionId = discussionId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getReplyId() { return replyId; }
    public void setReplyId(Integer replyId) { this.replyId = replyId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

