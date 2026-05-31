package cn.org.wang.exam.model.form.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/7 11:54 PM
 */
@Data
public class ExamQuAnswerFrom {
    // 试卷ID
    private Integer examId;
    // 试题ID
    private Integer quId;
    // 回答答案
    @NotBlank
    private String answer;
}

