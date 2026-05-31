package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 考试试题答案关联实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="考试试题答案关联实体类")
@TableName("t_exam_qu_answer")
public class ExamQuAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="考试记录答案ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="用户ID")
    private Integer userId;

    @Schema(description ="考试ID")
    private Integer examId;

    @Schema(description ="试题ID")
    private Integer questionId;

    @Schema(description ="题目类型")
    private Integer questionType;

    /**
     * 用于客观题，多选题id使用“，”分隔
     */
    @Schema(description ="答案ID")
    private String answerId;

    /**
     * 用于主观题
     */
    @Schema(description ="答案内容")
    private String answerContent;

    /**
     * 0未选中  1选中
     */
    @Schema(description ="是否选中")
    private Integer checkout;

    /**
     * 0未标记  1标记
     */
    @Schema(description ="是否标记")
    private Integer isSign;

    /**
     * 用于客观题，0错误 1正确
     */
    @Schema(description ="是否正确")
    private Integer isRight;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    public String getAnswerId() { return answerId; }
    public void setAnswerId(String answerId) { this.answerId = answerId; }
    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }
    public Integer getCheckout() { return checkout; }
    public void setCheckout(Integer checkout) { this.checkout = checkout; }
    public Integer getIsSign() { return isSign; }
    public void setIsSign(Integer isSign) { this.isSign = isSign; }
    public Integer getIsRight() { return isRight; }
    public void setIsRight(Integer isRight) { this.isRight = isRight; }
}

