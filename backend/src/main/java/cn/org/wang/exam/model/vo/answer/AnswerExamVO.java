package cn.org.wang.exam.model.vo.answer;

import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/15 2:17 PM
 */
@Data
public class AnswerExamVO {
    // 试卷ID
    private Integer examId;
    // 试卷标题
    private String examTitle;
    // 是否需要阅卷
    private Integer neededMark;
    // 课程总人数
    private Integer classSize;

    private Integer numberOfApplicants;
    // 已阅人数
    private Integer correctedPaper;

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
    public Integer getNeededMark() { return neededMark; }
    public void setNeededMark(Integer neededMark) { this.neededMark = neededMark; }
    public Integer getClassSize() { return classSize; }
    public void setClassSize(Integer classSize) { this.classSize = classSize; }
    public Integer getNumberOfApplicants() { return numberOfApplicants; }
    public void setNumberOfApplicants(Integer numberOfApplicants) { this.numberOfApplicants = numberOfApplicants; }
    public Integer getCorrectedPaper() { return correctedPaper; }
    public void setCorrectedPaper(Integer correctedPaper) { this.correctedPaper = correctedPaper; }
}
