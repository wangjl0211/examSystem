package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="用户考试分数记录实体类")
@TableName("t_user_exams_score")
public class UserExamsScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="用户考试成绩表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="用户id")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="试卷id")
    private Integer examId;

    @Schema(description ="总时长（秒）")
    private Long totalTime;

    @Schema(description ="用户用时（秒）")
    private Long userTime;

    @Schema(description ="用户得分")
    private Integer userScore;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @Schema(description ="交卷时间")
    private LocalDateTime limitTime;

    @Schema(description ="切屏次数")
    private Integer count;

    /**
     * 0正在考试 1考试完成
     */
    @Schema(description ="状态")
    private Integer state;

    /**
     * -1 未简答题 0 未阅卷 1已阅卷
     */
    @Schema(description ="是否阅卷")
    private Integer whetherMark;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Long getTotalTime() { return totalTime; }
    public void setTotalTime(Long totalTime) { this.totalTime = totalTime; }
    public Long getUserTime() { return userTime; }
    public void setUserTime(Long userTime) { this.userTime = userTime; }
    public Integer getUserScore() { return userScore; }
    public void setUserScore(Integer userScore) { this.userScore = userScore; }
    public LocalDateTime getLimitTime() { return limitTime; }
    public void setLimitTime(LocalDateTime limitTime) { this.limitTime = limitTime; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Integer getWhetherMark() { return whetherMark; }
    public void setWhetherMark(Integer whetherMark) { this.whetherMark = whetherMark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

