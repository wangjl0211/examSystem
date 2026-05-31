package cn.org.wang.exam.model.vo.score;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/15 9:28
 */
@Data
public class SubjectScoreVO {
    private Integer id;
    // 试卷ID
    private Integer examId;
    // 课程ID
    private Integer subjectId;
    private Integer passedScore;
    // 试卷标题
    private String examTitle;
    // 课程名称
    private String subjectName;
    // 创建试卷
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 平均分
    private Integer avgScore;
    // 最高分
    private Integer maxScore;
    // 最低分
    private Integer minScore;
    // 参考人数
    private Integer attendNum;
    // 缺考人数
    private Integer absentNum;
    // 及格人数
    private Integer passedNum;
    // 应参加人数
    private Integer totalNum;
    // 通过率
    private Double passingRate;
}
