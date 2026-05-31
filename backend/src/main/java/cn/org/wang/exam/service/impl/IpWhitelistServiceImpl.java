package cn.org.wang.exam.service.impl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.IpWhitelistMapper;
import cn.org.wang.exam.model.entity.IpWhitelist;
import cn.org.wang.exam.service.IIpWhitelistService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * IP白名单服务实现类
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
@Service
@Slf4j
public class IpWhitelistServiceImpl extends ServiceImpl<IpWhitelistMapper, IpWhitelist> implements IIpWhitelistService {

    /** Redis缓存键前缀 */
    private static final String CACHE_KEY_PREFIX = "ip:whitelist:";

    /** 缓存过期时间（分钟） */
    private static final long CACHE_EXPIRE_MINUTES = 30;

    private final StringRedisTemplate stringRedisTemplate;
    private final IpWhitelistMapper ipWhitelistMapper;

    public IpWhitelistServiceImpl(StringRedisTemplate stringRedisTemplate, IpWhitelistMapper ipWhitelistMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.ipWhitelistMapper = ipWhitelistMapper;
    }

    /**
     * 校验IP是否在白名单中
     *
     * @param clientIp 客户端IP地址
     * @return true-在白名单中，false-不在白名单中
     */
    @Override
    public boolean isIpAllowed(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            log.warn("客户端IP为空，拒绝访问");
            return false;
        }

        // 处理本地回环地址
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }

        // 获取所有启用的白名单规则
        List<IpWhitelist> rules = getAllEnabledRules();

        for (IpWhitelist rule : rules) {
            if (matchIp(clientIp, rule.getIpAddress(), rule.getIpType())) {
                log.info("IP {} 匹配白名单规则: {}", clientIp, rule.getIpAddress());
                return true;
            }
        }

        log.warn("IP {} 不在白名单中，拒绝访问", clientIp);
        return false;
    }

    /**
     * 获取所有启用的IP白名单规则（优先从缓存读取）
     *
     * @return IP白名单列表
     */
    @Override
    public List<IpWhitelist> getAllEnabledRules() {
        String cacheKey = CACHE_KEY_PREFIX + "all";

        // 尝试从缓存获取
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                log.debug("从缓存获取IP白名单规则");
                // 简单处理：如果缓存存在，直接查询数据库（实际生产环境可使用JSON序列化）
            }
        } catch (Exception e) {
            log.warn("从缓存获取IP白名单失败: {}", e.getMessage());
        }

        // 从数据库查询
        LambdaQueryWrapper<IpWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpWhitelist::getStatus, 1)
               .eq(IpWhitelist::getIsDeleted, 0);
        List<IpWhitelist> rules = ipWhitelistMapper.selectList(wrapper);

        // 更新缓存
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, "cached", CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("更新IP白名单缓存失败: {}", e.getMessage());
        }

        return rules;
    }

    /**
     * 添加IP白名单规则
     *
     * @param ipWhitelist IP白名单实体
     * @return 操作结果
     */
    @Override
    public Result<String> addRule(IpWhitelist ipWhitelist) {
        // 校验IP格式
        if (!validateIpFormat(ipWhitelist.getIpAddress(), ipWhitelist.getIpType())) {
            return Result.failed("IP地址格式不正确");
        }

        // 检查是否已存在
        LambdaQueryWrapper<IpWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpWhitelist::getIpAddress, ipWhitelist.getIpAddress())
               .eq(IpWhitelist::getIsDeleted, 0);
        if (ipWhitelistMapper.selectCount(wrapper) > 0) {
            return Result.failed("该IP规则已存在");
        }

        ipWhitelist.setCreateTime(LocalDateTime.now());
        ipWhitelist.setStatus(1);
        ipWhitelist.setIsDeleted(0);
        ipWhitelistMapper.insert(ipWhitelist);

        // 刷新缓存
        refreshCache();

        log.info("添加IP白名单规则成功: {}", ipWhitelist.getIpAddress());
        return Result.success("添加成功");
    }

    /**
     * 更新IP白名单规则
     *
     * @param ipWhitelist IP白名单实体
     * @return 操作结果
     */
    @Override
    public Result<String> updateRule(IpWhitelist ipWhitelist) {
        if (ipWhitelist.getId() == null) {
            return Result.failed("规则ID不能为空");
        }

        // 校验IP格式
        if (!validateIpFormat(ipWhitelist.getIpAddress(), ipWhitelist.getIpType())) {
            return Result.failed("IP地址格式不正确");
        }

        ipWhitelist.setUpdateTime(LocalDateTime.now());
        ipWhitelistMapper.updateById(ipWhitelist);

        // 刷新缓存
        refreshCache();

        log.info("更新IP白名单规则成功: {}", ipWhitelist.getIpAddress());
        return Result.success("更新成功");
    }

    /**
     * 删除IP白名单规则（逻辑删除）
     *
     * @param id 规则ID
     * @return 操作结果
     */
    @Override
    public Result<String> deleteRule(Integer id) {
        IpWhitelist ipWhitelist = ipWhitelistMapper.selectById(id);
        if (ipWhitelist == null) {
            return Result.failed("规则不存在");
        }

        ipWhitelist.setIsDeleted(1);
        ipWhitelist.setUpdateTime(LocalDateTime.now());
        ipWhitelistMapper.updateById(ipWhitelist);

        // 刷新缓存
        refreshCache();

        log.info("删除IP白名单规则成功: {}", ipWhitelist.getIpAddress());
        return Result.success("删除成功");
    }

    /**
     * 启用/禁用IP白名单规则
     *
     * @param id     规则ID
     * @param status 状态：1启用 0禁用
     * @return 操作结果
     */
    @Override
    public Result<String> toggleRule(Integer id, Integer status) {
        IpWhitelist ipWhitelist = ipWhitelistMapper.selectById(id);
        if (ipWhitelist == null) {
            return Result.failed("规则不存在");
        }

        ipWhitelist.setStatus(status);
        ipWhitelist.setUpdateTime(LocalDateTime.now());
        ipWhitelistMapper.updateById(ipWhitelist);

        // 刷新缓存
        refreshCache();

        log.info("更新IP白名单规则状态成功: {} -> {}", ipWhitelist.getIpAddress(), status == 1 ? "启用" : "禁用");
        return Result.success("操作成功");
    }

    /**
     * 刷新IP白名单缓存
     */
    @Override
    public void refreshCache() {
        try {
            String cacheKey = CACHE_KEY_PREFIX + "all";
            stringRedisTemplate.delete(cacheKey);
            log.info("IP白名单缓存已刷新");
        } catch (Exception e) {
            log.warn("刷新IP白名单缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 校验IP地址格式
     *
     * @param ipAddress IP地址
     * @param ipType    类型：1单个IP 2网段 3CIDR
     * @return true-格式正确，false-格式错误
     */
    private boolean validateIpFormat(String ipAddress, Integer ipType) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        switch (ipType) {
            case 1: // 单个IP
                return ipAddress.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
            case 3: // CIDR格式
                return ipAddress.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)/(\\d|[12]\\d|3[0-2])$");
            case 2: // 网段（使用CIDR格式）
                return ipAddress.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)/(\\d|[12]\\d|3[0-2])$");
            default:
                return false;
        }
    }

    /**
     * 匹配IP地址
     *
     * @param clientIp  客户端IP
     * @param ruleIp    规则IP
     * @param ruleType  规则类型
     * @return true-匹配，false-不匹配
     */
    private boolean matchIp(String clientIp, String ruleIp, Integer ruleType) {
        switch (ruleType) {
            case 1: // 单个IP
                return clientIp.equals(ruleIp);
            case 2: // 网段
            case 3: // CIDR格式
                return matchCidr(clientIp, ruleIp);
            default:
                return false;
        }
    }

    /**
     * CIDR格式匹配
     *
     * @param clientIp 客户端IP
     * @param cidr     CIDR格式地址（如192.168.1.0/24）
     * @return true-匹配，false-不匹配
     */
    private boolean matchCidr(String clientIp, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            // 将IP地址转换为整数
            long clientIpLong = ipToLong(clientIp);
            long networkIpLong = ipToLong(networkIp);

            // 计算掩码
            long mask = (1L << (32 - prefixLength)) - 1;
            mask = ~mask;

            // 比较网络地址
            return (clientIpLong & mask) == (networkIpLong & mask);
        } catch (Exception e) {
            log.error("CIDR匹配异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将IP地址转换为长整数
     *
     * @param ip IP地址
     * @return 长整数
     */
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = result * 256 + Long.parseLong(parts[i]);
        }
        return result;
    }
}
