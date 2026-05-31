package cn.org.wang.exam.filter;

import cn.org.wang.exam.service.IIpWhitelistService;
import cn.org.wang.exam.utils.IPUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * IP白名单拦截器
 * 用于校验管理员接口的IP访问权限
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
@Slf4j
@Component
public class IpWhitelistInterceptor implements HandlerInterceptor {

    private final IIpWhitelistService ipWhitelistService;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     * @SuppressFBWarnings("EI_EXPOSE_REP2") - Spring依赖注入模式，依赖bean由Spring容器管理，不存在外部修改风险
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public IpWhitelistInterceptor(IIpWhitelistService ipWhitelistService, ObjectMapper objectMapper) {
        this.ipWhitelistService = ipWhitelistService;
        this.objectMapper = objectMapper;
    }

    /**
     * 前置拦截：校验IP是否在白名单中
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @return true-放行，false-拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取客户端真实IP
        String clientIp = IPUtils.getIPAddress(request);
        log.info("管理员接口访问校验 - 客户端IP: {}", clientIp);

        // 校验IP是否在白名单中
        if (!ipWhitelistService.isIpAllowed(clientIp)) {
            log.warn("IP {} 不在白名单中，拒绝访问管理员接口", clientIp);

            // 返回403 Forbidden响应
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 403);
            errorResponse.put("message", "Access denied: IP not in whitelist");
            errorResponse.put("timestamp", System.currentTimeMillis());

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return false;
        }

        log.info("IP {} 校验通过，允许访问管理员接口", clientIp);
        return true;
    }
}