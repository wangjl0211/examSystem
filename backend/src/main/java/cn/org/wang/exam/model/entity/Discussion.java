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
 * @since 2026/4/3 9:26
 */
@Data
@Schema(description ="讨论实体类")
@TableName("t_discussion")
@EqualsAndHashCode(callSuper = true)
public class Discussion extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description ="发布人id")
    private Integer userId;

    @Schema(description ="接收课程id")
    private Integer subjectId;

    @Schema(description ="标题")
    private String title;

    @Schema(description ="内容")
    private String content;

    @Schema(description ="发布时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

