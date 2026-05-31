package cn.org.wang.exam.model.form.exam;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 试卷添加请求体
 *
 * @Author Wang
 * @Version
 * @Date 2026/4/5 11:15 PM
 */
@Data
public class ExamAddForm {
    // 考试标题
    @NotBlank(message = "考试标题不能为空")
    @Size(min = 3, max = 20, message = "请输入3-20个字符的考试标题")
    private String title;

    // 考试时长
    @NotNull(message = "请设置考试时间,单位m")
    @Min(value=0,message = "请设置大于0的考试时长")
    private Integer examDuration;

    // 最大切屏次数
    private Integer maxCount;

    // 及格分
    @Min(value=0,message = "及格分数必须大于0")
    @NotNull(message = "及格分不能为空")
    private Integer passedScore;

    // 开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime startTime;

    // 结束时间
    // @Future(message = "结束时间必须是一个必须是一个将来的日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime endTime;
    // 考试课程
    @NotBlank(message = "课程不能为空")
    private String subjectIds;

    // 题库ID
    private Integer repoId;

    // 单选题数量
    @NotNull(message = "及格分不能为空")
    @Min(value = 0)
    private Integer radioCount;

    // 单选题分数
    @NotNull(message = "单选题分数不能为空")
    @Min(value = 0)
    private Integer radioScore;

    // 多选题数量
    @NotNull(message = "多选题数量不能为空")
    @Min(value = 0)
    private Integer multiCount;

    // 多选题分数
    @NotNull(message = "多选题分数不能为空")
    @Min(value = 0)
    private Integer multiScore;

    // 判断题数量
    @NotNull(message = "判断题数量不能为空")
    @Min(value = 0)
    private Integer judgeCount;

    // 判断题分数
    @NotNull(message = "判断题分数不能为空")
    @Min(value = 0)
    private Integer judgeScore;

    // 简答题数量
    @NotNull(message = "简答题数量不能为空")
    @Min(value = 0)
    private Integer saqCount;

    // 简答题分数
    @NotNull(message = "简答题分数不能为空")
    @Min(value = 0)
    private Integer saqScore;

    // 简答题分数
    @NotBlank(message = "添加试题类型不能为空")
    private String addQuype;
    // 简答题分数
    private String quIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getExamDuration() { return examDuration; }
    public void setExamDuration(Integer examDuration) { this.examDuration = examDuration; }
    public Integer getMaxCount() { return maxCount; }
    public void setMaxCount(Integer maxCount) { this.maxCount = maxCount; }
    public Integer getPassedScore() { return passedScore; }
    public void setPassedScore(Integer passedScore) { this.passedScore = passedScore; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getSubjectIds() { return subjectIds; }
    public void setSubjectIds(String subjectIds) { this.subjectIds = subjectIds; }
    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
    public Integer getRadioCount() { return radioCount; }
    public void setRadioCount(Integer radioCount) { this.radioCount = radioCount; }
    public Integer getRadioScore() { return radioScore; }
    public void setRadioScore(Integer radioScore) { this.radioScore = radioScore; }
    public Integer getMultiCount() { return multiCount; }
    public void setMultiCount(Integer multiCount) { this.multiCount = multiCount; }
    public Integer getMultiScore() { return multiScore; }
    public void setMultiScore(Integer multiScore) { this.multiScore = multiScore; }
    public Integer getJudgeCount() { return judgeCount; }
    public void setJudgeCount(Integer judgeCount) { this.judgeCount = judgeCount; }
    public Integer getJudgeScore() { return judgeScore; }
    public void setJudgeScore(Integer judgeScore) { this.judgeScore = judgeScore; }
    public Integer getSaqCount() { return saqCount; }
    public void setSaqCount(Integer saqCount) { this.saqCount = saqCount; }
    public Integer getSaqScore() { return saqScore; }
    public void setSaqScore(Integer saqScore) { this.saqScore = saqScore; }
    public String getAddQuype() { return addQuype; }
    public void setAddQuype(String addQuype) { this.addQuype = addQuype; }
    public String getQuIds() { return quIds; }
    public void setQuIds(String quIds) { this.quIds = quIds; }
}

