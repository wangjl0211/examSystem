package cn.org.wang.exam.model.form.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送验证码请求表单
 * 
 * @Author Wang
 * @Version 1.0
 * @Date 2026-02-24
 */
@Data
public class SendVerificationCodeForm {

    // 学号/工号
    @NotBlank(message = "学号/工号不能为空")
    private String userNo;

    // 邮箱
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+", message = "邮箱格式不正确")
    private String mail;

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
}
