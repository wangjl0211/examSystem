package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 考试题库关联实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="考试题库关联实体类")
@TableName("t_exam_repo")
public class ExamRepo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="考试与题库ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="考试ID")
    private Integer examId;

    @Schema(description ="题库ID")
    private Integer repoId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
}

