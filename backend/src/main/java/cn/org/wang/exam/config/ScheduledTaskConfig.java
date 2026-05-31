package cn.org.wang.exam.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cn.org.wang.exam.mapper.LogMapper;
import cn.org.wang.exam.model.entity.Log;
import cn.org.wang.exam.service.impl.StatServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * 定时任务配置类
 * 用于定期执行数据验证任务
 *
 * @author Wang
 * @since 2026-03-21
 */
@Configuration
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
public class ScheduledTaskConfig {

    private static final Logger logger = Logger.getLogger(ScheduledTaskConfig.class.getName());

    @Resource
    private StatServiceImpl statService;

    @Resource
    private LogMapper logMapper;

    /**
     * 每小时执行一次数据验证任务
     * 核对统计数据与实际登录日志的一致性
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时整点执行
    public void validateOnlineUserCount() {
        logger.info("开始执行在线人数统计数据验证任务...");

        try {
            // 1. 获取当前统计的在线人数
            int currentOnlineCount = statService.getCurrentOnlineUserCount();

            // 2. 基于登录日志重新计算在线人数（作为基准值）
            int baselineCount = calculateBaselineOnlineCount();

            // 3. 比较两个值的差异
            int difference = Math.abs(currentOnlineCount - baselineCount);
            double errorRate = baselineCount > 0 ? (double) difference / baselineCount * 100 : 0;

            // 4. 记录验证结果
            String validationResult = String.format(
                    "在线人数统计数据验证结果：%n当前统计值: %d%n基准计算值: %d%n差异: %d%n误差率: %.2f%%%n验证时间: %s",
                    currentOnlineCount, baselineCount, difference, errorRate,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            logger.info(validationResult);

            // 5. 如果误差率超过10%，记录警告信息
            if (errorRate > 10) {
                logger.warning("在线人数统计数据误差率超过10%，可能存在数据不一致问题，请检查！");
            }

        } catch (Exception e) {
            logger.severe("执行在线人数统计数据验证任务时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("在线人数统计数据验证任务执行完成。");
    }

    /**
     * 计算基准在线人数（作为验证的基准值）
     * @return 基准在线人数
     */
    private int calculateBaselineOnlineCount() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 查询所有在最近30分钟内有登录操作且未登出的用户
        List<Log> loginLogs = logMapper.selectList(new LambdaQueryWrapper<Log>()
                .eq(Log::getBehavior, "设备登录")
                .ge(Log::getCreateTime, now.minusMinutes(30))
        );

        int baselineCount = 0;

        // 2. 对每个登录日志，检查是否有后续的登出日志
        for (Log loginLog : loginLogs) {
            List<Log> logoutLogs = logMapper.selectList(new LambdaQueryWrapper<Log>()
                    .eq(Log::getUserId, loginLog.getUserId())
                    .eq(Log::getBehavior, "设备登出")
                    .gt(Log::getCreateTime, loginLog.getCreateTime())
                    .last("LIMIT 1")
            );

            // 如果没有后续的登出日志，则认为用户在线
            if (logoutLogs.isEmpty()) {
                baselineCount++;
            }
        }

        return baselineCount;
    }
}
