package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户刷题记录实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="用户刷题记录实体类")
@TableName("t_user_exercise_record")
public class UserExerciseRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="用户刷题记录表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="题库ID")
    private Integer repoId;

    @Schema(description ="总题数")
    private Integer totalCount;

    @Schema(description ="已刷题数")
    private Integer exerciseCount;

    @Schema(description ="刷题时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getExerciseCount() { return exerciseCount; }
    public void setExerciseCount(Integer exerciseCount) { this.exerciseCount = exerciseCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

