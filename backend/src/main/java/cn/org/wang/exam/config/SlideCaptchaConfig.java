package cn.org.wang.exam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 滑块验证码配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "captcha.slide")
public class SlideCaptchaConfig {
    /**
     * 图片资源目录
     */
    private String imagePath = "classpath:images/captcha/";
    
    /**
     * 容错像素（默认5px）
     */
    private int tolerance = 5;
    
    /**
     * 验证码有效期（秒）
     */
    private long expireTime = 300;
    
    /**
     * 是否开启验证码
     */
    private boolean enabled = true;

    /**
     * 限流阈值（每分钟）
     */
    private int rateLimit = 60;

    /**
     * 失败锁定阈值
     */
    private int maxFailures = 5;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(int rateLimit) {
        this.rateLimit = rateLimit;
    }

    public int getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
        this.maxFailures = maxFailures;
    }
}
