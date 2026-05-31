package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.config.SqlExecutionInterceptor;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.vo.stat.AllStatsVO;
import cn.org.wang.exam.model.vo.stat.DailyVO;
import cn.org.wang.exam.model.vo.stat.SubjectExamVO;
import cn.org.wang.exam.model.vo.stat.SubjectStudentVO;
import cn.org.wang.exam.service.IStatService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计管理服务实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class StatServiceImpl extends ServiceImpl<ExamSubjectMapper, Examsubject> implements IStatService {

    @Resource
    private StatMapper statMapper;
    @Resource
    private SubjectMapper subjectMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private UserDailyLoginDurationMapper userDailyLoginDurationMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private LogMapper logMapper;

    @Override
    public Result<List<SubjectStudentVO>> getStudentSubjectCount() {
        //获取课程人数统计
        List<SubjectStudentVO> SubjectStudentVOs;
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode==1) {
            // 教师：只统计自己创建的课程，并排除创建者自己
            SubjectStudentVOs = statMapper.studentSubjectCount(1, userId, null);
        } else {
            SubjectStudentVOs = statMapper.studentSubjectCount(0, userId, null);
        }
        return Result.success("查询成功", SubjectStudentVOs);
    }

    @Override
    public Result<List<SubjectExamVO>> getExamSubjectCount() {
        // 重构为试题分布统计
        List<SubjectExamVO> SubjectExamVOs;
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        if (roleCode == 1) {
            // 教师：查看自己创建的试题分布
            SubjectExamVOs = statMapper.questionTypeCount(userId);
        } else {
            // 管理员：查看所有试题分布
            SubjectExamVOs = statMapper.questionTypeCount(null);
        }
        return Result.success("查询成功", SubjectExamVOs);
    }

    @Override
    public Result<AllStatsVO> getAllCount() {
        AllStatsVO allStatsVO = new AllStatsVO();
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if(roleCode==0){
            allStatsVO.setClassCount(subjectMapper.selectCount(new LambdaQueryWrapper<>()).intValue());
            allStatsVO.setExamCount(examMapper.selectCount(new LambdaQueryWrapper<>()).intValue());
            allStatsVO.setQuestionCount(questionMapper.selectCount(new LambdaQueryWrapper<>()).intValue());
        }else if(roleCode==1){
            // 教师：统计自己创建的课程（查询 t_subject 表，而不是 t_user_subject）
            allStatsVO.setClassCount(subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                            .eq(Subject::getUserId, userId)).intValue());
            allStatsVO.setExamCount(examMapper.selectCount(
                    new LambdaQueryWrapper<Exam>()
                            .eq(Exam::getUserId,userId)).intValue());
            allStatsVO.setQuestionCount(questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>()
                            .eq(Question::getUserId,userId)).intValue());
        }
        return Result.success("查询成功", allStatsVO);
    }

    @Override
    public Result<List<DailyVO>> getDaily() {
        List<DailyVO> daily = userDailyLoginDurationMapper.getDaily(SecurityUtil.getUserId());
        return Result.success("请求成功",daily);
    }

    @Override
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 服务器信息
        Map<String, Object> serverInfo = getServerInfo();
        result.put("serverInfo", serverInfo);
        
        // 2. 数据信息
        Map<String, Object> dataInfo = new HashMap<>();
        
        // 课程总数 - 不包含逻辑删除数据
        int courseCount = subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getIsDeleted, 0))
                .intValue();
        dataInfo.put("courseCount", courseCount);
        
        // 用户总数 - 不包含逻辑删除数据
        int userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, 0)).intValue();
        dataInfo.put("userCount", userCount);
        
        // 今日创建课程 - 使用LocalDateTime避免时区问题
        LocalDateTime todayStart = LocalDate.now().atStartOfDay(); // 今日00:00:00
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59); // 今日23:59:59
        int todayCourseCount = subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getIsDeleted, 0)
                .ge(Subject::getCreateTime, todayStart)
                .le(Subject::getCreateTime, todayEnd))
                .intValue();
        dataInfo.put("todayCourseCount", todayCourseCount);
        
        // 今日新增用户 - 使用LocalDateTime避免时区问题
        int todayUserCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, 0)
                .ge(User::getCreateTime, todayStart)
                .le(User::getCreateTime, todayEnd))
                .intValue();
        dataInfo.put("todayUserCount", todayUserCount);
        
        result.put("dataInfo", dataInfo);
        
        // 3. 在线人数统计（过去24小时，每2小时一次）
        List<Map<String, Object>> onlineData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime time = now.minusHours( (long) i * 2);
            String timeStr = time.format(DateTimeFormatter.ofPattern("HH:00"));
            
            // 基于登录登出日志计算在线人数
            int count = getOnlineUserCount(time);
            
            Map<String, Object> item = new HashMap<>();
            item.put("time", timeStr);
            item.put("count", count);
            onlineData.add(item);
        }
        result.put("onlineData", onlineData);
        
        // 4. 数据信息统计（过去7天的注册人数和新增课程）
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM/dd"));
            
            // 使用LocalDateTime避免时区问题，移除is_deleted过滤
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            // 每日注册人数 - 不包含逻辑删除数据
            int registerCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getIsDeleted, 0)
                    .ge(User::getCreateTime, dayStart)
                    .le(User::getCreateTime, dayEnd))
                    .intValue();
            
            // 每日新增课程 - 不包含逻辑删除数据
            int courseCountDaily = subjectMapper.selectCount(new LambdaQueryWrapper<Subject>()
                    .eq(Subject::getIsDeleted, 0)
                    .ge(Subject::getCreateTime, dayStart)
                    .le(Subject::getCreateTime, dayEnd))
                    .intValue();
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("registerCount", registerCount);
            item.put("courseCount", courseCountDaily);
            dailyData.add(item);
        }
        result.put("dailyData", dailyData);
        
        // 5. SQL执行统计信息
        SqlExecutionInterceptor.SqlStatistics sqlStats = SqlExecutionInterceptor.getStatistics();
        
        // 操作统计数据
        List<Integer> opData = new ArrayList<>();
        opData.add(sqlStats.operationCount.get("Select").get());
        opData.add(sqlStats.operationCount.get("Update").get());
        opData.add(sqlStats.operationCount.get("Delete").get());
        opData.add(sqlStats.operationCount.get("Insert").get());
        result.put("opData", opData);
        
        // 耗时分布数据
        List<Map<String, Object>> durationData = new ArrayList<>();
        for (Map.Entry<String, java.util.concurrent.atomic.AtomicInteger> entry : sqlStats.durationDistribution.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue().get());
            durationData.add(item);
        }
        result.put("durationData", durationData);
        
        return Result.success("获取数据成功", result);
    }
    

    
    /**
     * 获取服务器信息
     * @return 服务器信息
     */
    private Map<String, Object> getServerInfo() {
        Map<String, Object> serverInfo = new HashMap<>();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        try {
            // 获取服务器名称
            String name = InetAddress.getLocalHost().getHostName();
            serverInfo.put("name", name);
            
            // 获取操作系统信息
            String os = osBean.getName() + " " + osBean.getVersion();
            serverInfo.put("os", os);
            
            // 获取IP地址
            String ip = getLocalIpAddress();
            serverInfo.put("ip", ip);
            
            // 获取系统架构
            String arch = osBean.getArch();
            serverInfo.put("arch", arch);
        } catch (Exception e) {
            // 异常处理，使用默认值
            serverInfo.put("name", "未知");
            serverInfo.put("os", "未知");
            serverInfo.put("ip", "未知");
            serverInfo.put("arch", "未知");
        }
        
        return serverInfo;
    }
    
    /**
     * 获取本地IP地址
     * @return 本地IP地址
     */
    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }
    
    /**
     * 计算指定时间点的在线用户数
     * @param time 指定时间点
     * @return 在线用户数
     */
    private int getOnlineUserCount(LocalDateTime time) {
        // 1. 查询所有在指定时间点之前有登录/登出记录的唯一用户ID
        List<Integer> userIds = logMapper.selectObjs(new LambdaQueryWrapper<Log>()
                .le(Log::getCreateTime, time)
                .select(Log::getUserId)
                .groupBy(Log::getUserId)
        ).stream().filter(java.util.Objects::nonNull) // 过滤null用户ID
         .map(Integer.class::cast).toList();
        
        int onlineCount = 0;
        
        // 2. 对每个用户检查是否在线
        for (Integer userId : userIds) {
            // 排除管理员用户（roleId为0）
            User user = userMapper.selectById(userId);
            if (user == null || user.getRoleId() != null && user.getRoleId() == 0) {
                continue; // 跳过管理员用户或不存在的用户
            }
            
            // 查询用户在指定时间点之前的最近登录/登出日志
            List<Log> logs = logMapper.selectList(new LambdaQueryWrapper<Log>()
                    .eq(Log::getUserId, userId)
                    .le(Log::getCreateTime, time)
                    .orderByDesc(Log::getCreateTime)
                    .last("LIMIT 1")
            );
            
            if (!logs.isEmpty()) {
                Log latestLog = logs.get(0);
                // 检查最近的日志行为
                String behavior = latestLog.getBehavior();
                
                // 计算日志时间与指定时间的间隔
                long minutesDiff = java.time.Duration.between(latestLog.getCreateTime(), time).toMinutes();
                
                // 修正逻辑：登录且30分钟内 且 未登出
                if ("设备登录".equals(behavior) && minutesDiff <= 30) {
                    onlineCount++;
                }
                // 若最近操作是登出，视为离线（无需continue，直接结束当前迭代）
            }
            // 如果没有日志记录，默认视为离线
        }
        
        return onlineCount;
    }
    
    /**
     * 计算当前实时在线用户数
     * @return 当前在线用户数
     */
    public int getCurrentOnlineUserCount() {
        return getOnlineUserCount(LocalDateTime.now());
    }
}


