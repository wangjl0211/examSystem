package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/1 3:42 PM
 */
@Data
public class ExamDetailVO {

    /**
     * id    考试表
     */
    private Integer id;
    /**
     * 发布人
     */
    private String username;
    /**
     * 考试名称
     */
    private String title;

    /**
     * 考试时长
     */
    private Integer examDuration;

    /**
     * 及格分
     */
    private Integer passedScore;

    /**
     * 总分数
     */
    private Integer grossScore;

    /**
     * 最大切屏次数
     */
    private Integer maxCount;

    /**
     * 创建者id
     */
    private Integer userId;

    /**
     * 单选题数量
     */
    private Integer radioCount;

    /**
     * 单选题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer radioScore;

    /**
     * 多选题数量
     */
    private Integer multiCount;

    /**
     * 多选题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer multiScore;

    /**
     * 判断题数量
     */
    private Integer judgeCount;

    /**
     * 判断题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer judgeScore;

    /**
     * 简答题数量
     */
    private Integer saqCount;

    /**
     * 简答题成绩     数据库存储*100，前端正常输入和展示/100
     */
    private Integer saqScore;

    /**
     * 开始时间     YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime startTime;

    /**
     * 结束时间     YYYY-MM-DD hh:mm:ss
     */
    private LocalDateTime endTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getExamDuration() { return examDuration; }
    public void setExamDuration(Integer examDuration) { this.examDuration = examDuration; }
    public Integer getPassedScore() { return passedScore; }
    public void setPassedScore(Integer passedScore) { this.passedScore = passedScore; }
    public Integer getGrossScore() { return grossScore; }
    public void setGrossScore(Integer grossScore) { this.grossScore = grossScore; }
    public Integer getMaxCount() { return maxCount; }
    public void setMaxCount(Integer maxCount) { this.maxCount = maxCount; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
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
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
