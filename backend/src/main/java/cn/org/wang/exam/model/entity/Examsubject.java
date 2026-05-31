package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户课程关联实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="用户课程关联实体类")
@TableName("t_exam_subject")
public class Examsubject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="考试与课程关系表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="考试ID")
    private Integer examId;

    @Schema(description ="课程ID")
    private Integer subjectId;
}

