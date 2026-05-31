package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import cn.org.wang.exam.common.base.BaseSerializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 考试实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description = "考试实体类")
@TableName("t_exam")
@EqualsAndHashCode(callSuper = true)
public class Exam extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "考试表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "考试名称")
    private String title;

    @Schema(description = "考试时长")
    private Integer examDuration;

    @Schema(description = "及格分")
    private Integer passedScore;

    @Schema(description = "总分数")
    private Integer grossScore;

    @Schema(description = "最大切屏次数")
    private Integer maxCount;

    @Schema(description = "创建者ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description = "单选题数量")
    private Integer radioCount;

    /**
     * 数据库存储*100，前端正常输入和展示/100
     */
    @Schema(description = "单选题成绩")
    private Integer radioScore;

    @Schema(description = "多选题数量")
    private Integer multiCount;

    /**
     * 数据库存储*100，前端正常输入和展示/100
     */
    @Schema(description = "多选题成绩")
    private Integer multiScore;

    @Schema(description = "判断题数量")
    private Integer judgeCount;

    /**
     * 数据库存储*100，前端正常输入和展示/100
     */
    @Schema(description = "判断题成绩")
    private Integer judgeScore;

    @Schema(description = "简答题数量")
    private Integer saqCount;

    /**
     * 数据库存储*100，前端正常输入和展示/100
     */
    @Schema(description = "简答题成绩")
    private Integer saqScore;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @Schema(description = "逻辑删除字段")
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
