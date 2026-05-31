package cn.org.wang.exam.config;

import cn.org.wang.exam.filter.IpWhitelistInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于注册拦截器等配置
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private IpWhitelistInterceptor ipWhitelistInterceptor;

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册IP白名单拦截器，拦截管理员接口（排除管理员登录接口）
        registry.addInterceptor(ipWhitelistInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                        "/api/admin/login",  // 排除管理员登录接口，登录接口单独处理IP校验
                        "/api/admin/whitelist/**"  // 排除白名单管理接口
                )
                .order(1); // 优先级设为1，确保在其他拦截器之前执行

        // 注意：管理员登录接口的IP校验在控制器中单独处理
    }
}
