package cn.org.wang.exam.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.config.SlideCaptchaConfig;
import cn.org.wang.exam.constants.SystemConstants;
import cn.org.wang.exam.converter.UserConverter;
import cn.org.wang.exam.mapper.AdminMapper;
import cn.org.wang.exam.mapper.RoleMapper;
import cn.org.wang.exam.mapper.UserDailyLoginDurationMapper;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.model.entity.Admin;
import cn.org.wang.exam.model.entity.Log;
import cn.org.wang.exam.model.entity.User;
import cn.org.wang.exam.model.entity.UserDailyLoginDuration;
import cn.org.wang.exam.model.form.auth.LoginForm;
import cn.org.wang.exam.model.form.auth.UserForgotPasswordForm;
import cn.org.wang.exam.model.form.auth.AdminForgotPasswordForm;
import cn.org.wang.exam.model.form.user.UserForm;
import cn.org.wang.exam.service.IAuthService;
import cn.org.wang.exam.service.ILogService;
import cn.org.wang.exam.utils.IPUtils;
import cn.org.wang.exam.utils.JwtUtil;
import cn.org.wang.exam.filter.VerifyTokenFilter;
import cn.org.wang.exam.utils.SecurityUtil;
import cn.org.wang.exam.utils.captcha.SlideCaptchaUtil;
import cn.org.wang.exam.utils.security.SysUserDetails;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 权限管理服务实现类
 *
 * @Author Wang
 * @Version
 * @Date 2026/3/28 1:33 PM
 */
@Service
@Slf4j
public class AuthServiceImpl implements IAuthService {


    // Redis键前缀常量
    private static final String HEARTBEAT_KEY_PREFIX = "user:heartbeat:";
    private static final String VERIFY_CODE_KEY_PREFIX = "isVerifyCode";
    private static final String SLIDE_CAPTCHA_KEY_PREFIX = "captcha:slide:";

    // 缓存实例
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
    // 身份类型常量
    private static final String IDENTITY_TEACHER = SystemConstants.IDENTITY_TEACHER;

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final RoleMapper roleMapper;
    private final UserConverter userConverter;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final UserDailyLoginDurationMapper userDailyLoginDurationMapper;
    private final SlideCaptchaConfig slideCaptchaConfig;
    private final SlideCaptchaUtil slideCaptchaUtil;
    private final HttpServletRequest httpServletRequest;
    private final ILogService logService;
    private final VerificationCodeServiceImpl verificationCodeService;
    private final VerifyTokenFilter verifyTokenFilter;

    // 构造器注入
    public AuthServiceImpl(StringRedisTemplate stringRedisTemplate, UserMapper userMapper, AdminMapper adminMapper, 
                          RoleMapper roleMapper, UserConverter userConverter, ObjectMapper objectMapper, 
                          JwtUtil jwtUtil, UserDailyLoginDurationMapper userDailyLoginDurationMapper, 
                          SlideCaptchaConfig slideCaptchaConfig, SlideCaptchaUtil slideCaptchaUtil, 
                          HttpServletRequest httpServletRequest, ILogService logService, 
                          VerificationCodeServiceImpl verificationCodeService, VerifyTokenFilter verifyTokenFilter) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.roleMapper = roleMapper;
        this.userConverter = userConverter;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.userDailyLoginDurationMapper = userDailyLoginDurationMapper;
        this.slideCaptchaConfig = slideCaptchaConfig;
        this.slideCaptchaUtil = slideCaptchaUtil;
        this.httpServletRequest = httpServletRequest;
        this.logService = logService;
        this.verificationCodeService = verificationCodeService;
        this.verifyTokenFilter = verifyTokenFilter;
    }

    /**
     * 登录
     *
     * @param request
     * @param loginForm 入参
     * @return 响应
     */
    @Override
    public Result<String> login(HttpServletRequest request, LoginForm loginForm) {
        // 验证滑块验证码
        validateSlideCaptcha(request);

        // 获取普通用户信息
        User user = getRegularUser(loginForm);

        // 处理用户权限和认证
        String token = processUserAuthentication(request, user);

        // 清除验证标记
        clearVerificationToken(request);

        // 记录登录日志
        recordLoginLog(user);

        return Result.success("登录成功", token);
    }

    /**
     * 管理员登录
     *
     * @param request
     * @param loginForm 入参
     * @return 响应
     */
    @Override
    public Result<String> adminLogin(HttpServletRequest request, LoginForm loginForm) {
        // 验证滑块验证码
        validateSlideCaptcha(request);

        // 获取管理员用户信息
        User user = getAdminUser(loginForm);

        // 处理用户权限和认证
        String token = processUserAuthentication(request, user);

        // 清除验证标记
        clearVerificationToken(request);

        // 记录登录日志
        recordLoginLog(user);

        return Result.success("登录成功", token);
    }

    /**
     * 验证滑块验证码
     * 从请求头中获取验证token，不依赖session
     */
    private void validateSlideCaptcha(HttpServletRequest request) {
        if (slideCaptchaConfig.isEnabled()) {
            // 从请求头获取验证token
            String verifyToken = request.getHeader("X-Verify-Token");
            if (org.apache.commons.lang3.StringUtils.isBlank(verifyToken)) {
                throw new ServiceRuntimeException("请先通过滑块验证");
            }
            
            String verifyCodeKey = VERIFY_CODE_KEY_PREFIX + verifyToken;
            String verifyCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
            if (StringUtils.isBlank(verifyCode)) {
                throw new ServiceRuntimeException("请先通过滑块验证");
            }
            
            // 验证成功后删除token，防止重复使用
            stringRedisTemplate.delete(verifyCodeKey);
        }
    }

    /**
     * 获取用户信息
     */
    private User getUserInfo(LoginForm loginForm) {
        // 处理特殊登录场景：当输入以"admin"开头的用户名时查询t_admin表
        // 包含"admin"本身作为完整用户名的情况
        if (loginForm.getUserNo() != null && loginForm.getUserNo().startsWith("admin")) {
            log.info("特殊登录场景：用户输入以 'admin' 开头的用户名 '{}'，查询 t_admin 表", loginForm.getUserNo());
            return getAdminUser(loginForm);
        } else {
            // 普通用户登录流程
            return getRegularUser(loginForm);
        }
    }

    /**
     * 获取管理员用户
     * 修复P0安全漏洞：根据输入的用户名精确查询，而非硬编码"admin"
     * 增加 status 状态检查
     */
    private User getAdminUser(LoginForm loginForm) {
        log.info("管理员登录场景：用户输入 '{}'，查询 t_admin 表", loginForm.getUserNo());
        // 根据输入的用户名精确查询管理员，防止登录绕过漏洞
        LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(Admin::getAdminName, loginForm.getUserNo());
        Admin admin = adminMapper.selectOne(adminWrapper);
        if (Objects.isNull(admin)) {
            log.warn("管理员用户不存在");
            throw new ServiceRuntimeException("该用户不存在");
        }
        // 检查管理员是否被禁用
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            log.warn("管理员账号已被禁用：{}", loginForm.getUserNo());
            throw new ServiceRuntimeException("管理员账号已被禁用");
        }
        // 验证密码
        log.debug("验证管理员密码");
        if (!PASSWORD_ENCODER.matches(loginForm.getPassword(), admin.getPassword())) {
            log.warn("管理员密码验证失败");
            throw new ServiceRuntimeException("用户名或密码错误");
        }
        log.info("管理员密码验证成功");
        // 创建临时User对象用于后续流程
        User user = new User();
        user.setId(admin.getId());
        user.setUserNo(loginForm.getUserNo()); // 使用用户输入的完整用户名
        user.setRealName(admin.getAdminName());
        user.setPassword(null);
        user.setRoleId(admin.getRoleId());
        user.setAvatar(admin.getAvatar());
        user.setStatus(1);
        user.setIsDeleted(0);
        return user;
    }

    /**
     * 获取普通用户
     * 增加 status 状态检查
     */
    private User getRegularUser(LoginForm loginForm) {
        log.info("普通用户登录：用户输入 '{}'，查询 t_user 表", loginForm.getUserNo());
        // 根据学号/工号获取用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserNo, loginForm.getUserNo());
        User user = userMapper.selectOne(wrapper);
        // 判断学号/工号是否存在
        if (Objects.isNull(user)) {
            log.warn("普通用户不存在：{}", loginForm.getUserNo());
            throw new ServiceRuntimeException("该用户不存在");
        }
        // 检查用户是否已注销
        if (user.getIsDeleted() == 1) {
            log.warn("普通用户已注销：{}", loginForm.getUserNo());
            throw new ServiceRuntimeException("该用户已注销");
        }
        // 检查用户是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("普通用户已被禁用：{}", loginForm.getUserNo());
            throw new ServiceRuntimeException("账号已被禁用，请联系管理员");
        }
        log.debug("验证普通用户密码：{}", loginForm.getUserNo());
        if (!PASSWORD_ENCODER.matches(loginForm.getPassword(), user.getPassword())) {
            log.warn("普通用户密码验证失败：{}", loginForm.getUserNo());
            throw new ServiceRuntimeException("学号或密码错误，请重新输入");
        }
        log.info("普通用户密码验证成功：{}", loginForm.getUserNo());
        user.setPassword(null);
        return user;
    }

    /**
     * 处理用户认证和生成 Token
     * 采用纯 JWT 无状态认证，不再绑定 Session
     */
    @SneakyThrows(JsonProcessingException.class)
    private String processUserAuthentication(HttpServletRequest request, User user) {
        // 根据用户角色代码
        List<String> permissions = roleMapper.selectCodeById(user.getRoleId());

        // 数据库获取的权限是字符串 Spring Security 需要实现 GrantedAuthority 接口类型，这里做一个类型转换
        List<SimpleGrantedAuthority> userPermissions = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("role_" + permission))
                .toList();

        // 创建一个 SysUserDetails 对象，该类实现了 UserDetails 接口
        SysUserDetails sysUserDetails = new SysUserDetails(user);
        // 把转型后的权限放进 SysUserDetails 对象
        sysUserDetails.setPermissions(userPermissions);
        // 将用户序列化转为字符串
        String userInfo = objectMapper.writeValueAsString(user);
        // 创建 Token
        String token = jwtUtil.createJwt(userInfo, userPermissions.stream()
                .map(String::valueOf)
                .toList());

        // 封装用户的身份信息，为后续的身份验证和授权操作提供必要的输入
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(sysUserDetails, null, userPermissions);

        // 用户信息存放进上下文
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        return token;
    }

    /**
     * 清除验证标记
     * 注意：验证token已在validateSlideCaptcha中删除，此方法保留用于扩展
     */
    private void clearVerificationToken(HttpServletRequest request) {
        // 验证token已在validateSlideCaptcha中处理，无需额外操作
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(User user) {
        String device = httpServletRequest.getHeader("User-Agent");
        String ipRegion = getIpRegion();
        Log log = Log.builder()
                .place(ipRegion)
                .device(extractDeviceType(device))
                .behavior("设备登录")
                .userId(user.getId()).build();
        logService.add(log);
    }

    /**
     * 获取IP归属地
     */
    private String getIpRegion() {
        try {
            return IPUtils.getIPRegion(httpServletRequest);
        } catch (Exception e) {
            log.error("获取IP归属地失败", e);
            return "未知";
        }
    }


    /**
     * 获取设备名称（智能识别）
     * @param userAgent
     * @return 友好的设备类型名称
     */
    public static String extractDeviceType(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return "未知设备";
        }
        
        // 优先识别移动设备
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("iPod")) {
            return "iOS";
        }
        if (userAgent.contains("Mobile") && userAgent.contains("Safari")) {
            return "移动设备";
        }
        
        // 识别桌面系统
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS")) {
            return "Mac";
        }
        if (userAgent.contains("Linux") && !userAgent.contains("Android")) {
            return "Linux";
        }
        
        // 识别浏览器
        if (userAgent.contains("Chrome")) {
            return "Chrome浏览器·";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox浏览器";
        }
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari浏览器";
        }
        if (userAgent.contains("Edge")) {
            return "Edge浏览器";
        }
        
        // 降级方案：提取第一个括号内容
        try {
            String pattern = "\\((.*?);";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(userAgent);
            if (m.find()) {
                String match = m.group(1);
                // 如果匹配到的是 Linux 等简单标识，返回更友好的名称
                if (match.contains("Linux")) {
                    return "Linux";
                }
                return match;
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        
        return "未知设备";
    }

    /**
     * 注销
     * 将 Token 加入黑名单，实现无状态注销
     *
     * @param request request对象
     * @return 注销结果
     */
    @Override
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token)) {
            try {
                // 去除 "Bearer " 前缀
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                // 记录日志
                String device = httpServletRequest.getHeader("User-Agent");
                String ipRegion = Optional.ofNullable(IPUtils.getIPRegion(httpServletRequest)).orElse("暂无信息");
                Log logEntity = Log.builder()
                        .place(ipRegion)
                        .device(extractDeviceType(device))
                        .behavior("设备登出")
                        .userId(SecurityUtil.getUserId()).build();
                logService.add(logEntity);

                // 将 Token 加入黑名单，过期时间为 Token 的剩余有效期
                long expirationSeconds = jwtUtil.getExpirationSeconds();
                verifyTokenFilter.blacklistToken(token, expirationSeconds);

                log.info("用户注销成功，Token 已加入黑名单");
            } catch (Exception e) {
                log.error("登出操作异常", e);
            }
        }
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        return Result.success("退出成功");
    }

    @Override
    public Result<SlideCaptchaUtil.SlideCaptchaData> createSlideCaptcha() {
        if (!slideCaptchaConfig.isEnabled()) {
            return Result.success(null);
        }
        
        try {
            // 1. 生成数据
            SlideCaptchaUtil.SlideCaptchaData data = slideCaptchaUtil.generate();
            
            // 2. 生成唯一Token
            String token = IdUtil.fastSimpleUUID();
            data.setToken(token);
            data.setExpireTime(slideCaptchaConfig.getExpireTime());
            
            // 3. 缓存缺口位置 (key: captcha:slide:{token}, value: xGap)
            String key = SLIDE_CAPTCHA_KEY_PREFIX + token;
            stringRedisTemplate.opsForValue().set(key, String.valueOf(data.getXGap()), 
                    slideCaptchaConfig.getExpireTime(), TimeUnit.SECONDS);
            
            // 4. 清除返回前端的X坐标，防止作弊
            data.setXGap(0);
            
            // 日志优化：只记录关键信息，不记录完整Base64
            log.debug("滑块验证码生成成功，token: {}, 背景图Base64长度: {}, 滑块图Base64长度: {}", 
                    token, 
                    data.getBackgroundImageBase64() != null ? data.getBackgroundImageBase64().length() : 0, 
                    data.getSliderImageBase64() != null ? data.getSliderImageBase64().length() : 0);
            
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("生成滑块验证码失败", e);
            return Result.failed("生成验证码失败，请重试");
        }
    }

    @Override
    public Result<String> verifySlideCaptcha(HttpServletRequest request, String token, Integer xPos) {
        if (!slideCaptchaConfig.isEnabled()) {
            return Result.success("验证功能未开启");
        }
    
        if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
            throw new ServiceRuntimeException("验证Token不能为空");
        }
        
        if (xPos == null) {
            throw new ServiceRuntimeException("滑块横坐标xPos不能为空");
        }
        
        // 1. 获取缓存的缺口位置
        String key = SLIDE_CAPTCHA_KEY_PREFIX + token;
        String xGapStr = stringRedisTemplate.opsForValue().get(key);
        
        if (org.apache.commons.lang3.StringUtils.isBlank(xGapStr)) {
            throw new ServiceRuntimeException("验证码已过期或无效");
        }
        
        int xGap = Integer.parseInt(xGapStr);
        
        // 2. 校验 (允许配置的容错范围)
        if (Math.abs(xPos - xGap) > slideCaptchaConfig.getTolerance()) {
            // 验证失败
            throw new ServiceRuntimeException("验证失败，偏差过大");
        }
        
        // 3. 验证成功
        // 删除一次性Token
        stringRedisTemplate.delete(key);
        
        // 生成独立的验证token（不依赖session）
        String verifyToken = IdUtil.fastSimpleUUID();
        String verifyCodeKey = VERIFY_CODE_KEY_PREFIX + verifyToken;
        stringRedisTemplate.opsForValue().set(verifyCodeKey, "1", 5, TimeUnit.MINUTES);
        
        // 返回验证token给前端
        return Result.success("验证通过", verifyToken);
    }

    /**
     * 注册用户
     *
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return
     */
    @Override
    public Result<java.util.Map<String, String>> register(HttpServletRequest request, UserForm userForm) {
        // 判断验证码
        if (slideCaptchaConfig.isEnabled()) {
            // 从请求头获取验证token
            String verifyToken = request.getHeader("X-Verify-Token");
            if (org.apache.commons.lang3.StringUtils.isBlank(verifyToken)) {
                throw new ServiceRuntimeException("请先通过滑块验证");
            }
            
            String verifyKey = VERIFY_CODE_KEY_PREFIX + verifyToken;
            String verifyCode = stringRedisTemplate.opsForValue().get(verifyKey);
            
            if (StringUtils.isBlank(verifyCode)) {
                throw new ServiceRuntimeException("请先通过滑块验证");
            }
            
            // 验证成功后删除token，防止重复使用
            stringRedisTemplate.delete(verifyKey);
        }
        
        // 判断两次密码是否一致
        if (!userForm.getPassword().equals(userForm.getCheckedPassword())) {
            throw new ServiceRuntimeException("两次密码不一致");
        }
        
        // 验证教师证件编号唯一性
        if (IDENTITY_TEACHER.equals(userForm.getIdentity())) {
            if (StringUtils.isBlank(userForm.getTeacherCertNo())) {
                throw new ServiceRuntimeException("教师资格证编号不能为空");
            }
            // 检查证件编号是否已存在
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getTeacherCertNo, userForm.getTeacherCertNo());
            User existingUser = userMapper.selectOne(wrapper);
            if (existingUser != null) {
                throw new ServiceRuntimeException("该教师资格证编号已注册");
            }
        }
        
        // 生成用户编号
        String userNo = generateUserNo(userForm.getIdentity());
        
        // 创建用户实体
        User user = userConverter.fromToEntity(userForm);
        user.setUserNo(userNo);
        user.setPassword(PASSWORD_ENCODER.encode(userForm.getPassword()));
        
        // 设置角色ID
        if (IDENTITY_TEACHER.equals(userForm.getIdentity())) {
            user.setRoleId(SystemConstants.ROLE_TEACHER); // 教师角色
        } else {
            user.setRoleId(SystemConstants.ROLE_STUDENT); // 学生角色
        }
        
        // 插入数据库
        userMapper.insert(user);
        
        // 注册成功后清除验证码标记（验证token已在验证时删除，此处理保留用于扩展）
        // 验证token已在上面的验证逻辑中删除，无需额外处理
        
        // 构建返回结果
        java.util.Map<String, String> resultMap = new java.util.HashMap<>();
        resultMap.put("identity", userForm.getIdentity());
        resultMap.put("no", userNo);
        
        // 确保返回的数据结构清晰
        return Result.success("注册成功", resultMap);
    }
    
    /**
     * 生成用户编号
     * @param identity 身份类型 (student/teacher)
     * @return 用户编号
     */
    private String generateUserNo(String identity) {
        String prefix = "S";
        int sequenceLength = 6;
        Integer roleId = SystemConstants.ROLE_STUDENT; // 默认学生角色
        if (IDENTITY_TEACHER.equals(identity)) {
            prefix = "T";
            sequenceLength = 4;
            roleId = SystemConstants.ROLE_TEACHER; // 教师角色
        }
        
        // 获取当前年份后两位
        String year = String.valueOf(java.time.LocalDate.now().getYear()).substring(2);
        
        // 查询当前最大编号，包括逻辑删除的记录
        User lastUser = userMapper.selectMaxUserNo(prefix + year, roleId);
        
        int sequence = 1;
        if (lastUser != null) {
            String lastNo = lastUser.getUserNo();
            try {
                String sequenceStr = lastNo.substring(prefix.length() + year.length());
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (Exception e) {
                log.error("解析心跳时间失败", e);
            }
        }
        
        // 生成新编号
        String format = "%0" + sequenceLength + "d";
        String sequenceStr = String.format(format, sequence);
        return prefix + year + sequenceStr;
    }

    /**
     * 用户发送心跳，更新最后活跃时间。
     *
     * @return
     */
    @Override
    public Result<String> sendHeartbeat(HttpServletRequest request) {
        try {
            Integer userId = SecurityUtil.getUserId();
            // 只处理学生用户的心跳
            if (SecurityUtil.getRoleCode() == SystemConstants.ROLE_STUDENT) {
                processStudentHeartbeat(userId);
            }
        } catch (ServiceRuntimeException e) {
            // 未登录或 Token 未解析时跳过心跳，避免刷 ERROR 日志
            log.debug("心跳跳过：{}", e.getMessage());
        } catch (Exception e) {
            log.warn("心跳处理异常", e);
        }
        return Result.success("请求成功");
    }

    /**
     * 处理学生用户的心跳
     */
    private void processStudentHeartbeat(Integer userId) {
        // 创建Redis键
        String key = HEARTBEAT_KEY_PREFIX + userId;
        
        // 获取当前时间（使用系统默认时区，简化时区转换）
        LocalDateTime now = LocalDateTime.now();
        
        // 获取上一次的心跳时间
        String lastHeartbeatStr = stringRedisTemplate.opsForValue().get(key);
        
        // 设置新的时间
        stringRedisTemplate.opsForValue().set(key, now.toString());
        
        // 计算时间差
        Duration durationSinceLastHeartbeat = calculateDurationSinceLastHeartbeat(lastHeartbeatStr, now);
        
        // 只处理有效的时间差（避免负数或过大的时间差）
        if (durationSinceLastHeartbeat.getSeconds() > 0 && durationSinceLastHeartbeat.getSeconds() < 3600) {
            // 获取今天日期
            LocalDate date = LocalDate.now();
            
            // 实现累加逻辑，更新数据库中的记录
            updateUserLoginDuration(userId, date, (int) durationSinceLastHeartbeat.getSeconds());
        }
    }

    /**
     * 计算上次心跳到现在的时间差
     */
    private Duration calculateDurationSinceLastHeartbeat(String lastHeartbeatStr, LocalDateTime now) {
        if (StringUtils.isNotBlank(lastHeartbeatStr)) {
            try {
                LocalDateTime lastHeartbeat = LocalDateTime.parse(lastHeartbeatStr);
                return Duration.between(lastHeartbeat, now);
        } catch (Exception e) {
            log.error("发送心跳失败", e);
            // 心跳失败不影响其他功能，返回成功
        }
        }
        return Duration.ZERO;
    }

    /**
     * 更新用户登录时长
     * @param userId 用户ID
     * @param date 登录日期
     * @param seconds 登录时长（秒）
     */
    private void updateUserLoginDuration(Integer userId, LocalDate date, int seconds) {
        try {
            UserDailyLoginDuration userDailyLogin = userDailyLoginDurationMapper.getTodayRecord(userId, date);
            if (Objects.isNull(userDailyLogin)) {
                // 如果没记录，创建新记录
                UserDailyLoginDuration userDailyLoginDuration = new UserDailyLoginDuration();
                userDailyLoginDuration.setUserId(userId);
                userDailyLoginDuration.setLoginDate(date);
                userDailyLoginDuration.setTotalSeconds(seconds);
                userDailyLoginDurationMapper.insert(userDailyLoginDuration);
            } else {
                // 如果有记录，累加时长
                userDailyLogin.setTotalSeconds(userDailyLogin.getTotalSeconds() + seconds);
                userDailyLoginDurationMapper.updateById(userDailyLogin);
            }
        } catch (Exception e) {
            log.error("更新用户登录时长失败", e);
            // 数据库操作失败不影响心跳响应
        }
    }

    /**
     * 发送普通用户忘记密码验证码
     * 仅允许普通用户使用，禁止管理员使用此接口
     *
     * @param userNo 学号/工号（普通用户）
     * @param mail 邮箱
     * @return 响应结果
     */
    @Override
    public Result<String> sendUserForgotPasswordCode(String userNo, String mail) {
        try {
            // 空值检查
            if (org.apache.commons.lang3.StringUtils.isBlank(userNo)) {
                log.warn("学号/工号不能为空");
                return Result.failed("请输入学号/工号");
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(mail)) {
                log.warn("邮箱不能为空");
                return Result.failed("请输入邮箱");
            }

            // 安全校验：禁止管理员使用此接口
            if (userNo.toLowerCase().startsWith("admin")) {
                log.warn("安全警告：尝试通过普通用户接口发送管理员验证码，用户: {}", userNo);
                return Result.failed("账号越权");
            }

            // 验证用户类型并发送验证码
            verificationCodeService.verifyUserMailForNormalUser(userNo, mail);
            verificationCodeService.sendVerificationCode(userNo, mail);
            return Result.success("验证码发送成功，请查收邮件");
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("发送普通用户忘记密码验证码失败", e);
            return Result.failed("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 重置普通用户密码
     * 仅允许修改普通用户密码，禁止修改管理员密码
     *
     * @param request 请求对象
     * @param form 普通用户忘记密码请求表单
     * @return 响应结果
     */
    @Override
    public Result<String> resetUserPassword(HttpServletRequest request, UserForgotPasswordForm form) {
        try {
            // 安全校验：禁止管理员使用此接口
            if (form.getUserNo() != null && form.getUserNo().toLowerCase().startsWith("admin")) {
                log.warn("安全警告：尝试通过普通用户接口修改管理员密码，用户: {}", form.getUserNo());
                return Result.failed("账号越权");
            }

            // 验证滑块验证码
            validateSlideCaptchaForReset(request);

            // 验证新密码和确认密码是否一致
            if (!form.isPasswordMatch()) {
                throw new ServiceRuntimeException("两次输入的密码不一致");
            }

            // 验证验证码
            verificationCodeService.verifyVerificationCode(form.getUserNo(), form.getMail(), form.getVerificationCode());

            // 查询普通用户（强制只查普通用户表）
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUserNo, form.getUserNo())
                   .eq(User::getIsDeleted, 0);
            User user = userMapper.selectOne(wrapper);

            if (Objects.isNull(user)) {
                log.warn("普通用户不存在：{}", form.getUserNo());
                throw new ServiceRuntimeException("该用户不存在或已被注销");
            }

            // 额外安全检查：确保不是管理员账户
            if (user.getRoleId() != null && user.getRoleId() == SystemConstants.ROLE_ADMIN) {
                log.warn("安全警告：尝试通过普通用户接口修改管理员密码，用户: {}", form.getUserNo());
                return Result.failed("账号越权");
            }

            // 加密新密码并更新
            String encryptedPassword = PASSWORD_ENCODER.encode(form.getNewPassword());
            user.setPassword(encryptedPassword);
            userMapper.updateById(user);

            log.info("普通用户密码重置成功，用户: {}", form.getUserNo());
            return Result.success("密码重置成功");
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("重置普通用户密码失败", e);
            return Result.failed("重置密码失败，请稍后重试");
        }
    }

    /**
     * 发送管理员忘记密码验证码
     * 仅允许管理员使用，禁止普通用户使用此接口
     *
     * @param adminName 管理员用户名
     * @param mail 邮箱
     * @return 响应结果
     */
    @Override
    public Result<String> sendAdminForgotPasswordCode(String adminName, String mail) {
        try {
            // 空值检查
            if (org.apache.commons.lang3.StringUtils.isBlank(adminName)) {
                log.warn("管理员用户名不能为空");
                return Result.failed("请输入管理员用户名");
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(mail)) {
                log.warn("邮箱不能为空");
                return Result.failed("请输入邮箱");
            }

            // 安全校验：仅允许管理员使用此接口
            if (!adminName.toLowerCase().startsWith("admin")) {
                log.warn("安全警告：尝试通过管理员接口发送管理员验证码，用户: {}", adminName);
                return Result.failed("非管理员用户请使用普通用户密码重置接口");
            }

            // 验证管理员邮箱并发送验证码
            verificationCodeService.verifyUserMailForAdmin(adminName, mail);
            verificationCodeService.sendVerificationCode(adminName, mail);
            return Result.success("验证码发送成功，请查收邮件");
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("发送管理员忘记密码验证码失败", e);
            return Result.failed("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 重置管理员密码
     * 仅允许修改管理员密码，禁止修改普通用户密码
     *
     * @param request 请求对象
     * @param form 管理员忘记密码请求表单
     * @return 响应结果
     */
    @Override
    public Result<String> resetAdminPassword(HttpServletRequest request, AdminForgotPasswordForm form) {
        try {
            // 安全校验：仅允许管理员使用此接口
            if (form.getAdminName() == null || !form.getAdminName().toLowerCase().startsWith("admin")) {
                log.warn("安全警告：尝试通过管理员接口修改管理员密码，用户: {}", form.getAdminName());
                return Result.failed("非管理员用户请使用管理员密码重置接口");
            }

            // 验证滑块验证码
            validateSlideCaptchaForReset(request);

            // 验证新密码和确认密码是否一致
            if (!form.isPasswordMatch()) {
                throw new ServiceRuntimeException("两次输入的密码不一致");
            }

            // 验证验证码
            verificationCodeService.verifyVerificationCode(form.getAdminName(), form.getMail(), form.getVerificationCode());

            // 查询管理员（强制只查管理员表）
            LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.eq(Admin::getAdminName, form.getAdminName());
            Admin admin = adminMapper.selectOne(adminWrapper);

            if (Objects.isNull(admin)) {
                log.warn("管理员不存在：{}", form.getAdminName());
                throw new ServiceRuntimeException("管理员账号不存在");
            }

            // 加密新密码并更新
            String encryptedPassword = PASSWORD_ENCODER.encode(form.getNewPassword());
            admin.setPassword(encryptedPassword);
            adminMapper.updateById(admin);

            log.info("管理员密码重置成功，管理员: {}", form.getAdminName());
            return Result.success("密码重置成功");
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("重置管理员密码失败", e);
            return Result.failed("重置密码失败，请稍后重试");
        }
    }

    /**
     * 验证滑块验证码（密码重置专用）
     *
     * @param request 请求对象
     */
    private void validateSlideCaptchaForReset(HttpServletRequest request) {
        if (slideCaptchaConfig.isEnabled()) {
            String verifyToken = request.getHeader("X-Verify-Token");
            if (org.apache.commons.lang3.StringUtils.isBlank(verifyToken)) {
                throw new ServiceRuntimeException("请先通过滑块验证");
            }

            String verifyKey = VERIFY_CODE_KEY_PREFIX + verifyToken;
            String verifyCode = stringRedisTemplate.opsForValue().get(verifyKey);
            if (StringUtils.isBlank(verifyCode)) {
                throw new ServiceRuntimeException("请先通过滑块验证");
            }

            // 验证成功后删除token，防止重复使用
            stringRedisTemplate.delete(verifyKey);
        }
    }

    /**
     * 发送忘记密码验证码 (旧接口，保留兼容性)
     *
     * @param userNo 学号/工号
     * @param mail 邮箱
     * @return 响应结果
     * @deprecated 请使用 sendUserForgotPasswordCode 或 sendAdminForgotPasswordCode
     */
    @Deprecated
    @Override
    public Result<String> sendForgotPasswordCode(String userNo, String mail) {
        try {
            // 发送验证码邮件
            verificationCodeService.sendVerificationCode(userNo, mail);
            return Result.success("验证码发送成功，请查收邮件");
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("发送忘记密码验证码失败", e);
            return Result.failed("发送验证码失败，请稍后重试");
        }
    }

}
