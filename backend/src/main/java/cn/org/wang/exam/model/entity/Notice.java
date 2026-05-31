package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体类
 *
Apiauthor WeiJin
 * @since 2026-03-21
 */
@Data
@Schema(description ="公告实体类")
@TableName("t_notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="公告ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="公告标题")
    private String title;

    @Schema(description ="图片地址")
    private String image;

    @Schema(description ="公告内容")
    private String content;

    @Schema(description ="创建用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 教师：所有课程用户 管理员：所有用户
     */
    @Schema(description ="是否公开")
    private Integer isPublic;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

