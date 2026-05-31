package cn.org.wang.exam.model.form.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 忘记密码请求表单
 * 
 * @Author Wang
 * @Version 1.0
 * @Date 2026-02-24
 */
@Data
public class ForgotPasswordForm {

    // 学号/工号
    @NotBlank(message = "学号/工号不能为空")
    private String userNo;

    // 邮箱
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+", message = "邮箱格式不正确")
    private String mail;

    // 验证码
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String verificationCode;

    // 新密码
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    // 确认新密码
    @NotBlank(message = "确认新密码不能为空")
    private String confirmPassword;

    /**
     * 验证新密码和确认密码是否一致
     * 
     * @return 是否一致
     */
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
