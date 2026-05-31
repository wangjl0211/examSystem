package cn.org.wang.exam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 匿名访问路径统一配置（Security 与 Token 过滤器共用）
 */
@Component
@ConfigurationProperties(prefix = "security.public")
public class SecurityPublicPathProperties {

    /**
     * 无需 Token 的路径前缀列表
     */
    private List<String> paths = defaultPaths();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths != null ? paths : defaultPaths();
    }

    /**
     * 判断请求 URI 是否匹配任一公开路径前缀
     */
    public boolean isPublicPath(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        for (String path : paths) {
            String prefix = path.endsWith("**") ? path.substring(0, path.length() - 2) : path;
            if (requestUri.startsWith(prefix) || requestUri.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> defaultPaths() {
        List<String> list = new ArrayList<>();
        list.add("/api/auths/register");
        list.add("/api/auths/captcha/slide/create");
        list.add("/api/auths/captcha/slide/verify");
        list.add("/api/auths/forgot-password/**");
        list.add("/auths/register");
        list.add("/auths/captcha/slide/create");
        list.add("/auths/captcha/slide/verify");
        list.add("/auths/forgot-password/**");
        list.add("/api/user/login");
        list.add("/user/login");
        list.add("/api/admin/login");
        list.add("/admin/login");
        list.add("/webjars/**");
        list.add("/swagger-ui/**");
        list.add("/swagger-ui.html");
        list.add("/v3/api-docs/**");
        list.add("/doc.html");
        list.add("/ws/**");
        list.add("/ws-app/**");
        list.add("/api/exams/server-time");
        return list;
    }
}
