package cn.org.wang.exam.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lionsoul.ip2region.xdb.Searcher;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * IP地址工具类
 * 用于获取客户端真实IP地址和查询IP归属地
 *
 * @author Wang
 * @since 2026-03-21
 */
public class IPUtils {

    /**
     * 私有构造器，防止实例化工具类
     */
    private IPUtils() {
    }

    /**
     * 日志记录器
     */
    private static final Logger log = LogManager.getLogger(IPUtils.class);

    /**
     * 未知IP标识
     */
    private static final String UNKNOWN = "unknown";

    /**
     * IP地址查询器
     */
    private static Searcher searcher;

    /**
     * 静态初始化块，加载IP数据库
     */
    static {
        try {
            // 从classpath加载资源文件为输入流
            try (InputStream inputStream = IPUtils.class.getClassLoader()
                    .getResourceAsStream("ipdata/ip2region.xdb")) {
                
                if (inputStream == null) {
                    throw new ServiceRuntimeException("IP数据库文件不存在");
                }
                
                // 读取输入流为字节数组
                byte[] bytes = inputStream.readAllBytes();
                
                // 使用缓存方式创建Searcher，提高查询性能
                searcher = Searcher.newWithBuffer(bytes);
                
                log.info("IP 归属地查询初始化成功");
            }
        } catch (Exception e) {
            log.error("初始化 IP 归属地查询失败: {}", e.getMessage());
            throw new ServiceRuntimeException("初始化IP数据库失败");
        }
    }

    /**
     * 根据HTTP请求获取IP归属地
     *
     * @param request HTTP请求对象
     * @return IP归属地信息
     */
    public static String getIPRegion(HttpServletRequest request) {
        String ip = getIPAddress(request);
        return getIPRegion(ip);
    }

    /**
     * 根据IP地址获取归属地信息
     *
     * @param ip IP地址
     * @return 归属地信息
     */
    public static String getIPRegion(String ip) {
        // 如果是本地IP，直接返回"本地"
        if (isLocalIP(ip)) {
            return "本地";
        }
        
        // 检查Searcher是否初始化
        if (searcher == null) {
            log.error("IP 归属地查询失败，Searcher未初始化");
            return "未知";
        }
        
        try {
            // 记录查询开始时间
            long startTime = System.nanoTime();
            // 查询IP归属地
            String region = searcher.search(ip);
            // 计算查询耗时
            long cost = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);
            log.info("IP: {}, Region: {}, Took: {} μs", ip, region, cost);
            // 返回归属地信息
            return region != null ? region : "未知";
        } catch (Exception e) {
            log.error("IP: {} 获取 IP 归属地错误，错误原因: {}", ip, e.getMessage());
            return "未知";
        }
    }

    /**
     * 从HTTP请求中获取客户端真实IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getIPAddress(HttpServletRequest request) {
        String ipAddress = getIpFromHeaders(request);
        return processMultiProxy(ipAddress);
    }

    /**
     * 从HTTP请求头中获取IP地址
     * 按优先级检查多个可能的代理请求头
     *
     * @param request HTTP请求对象
     * @return IP地址
     */
    private static String getIpFromHeaders(HttpServletRequest request) {
        // 检查X-Forwarded-For头（最常见的代理头）
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (isValidIp(ipAddress)) {
            return ipAddress;
        }
        
        // 检查Proxy-Client-IP头
        ipAddress = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ipAddress)) {
            return ipAddress;
        }
        
        // 检查WL-Proxy-Client-IP头
        ipAddress = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ipAddress)) {
            return ipAddress;
        }
        
        // 检查HTTP_CLIENT_IP头
        ipAddress = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ipAddress)) {
            return ipAddress;
        }
        
        // 检查HTTP_X_FORWARDED_FOR头
        ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ipAddress)) {
            return ipAddress;
        }
        
        // 如果以上头都没有，直接返回远程地址
        return request.getRemoteAddr();
    }

    /**
     * 处理多层代理的情况
     * X-Forwarded-For格式通常为：client_ip, proxy1_ip, proxy2_ip
     * 需要提取第一个非unknown的IP
     *
     * @param ipAddress IP地址字符串
     * @return 处理后的IP地址
     */
    private static String processMultiProxy(String ipAddress) {
        if (ipAddress != null && ipAddress.contains(",")) {
            String[] ips = ipAddress.split(",");
            for (String ip : ips) {
                ip = ip.trim();
                if (!UNKNOWN.equalsIgnoreCase(ip)) {
                    return ip;
                }
            }
        }
        return ipAddress;
    }

    /**
     * 检查IP地址是否有效
     *
     * @param ipAddress IP地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ipAddress) {
        return ipAddress != null && !ipAddress.isEmpty() && !UNKNOWN.equalsIgnoreCase(ipAddress);
    }

    /**
     * 判断是否为本地IP地址
     *
     * @param ip IP地址
     * @return 是否为本地IP
     */
	private static boolean isLocalIP(String ip) {
			return ip.startsWith("127.") || ip.startsWith("192.168.") || 
				   ip.startsWith("10.") || (ip.startsWith("172.") && ip.length() >= 6 && Integer.parseInt(ip.substring(4, 6)) >= 16 && Integer.parseInt(ip.substring(4, 6)) <= 31) || 
				   ip.equals("0:0:0:0:0:0:0:1") || ip.equals("localhost");
		}

    /**
     * 关闭Searcher
     */
    public static void closeSearcher() {
        try {
            if (Objects.nonNull(searcher)) {
                searcher.close();
                searcher = null;
                log.info("IP 归属地查询关闭成功");
            }
        } catch (Exception e) {
            log.error("关闭IP归属地查询异常", e);
        }
    }
}