package cn.org.wang.exam.model.form.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

import cn.org.wang.exam.common.group.UserGroup;
import cn.org.wang.exam.utils.excel.ExcelImport;


/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/29 15:17
 */
@Data
public class UserForm {
    // 用户ID
    private Integer id;

    // 创建试卷
    private LocalDateTime createTime;

    // 真实姓名
    @NotBlank(groups = {UserGroup.CreateUserGroup.class, UserGroup.RegisterGroup.class}, message = "真实姓名不能为空")
    @ExcelImport(value = "真实姓名*")
    private String realName;

    // 密码
    @NotBlank(groups = UserGroup.RegisterGroup.class,message = "密码不能为空")
    private String password;

    // 校验密码
    @NotBlank(groups = {UserGroup.UpdatePasswordGroup.class, UserGroup.RegisterGroup.class}, message = "校验密码不能为空")
    private String checkedPassword;

    // 角色ID
    @ExcelImport(value = "角色")
    private Integer roleId;

    // 课程ID
    private Integer subjectId;

    // 旧密码
    @NotBlank(groups = {UserGroup.UpdatePasswordGroup.class}, message = "原密码不能为空")
    private String originPassword;

    // 新密码
    @NotBlank(groups = {UserGroup.UpdatePasswordGroup.class}, message = "新密码不能为空")
    private String newPassword;

    // 身份类型 (student/teacher)
    @NotBlank(groups = UserGroup.RegisterGroup.class, message = "身份类型不能为空")
    private String identity;

    // 教师资格证件编号
    @Pattern(groups = UserGroup.RegisterGroup.class, regexp = "\\d{17}", message = "教师资格证编号必须是17位数字")
    private String teacherCertNo;

    // 邮箱
    @NotBlank(groups = UserGroup.RegisterGroup.class, message = "邮箱不能为空")
    @Pattern(groups = UserGroup.RegisterGroup.class, regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+", message = "邮箱格式不正确")
    private String mail;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCheckedPassword() { return checkedPassword; }
    public void setCheckedPassword(String checkedPassword) { this.checkedPassword = checkedPassword; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getsubjectId() { return subjectId; }
    public void setsubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getOriginPassword() { return originPassword; }
    public void setOriginPassword(String originPassword) { this.originPassword = originPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }
    public String getTeacherCertNo() { return teacherCertNo; }
    public void setTeacherCertNo(String teacherCertNo) { this.teacherCertNo = teacherCertNo; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
}

