package cn.org.wang.exam.model.form.exercise;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/6 9:52
 */
@Data
public class ExerciseFillAnswerFrom {
    // 题库ID
    @NotNull(message = "题库Id不能为空")
    private Integer repoId;
    // 试题ID
    @NotNull(message = "试题Id不能为空")
    private Integer quId;
    // 作答内容
    @NotBlank(message = "作答内容不能为空")
    private String answer;
    // 试题类型
    @NotNull(message = "试题类型不能为空")
    @Min(value = 1, message = "试题类型最小值应为1")
    @Max(value = 4, message = "试题类型最大值应为4")
    private Integer quType;

    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
    public Integer getQuId() { return quId; }
    public void setQuId(Integer quId) { this.quId = quId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
}

