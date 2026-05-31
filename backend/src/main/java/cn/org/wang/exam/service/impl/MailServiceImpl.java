package cn.org.wang.exam.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 邮件发送服务实现类
 * 
 * @Author Wang
 * @Version 1.0
 * @Date 2026-02-24
 */
@Service
@Slf4j
public class MailServiceImpl {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.send-nickname:考试系统}")
    private String senderNickname;


    /**
     * 构造器注入
     * 
     * @param javaMailSender JavaMailSender实例
     */
    @Autowired
    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * 发送验证码邮件
     * 
     * @param toEmail 收件人邮箱
     * @param verificationCode 验证码
     * @param expireMinutes 验证码过期时间（分钟）
     * @throws ServiceRuntimeException 邮件发送异常
     */
    public void sendVerificationCode(String toEmail, String verificationCode, int expireMinutes) {
        try {
            // 创建MimeMessage
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置发件人（使用别名，同时保留真实邮箱用于认证）
            InternetAddress fromAddress = new InternetAddress(fromEmail, senderNickname, "UTF-8");
            helper.setFrom(fromAddress);
            
            // 设置收件人
            helper.setTo(toEmail);
            
            // 设置邮件主题
            helper.setSubject("考试系统 - 密码重置验证码");
            
            // 设置邮件内容
            String content = buildVerificationCodeContent(verificationCode, expireMinutes);
            helper.setText(content);
            
            // 发送邮件
            javaMailSender.send(message);
            
            log.info("验证码邮件发送成功，收件人: {}, 验证码: {}, 过期时间: {}分钟", 
                     toEmail, verificationCode, expireMinutes);
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人: {}, 错误信息: {}", toEmail, e.getMessage());
            throw new ServiceRuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建验证码邮件内容
     * 
     * @param verificationCode 验证码
     * @param expireMinutes 过期时间（分钟）
     * @return 邮件内容
     */
    private String buildVerificationCodeContent(String verificationCode, int expireMinutes) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        StringBuilder content = new StringBuilder();
        content.append("尊敬的用户：\n\n")
               .append("您正在申请重置考试系统的账号密码，以下是您的验证码：\n\n")
               .append("【")
               .append(verificationCode)
               .append("】\n\n")
               .append("验证码有效期为 ")
               .append(expireMinutes)
               .append(" 分钟，请在有效期内使用。\n\n")
               .append("如果您没有发起此操作，请忽略此邮件。\n\n")
               .append("发送时间：")
               .append(currentTime)
               .append("\n")
               .append("在线考试系统为您服务");
        
        return content.toString();
    }

    /**
     * 发送通用邮件
     * 
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @throws ServiceRuntimeException 邮件发送异常
     */
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            // 创建MimeMessage
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置发件人（使用别名，同时保留真实邮箱用于认证）
            InternetAddress fromAddress = new InternetAddress(fromEmail, senderNickname, "UTF-8");
            helper.setFrom(fromAddress);
            
            // 设置收件人
            helper.setTo(toEmail);
            
            // 设置邮件主题
            helper.setSubject(subject);
            
            // 设置邮件内容
            helper.setText(content);
            
            // 发送邮件
            javaMailSender.send(message);
            
            log.info("邮件发送成功，收件人: {}, 主题: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("邮件发送失败，收件人: {}, 主题: {}, 错误信息: {}", toEmail, subject, e.getMessage());
            throw new ServiceRuntimeException("邮件发送失败，请稍后重试");
        }
    }
}
