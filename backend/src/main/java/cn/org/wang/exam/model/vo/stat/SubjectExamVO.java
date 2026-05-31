package cn.org.wang.exam.model.vo.stat;

import lombok.Data;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2026/5/11 15:45
 */
@Data
public class SubjectExamVO {
    private Integer id;
    // 课程名称
    private String subjectName;
    private Integer total;
}
