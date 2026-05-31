package cn.org.wang.exam.model.vo.exercise;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.org.wang.exam.model.entity.Option;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/6/6 18:13
 */
@Data
public class AnswerInfoVO {
    private Integer id;
    private String content;
    private Integer repoId;
    private  String image;
    private String repoTitle;
    private Integer quType;
    private String analysis;
    private String answerContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    private List<Option> options;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getRepoTitle() { return repoTitle; }
    public void setRepoTitle(String repoTitle) { this.repoTitle = repoTitle; }
    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
}
