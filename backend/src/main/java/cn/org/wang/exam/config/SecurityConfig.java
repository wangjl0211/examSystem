package cn.org.wang.exam.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.filter.VerifyTokenFilter;
import cn.org.wang.exam.utils.ResponseUtil;

import java.util.List;

/**
 * Spring Security 权限配置类
 * 采用无状态 JWT 认证，禁用 Session
 *
 * @Author Wang
 * @since 2026/4/17
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Resource
    private ResponseUtil responseUtil;

    @Resource
    private VerifyTokenFilter verifyTokenFilter;

    @Resource
    private SecurityPublicPathProperties publicPathProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        List<String> publicPaths = publicPathProperties.getPaths();
        http
            // 禁用 CSRF（纯 JWT 无状态认证不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 会话管理：无状态模式
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> {
                for (String path : publicPaths) {
                    auth.requestMatchers(path).permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    responseUtil.response(response, Result.failed("你没有该资源的访问权限"))
                )
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(verifyTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
