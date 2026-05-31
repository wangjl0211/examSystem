package cn.org.wang.exam.model.vo.exam;

import lombok.Data;

import java.util.Collections;
import java.util.List;

import cn.org.wang.exam.model.entity.Option;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/10 9:54 AM
 */
@Data
public class ExamQuCollectVO {
    /**
     * 题目ID
     */
    private Integer id;
    /**
     * 图片
     */
    private String image;
    /**
     * 题干
     */
    private String title;
    /**
     * 选项
     */
    private List<Option> option;
    /**
     * 我的答案
     */
    private String myOption;

    /**
     * 试题类型
     */
    private Integer quType;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<Option> getOption() { return option == null ? Collections.emptyList() : List.copyOf(option); }
    public void setOption(List<Option> option) { this.option = option; }
    public String getMyOption() { return myOption; }
    public void setMyOption(String myOption) { this.myOption = myOption; }
    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
}
