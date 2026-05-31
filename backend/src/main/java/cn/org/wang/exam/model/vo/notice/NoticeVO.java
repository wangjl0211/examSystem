package cn.org.wang.exam.model.vo.notice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Wang
 * @Version
 * @Date 2026/3/28 11:26 PM
 */
@Data
public class NoticeVO {
    private Integer id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 图片地址
     */
    private String image;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 创建用户ID   唯一
     */
    private Integer userId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 创建人名字
     */
    private String realName;

    /**
     * 是否公开 教师：所有课程用户 管理员：所有用户
     */
    private Integer isPublic;

    /**
     * 课程id列表
     */
    private List<Integer> subjectIds;

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
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }
    public List<Integer> getsubjectIds() { return subjectIds; }
    public void setsubjectIds(List<Integer> subjectIds) { this.subjectIds = subjectIds; }
}
