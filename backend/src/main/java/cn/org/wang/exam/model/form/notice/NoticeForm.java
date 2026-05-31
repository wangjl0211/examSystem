package cn.org.wang.exam.model.form.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/3/28 10:44 PM
 */
@Data
public class NoticeForm {

    /**
     * ID
     */
    private Integer id;

    /**
     * 公告标题
     */
    @NotBlank
    private String title;

    /**
     * 图片地址
     */
    private String image;

    /**
     * 公告内容
     */
    @NotBlank
    private String content;

    /**
     * 创建用户ID
     */
    private Integer userId;

    /**
     * 公告课程
     */
    private String subjectIds;

    /**
     * 是否公开 教师：所有课程用户 管理员：所有用户
     */
    private Integer isPublic;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getsubjectIds() { return subjectIds; }
    public void setsubjectIds(String subjectIds) { this.subjectIds = subjectIds; }
    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }
}

