package cn.org.wang.exam.model.vo.repo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/28 19:57
 */
@Data
public class RepoVO {
    // 题库ID
    private Integer id;
    // 用户ID
    private Integer userId;
    // 题库标题
    private String title;
    // 是否可以刷题
    private Integer isExercise;
    // 创建试卷
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 创建人真实姓名
    private String realName;
    // 分类ID
    private Integer categoryId;
    // 分类名称
    private String categoryName;
    // 题目数量
    private Integer questionCount;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getIsExercise() { return isExercise; }
    public void setIsExercise(Integer isExercise) { this.isExercise = isExercise; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
}
