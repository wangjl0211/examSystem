package cn.org.wang.exam.model.form.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import cn.org.wang.exam.common.group.QuestionGroup;
import cn.org.wang.exam.model.entity.Option;

/**
 * 试卷请求体
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/1 11:12
 */
@Data
public class QuestionFrom {

    private Integer id;

    /**
     * 试题类型
     */
    @NotNull(message = "试题类型(quType)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    @Min(value = 1, message = "试题类型(quType)只能是：1单选2多选3判断4简答", groups = QuestionGroup.QuestionAddGroup.class)
    @Max(value = 4, message = "试题类型(quType)只能是：1单选2多选3判断4简答", groups = QuestionGroup.QuestionAddGroup.class)
    private Integer quType;

    /**
     * 试题图片
     */
    private String image;
    private String analysis;

    /**
     * 题干
     */
    @NotBlank(message = "题干(content)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 题库ID
     */
    @NotNull(message = "题库id(repoId)不能为空", groups = QuestionGroup.QuestionAddGroup.class)
    private Integer repoId;

    /**
     * 选项列表
     */
    private List<Option> options;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getRepoId() { return repoId; }
    public void setRepoId(Integer repoId) { this.repoId = repoId; }
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
}

