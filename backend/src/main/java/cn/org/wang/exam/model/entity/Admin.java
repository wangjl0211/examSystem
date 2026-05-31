package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员实体类
 *
 * @author Wang
 * @Version
 * @Date 2026/2/1 10:00 AM
 */
@Data
@Schema(description = "管理员实体类")
@TableName("t_admin")
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "管理员ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "管理员登录名")
    private String adminName;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "角色标识")
    private Integer roleId;

    @Schema(description = "头像路径")
    private String avatar;

    @Schema(description = "邮箱")
    private String mail;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

}
