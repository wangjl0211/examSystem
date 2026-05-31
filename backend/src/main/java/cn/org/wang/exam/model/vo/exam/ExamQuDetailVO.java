package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/1 3:39 PM
 */
@Data
public class ExamQuDetailVO {

    /**
     * 图片
     */
    private String image;

    /**
     * 题目内容
     */
    private String content;
    /**
     * 答案内容
     */
    private List<OptionVO> answerList;
    // 试题类型
    private Integer quType;
    /**
     * 排序
     */
    private Integer sort;

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<OptionVO> getAnswerList() { return answerList == null ? Collections.emptyList() : List.copyOf(answerList); }
    public void setAnswerList(List<OptionVO> answerList) { this.answerList = answerList; }
    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
