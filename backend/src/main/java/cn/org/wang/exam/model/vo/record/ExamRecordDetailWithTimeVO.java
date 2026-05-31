package cn.org.wang.exam.model.vo.record;

import lombok.Data;

import java.util.List;

@Data
public class ExamRecordDetailWithTimeVO {
    // 题目列表
    private List<ExamRecordDetailVO> questions;
    // 用户用时（秒）
    private Long userTime;
    // 用户得分
    private Integer userScore;
    // 总分
    private Integer totalScore;
    // 交卷时间
    private String limitTime;
}