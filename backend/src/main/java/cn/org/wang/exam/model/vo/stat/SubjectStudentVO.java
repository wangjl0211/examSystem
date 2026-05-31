package cn.org.wang.exam.model.vo.stat;

import lombok.Data;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2026/5/12 14:38
 */
@Data
public class SubjectStudentVO {
    private Long id;
    // 课程名称
    private String subjectName;
    // 课程下总学生数
    private Integer  totalStudent;

}
