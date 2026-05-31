package cn.org.wang.exam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统滑块验证码配置类
 *
 * @Author Wang
 * @Version
 * @Date 2026/4/12 1:33 PM
 */
@Configuration
@ConfigurationProperties(prefix = "system.slider-captcha")
public class SystemSliderCaptchaConfig {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
