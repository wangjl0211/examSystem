package cn.org.wang.exam.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/31 13:14
 */
@Data
public class UserVO {

    private Integer id;
    // 学号/工号
    private String userNo;
    // 真实姓名
    private String realName;
    // 头像
    private String avatar;
    // 用户角色
    private Integer roleId;
    // 注册时间
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUserNo() { return userNo; }
    public void setUserNo(String userNo) { this.userNo = userNo; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
