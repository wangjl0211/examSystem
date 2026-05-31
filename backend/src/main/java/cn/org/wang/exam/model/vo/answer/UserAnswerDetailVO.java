package cn.org.wang.exam.model.vo.answer;

import lombok.Data;

/**
 * 用户作答信息
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/29 10:29
 */
@Data
public class UserAnswerDetailVO {
    // 试题ID
    private Integer quId;
    // 用户ID
    private Integer userId;
    // 试卷ID
    private Integer examId;
    // 试题标题
    private String quTitle;
    // 试题图片
    private String quImg;
    // 题目类型：1-单选题，2-多选题，3-判断题，4-简答题
    private Integer quType;
    private String answer;
    private String refAnswer;
    private Integer totalScore;
    // 是否正确：1-正确，0-错误，-1-未批改
    private Integer isRight;

}
