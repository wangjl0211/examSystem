package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/8 10:07 AM
 */
@Data
public class ExamSubjectVO {
    private Integer id;

    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 课程id  唯一
     */
    private Integer subjectId;
}