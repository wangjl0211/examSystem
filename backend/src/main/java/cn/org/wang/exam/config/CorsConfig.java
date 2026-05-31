package cn.org.wang.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 资源共享配置
 * 修复P0安全漏洞：使用白名单域名替代通配符
 *
 * @Author Wang
 * @since 2026/4/17
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的跨域来源列表，可通过配置文件覆盖
     */
    @Value("${security.cors.allowed-origins:http://localhost:9527,http://localhost:9528}")
    private List<String> allowedOrigins;

    /**
     * 配置允许跨域
     * 使用白名单域名，确保安全性
     *
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 使用配置化的允许来源，而非通配符
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setMaxAge(3600L); // 预检请求缓存1小时
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configurationSource);
    }
}