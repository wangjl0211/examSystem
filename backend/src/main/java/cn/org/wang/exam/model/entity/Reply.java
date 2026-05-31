package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回复实体类
 *
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 13:16
 */
@Data
@TableName("t_reply")
@Schema(description ="回复实体类")
public class Reply implements Serializable {

    @Schema(description ="id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description ="讨论id")
    private Integer discussionId;

    @Schema(description ="用户id")
    private Integer userId;

    @Schema(description ="父id")
    private Integer parentId;

    @Schema(description ="回复内容")
    private String content;

    @Schema(description ="回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getDiscussionId() { return discussionId; }
    public void setDiscussionId(Integer discussionId) { this.discussionId = discussionId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

