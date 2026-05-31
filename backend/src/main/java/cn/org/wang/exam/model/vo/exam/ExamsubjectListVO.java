package cn.org.wang.exam.model.vo.exam;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/11 9:03 AM
 */
@Data
public class ExamsubjectListVO {
    /**
     * id    考试表
     */
    private Integer id;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间     YYYY-MM-DD hh:mm:ss
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 创建时间     YYYY-MM-DD hh:mm:ss
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 考试状态：0正在考试 1考试完成
     */
    private Integer state;

    /**
     * 是否阅卷：-1 未简答题 0 未阅卷 1已阅卷
     */
    private Integer whetherMark;

    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Integer getWhetherMark() { return whetherMark; }
    public void setWhetherMark(Integer whetherMark) { this.whetherMark = whetherMark; }
}
