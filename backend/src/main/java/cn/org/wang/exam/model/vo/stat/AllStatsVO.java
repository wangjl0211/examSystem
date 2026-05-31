package cn.org.wang.exam.model.vo.stat;

import lombok.Data;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2026/5/12 15:59
 */
@Data
public class AllStatsVO {
    // 课程数量
    private Integer classCount;
    // 试卷数量
    private Integer examCount;
    // 试题数量
    private Integer questionCount;

    public Integer getClassCount() { return classCount; }
    public void setClassCount(Integer classCount) { this.classCount = classCount; }
    public Integer getExamCount() { return examCount; }
    public void setExamCount(Integer examCount) { this.examCount = examCount; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
}
