package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/20 9:26 AM
 */
@Data
public class ExamQuestionVO {
    private Integer id;
    /**
     * 考试id  唯一
     */
    private Integer examId;

    /**
     * 试题id  唯一
     */
    private Integer questionId;
    /**
     * 分数
     */
    private Integer score;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 类型
     */
    private Boolean checkout;
    
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
    private List<OptionVO> options;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Boolean getCheckout() { return checkout; }
    public void setCheckout(Boolean checkout) { this.checkout = checkout; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<OptionVO> getOptions() { return options == null ? Collections.emptyList() : List.copyOf(options); }
    public void setOptions(List<OptionVO> options) { this.options = options; }
}
