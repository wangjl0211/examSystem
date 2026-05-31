package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/11 9:45 AM
 */
@Data
public class ExamQuestionListVO {
    // 单选题列表
    private List<ExamQuestionVO> radioList;
    // 多选题列表
    private List<ExamQuestionVO> multiList;
    // 判断题列表
    private List<ExamQuestionVO> judgeList;
    // 简答题列表
    private List<ExamQuestionVO> saqList;
    private Integer examDuration;
    private Long leftSeconds;
    // 考试开始时间的时间戳（毫秒）
    private Long startTime;

    public List<ExamQuestionVO> getRadioList() { return radioList == null ? Collections.emptyList() : List.copyOf(radioList); }
    public void setRadioList(List<ExamQuestionVO> radioList) { this.radioList = radioList; }
    public List<ExamQuestionVO> getMultiList() { return multiList == null ? Collections.emptyList() : List.copyOf(multiList); }
    public void setMultiList(List<ExamQuestionVO> multiList) { this.multiList = multiList; }
    public List<ExamQuestionVO> getJudgeList() { return judgeList == null ? Collections.emptyList() : List.copyOf(judgeList); }
    public void setJudgeList(List<ExamQuestionVO> judgeList) { this.judgeList = judgeList; }
    public List<ExamQuestionVO> getSaqList() { return saqList == null ? Collections.emptyList() : List.copyOf(saqList); }
    public void setSaqList(List<ExamQuestionVO> saqList) { this.saqList = saqList; }
    public Integer getExamDuration() { return examDuration; }
    public void setExamDuration(Integer examDuration) { this.examDuration = examDuration; }
    public Long getLeftSeconds() { return leftSeconds; }
    public void setLeftSeconds(Long leftSeconds) { this.leftSeconds = leftSeconds; }
    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
}
