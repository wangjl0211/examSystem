package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/7 11:10 PM
 */
@Data
public class ExamQuAnswerExtVO {

    private Integer id;
    // 课程ID
    private Integer subjectId;
    /**
     * 试题图片
     */
    private String image;

    /**
     * 答案内容
     */
    private String content;
    /**
     * 顺序
     */
    private Integer sort;
}
