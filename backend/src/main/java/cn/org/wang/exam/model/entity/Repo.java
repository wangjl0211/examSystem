package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import cn.org.wang.exam.common.base.BaseSerializable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 题库实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="题库实体类")
@TableName("t_repo")
@EqualsAndHashCode(callSuper = true)
public class Repo extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="题库ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="题库标题")
    @NotBlank(message = "题库名不能为空")
    private String title;

    @Schema(description ="是否可以刷题")
    private Integer isExercise;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;
    
    @Schema(description = "分类ID")
    private Integer categoryId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getIsExercise() { return isExercise; }
    public void setIsExercise(Integer isExercise) { this.isExercise = isExercise; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}

