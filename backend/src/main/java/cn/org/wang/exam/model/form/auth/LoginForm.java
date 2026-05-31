package cn.org.wang.exam.model.form.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录表单
 *
 * @Author Wang
 * @Version
 * @Date 2026/5/6 10:07 PM
 */
@Data
public class LoginForm {

    // 学号/工号
    @NotBlank(message = "学号/工号不能为空")
    private String userNo;

    // 密码
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUserNo() { return userNo; }
    public void setUserNo(String userNo) { this.userNo = userNo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

