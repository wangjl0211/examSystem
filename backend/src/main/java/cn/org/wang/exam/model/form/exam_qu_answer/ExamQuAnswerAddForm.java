package cn.org.wang.exam.model.form.exam_qu_answer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/6 11:27 AM
 */
@Data
public class ExamQuAnswerAddForm {
    // 试卷ID
    private Integer examId;
    // 试题ID
    private Integer quId;
    // 回答答案
    @NotBlank
    private String answer;

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Integer getQuId() { return quId; }
    public void setQuId(Integer quId) { this.quId = quId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}

