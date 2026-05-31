package cn.org.wang.exam.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.User;
import cn.org.wang.exam.config.SecurityPublicPathProperties;
import cn.org.wang.exam.utils.JwtUtil;
import cn.org.wang.exam.utils.ResponseUtil;
import cn.org.wang.exam.utils.security.SysUserDetails;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token 校验过滤器
 * 纯 JWT 无状态认证，Token 黑名单存储在 Redis 中用于注销管理
 *
 * @author Wang
 * @Version 2.0
 * @Date 2026/3/25 19:50
 */
@Slf4j
@Component
public class VerifyTokenFilter extends OncePerRequestFilter {

    /**
     * Token 黑名单 Redis 键前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * JWT 工具类
     */
    @Resource
    private JwtUtil jwtUtil;

    /**
     * Redis 服务
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ResponseUtil responseUtil;

    @Resource
    private SecurityPublicPathProperties publicPathProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (publicPathProperties.isPublicPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 Token
        String token = request.getHeader("Authorization");

        // 判断是否为空
        if (StringUtils.isBlank(token)) {
            responseUtil.response(response, Result.failed("Authorization为空，请先登录"), 401);
            return;
        }

        // 去除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 检查 Token 是否在黑名单中（已注销）
        if (isTokenBlacklisted(token)) {
            responseUtil.response(response, Result.failed("token已失效，请重新登录"), 401);
            return;
        }

        // 验证并尝试续签 Token
        String refreshedToken = jwtUtil.verifyAndRefreshToken(token);
        if (refreshedToken == null) {
            responseUtil.response(response, Result.failed("token无效或已过期，请重新登录"), 401);
            return;
        }

        // 如果 Token 已续签，设置到响应头
        if (!refreshedToken.equals(token)) {
            response.setHeader("Authorization", "Bearer " + refreshedToken);
        }

        try {
            // 从续签后的 Token 中获取用户信息和权限
            String userInfo = jwtUtil.getUser(refreshedToken);
            List<String> authList = jwtUtil.getAuthList(refreshedToken);

            log.debug("请求路径: {}, 用户权限列表: {}", requestUri, authList);

            // 反序列化 jwtToken 获取用户信息
            User sysUser = objectMapper.readValue(userInfo, User.class);

            // 权限转型
            List<SimpleGrantedAuthority> permissions = authList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            // 创建登录用户
            SysUserDetails securityUser = new SysUserDetails(sysUser);
            securityUser.setPermissions(permissions);

            // 创建权限授权的 token 参数：用户，密码，权限 不给密码因为已经登录了
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(securityUser, null, permissions);

            // 通过安全上下文设置授权 token
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Token 解析失败", e);
            responseUtil.response(response, Result.failed("token解析失败，请重新登录"), 401);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return 是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey));
    }

    /**
     * 将 Token 加入黑名单（用于注销）
     *
     * @param token JWT Token
     * @param expirationSeconds 过期时间（秒）
     */
    public void blacklistToken(String token, long expirationSeconds) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        stringRedisTemplate.opsForValue().set(blacklistKey, "1", expirationSeconds, TimeUnit.SECONDS);
        log.debug("Token 已加入黑名单，过期时间: {}秒", expirationSeconds);
    }
}
