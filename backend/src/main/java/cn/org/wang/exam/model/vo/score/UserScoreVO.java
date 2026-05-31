package cn.org.wang.exam.model.vo.score;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/15 13:38
 */
@Data
public class UserScoreVO {
    private Integer id;
    // 用户ID
    private Integer userId;
    private String title;
    // 真实姓名
    private  String realName;
    private Long userTime;
    // 用户分数
    private Integer userScore;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime limitTime;
    private Integer count;
    private Integer examId;

}
