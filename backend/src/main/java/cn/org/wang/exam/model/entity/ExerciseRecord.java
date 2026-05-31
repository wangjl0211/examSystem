package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷题记录实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="刷题记录实体类")
@TableName("t_exercise_record")
public class ExerciseRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="刷题记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="题库ID")
    private Integer repoId;

    @Schema(description ="试题ID")
    private Integer questionId;

    @Schema(description ="用户ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="主观题答案")
    private String answer;

    @Schema(description ="题目类型")
    private Integer questionType;

    /**
     * 用于客观题,多选题id使用","分隔
     */
    @Schema(description ="客观题答案集合")
    private String options;

    @Schema(description ="客观题是否正确")
    private Integer isRight;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public Integer getIsRight() { return isRight; }
    public void setIsRight(Integer isRight) { this.isRight = isRight; }
}

