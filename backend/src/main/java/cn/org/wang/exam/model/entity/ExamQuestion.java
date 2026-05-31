package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 考试试题实体类
 *
 * @Author Wang
 * @Version
 * @Date 2026/4/7 3:42 PM
 */
@Data
@Schema(description ="考试试题实体类")
@TableName("t_exam_question")
public class ExamQuestion {
    @Schema(description ="考试试题ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="考试ID")
    private Integer examId;

    @Schema(description ="试题ID")
    private Integer questionId;

    @Schema(description ="分数")
    private Integer score;

    @Schema(description ="排序")
    private Integer sort;

    @Schema(description ="类型")
    private Integer type;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}

