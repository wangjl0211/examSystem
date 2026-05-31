package cn.org.wang.exam.model.vo.exercise;

import lombok.Data;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/6 11:08
 */
@Data
public class ExerciseRepoVO {
    private Integer id;
    // 题库标题
    private String repoTitle;
    // 总题数
    private Integer totalCount;
    private Integer exerciseCount;
    // 分类相关字段
    private Integer categoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRepoTitle() { return repoTitle; }
    public void setRepoTitle(String repoTitle) { this.repoTitle = repoTitle; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getExerciseCount() { return exerciseCount; }
    public void setExerciseCount(Integer exerciseCount) { this.exerciseCount = exerciseCount; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }
}
