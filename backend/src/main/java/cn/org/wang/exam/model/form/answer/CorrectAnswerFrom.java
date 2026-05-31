package cn.org.wang.exam.model.form.answer;

import cn.org.wang.exam.common.group.AnswerGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/15 1:37 PM
 */
@Data
public class CorrectAnswerFrom {
    // 被批改人Id
    @NotBlank(message = "被批改人Id不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer userId;
    // 试卷ID
    @NotBlank(message = "试卷Id不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer examId;
    // 试题ID
    @NotBlank(message = "试题Id不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer questionId;
    // 分数
    @NotBlank(message = "分数不能为空",groups = AnswerGroup.CorrectGroup.class)
    private Integer score;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}

