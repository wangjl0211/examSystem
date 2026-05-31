package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import cn.org.wang.exam.common.base.BaseSerializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="用户实体类")
@TableName("t_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="用户编号(学号/工号)")
    private String userNo;

    @Schema(description ="真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description ="密码")
    @TableField(fill = FieldFill.INSERT)
    private String password;

    @Schema(description ="头像地址")
    private String avatar;

    @Schema(description ="角色ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer roleId;

    @Schema(description ="教师资格证件编号")
    private String teacherCertNo;

    @Schema(description ="邮箱")
    private String mail;

    /**
     * YYYY-MM-DD hh:mm:ss
     */
    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 1正常0禁用
     */
    @Schema(description ="状态")
    private Integer status;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUserNo() { return userNo; }
    public void setUserNo(String userNo) { this.userNo = userNo; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public String getTeacherCertNo() { return teacherCertNo; }
    public void setTeacherCertNo(String teacherCertNo) { this.teacherCertNo = teacherCertNo; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

