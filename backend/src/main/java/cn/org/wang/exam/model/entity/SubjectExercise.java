package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程刷题关联实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="课程刷题关联实体类")
@TableName("t_subject_exercise")
public class SubjectExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="刷题表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="题库ID")
    private Integer repoId;

    @Schema(description ="课程ID")
    private Integer subjectId;

    @Schema(description ="创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}

