package cn.org.wang.exam.model.vo.record;

import lombok.Data;

import java.util.List;

import cn.org.wang.exam.model.entity.Option;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/30 11:44 AM
 */
@Data
public class ExamRecordDetailVO {
    // 1、题干 2、选项 3、自己的答案 4、正确的答案 5、是否正确 6、试题分析 7、得分 8、题目分数
    /**
     * 题干
     */
    private String title;
    /**
     * 题干图片
     */
    private String image;
    /**
     * 选项
     */
    private List<Option> option;
    /**
     * 我的答案
     */
    private String myOption;
    /**
     * 正确答案
     */
    private String rightOption;
    /**
     * 是否正确
     */
    private Integer isRight;
    /**
     * 试题分析
     */
    private String analyse;
    /**
     * 试题类型
     */
    private Integer quType;
    /**
     * 题目设置分数
     */
    private Integer score;
    /**
     * 实际得分
     */
    private Integer userScore;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<Option> getOption() { return option; }
    public void setOption(List<Option> option) { this.option = option; }
    public String getMyOption() { return myOption; }
    public void setMyOption(String myOption) { this.myOption = myOption; }
    public String getRightOption() { return rightOption; }
    public void setRightOption(String rightOption) { this.rightOption = rightOption; }
    public Integer getIsRight() { return isRight; }
    public void setIsRight(Integer isRight) { this.isRight = isRight; }
    public String getAnalyse() { return analyse; }
    public void setAnalyse(String analyse) { this.analyse = analyse; }
    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getUserScore() { return userScore; }
    public void setUserScore(Integer userScore) { this.userScore = userScore; }
}
