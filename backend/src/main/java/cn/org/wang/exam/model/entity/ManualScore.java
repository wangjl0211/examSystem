package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="人工评分表")
@TableName("t_manual_score")
public class ManualScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="人工评分表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="考试记录答案ID")
    private Integer examQuAnswerId;

    @Schema(description ="批改人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="得分")
    private Integer score;

    @Schema(description ="批改时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getExamQuAnswerId() { return examQuAnswerId; }
    public void setExamQuAnswerId(Integer examQuAnswerId) { this.examQuAnswerId = examQuAnswerId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

