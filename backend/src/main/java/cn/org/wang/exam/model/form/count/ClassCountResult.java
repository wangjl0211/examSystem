package cn.org.wang.exam.model.form.count;

import lombok.Data;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2026/5/13 11:19
 */
@Data
public class ClassCountResult {
    // 课程ID
    private Integer subjectId;
    // 课程名称
    private String subjectName;
    private Integer count;
    // 课程数量
    private int subjectCount;
    // 试卷数量
    private  int examCount;
    // 试题数量
    private int questionCount;
}
