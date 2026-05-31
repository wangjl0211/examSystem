package cn.org.wang.exam.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.mapper.AdminMapper;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.model.entity.Admin;
import cn.org.wang.exam.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

/**
 * 验证码服务实现类
 * 
 * @Author Wang
 * @Version 1.0
 * @Date 2026-02-24
 */
@Service
@Slf4j
public class VerificationCodeServiceImpl {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final MailServiceImpl mailService;

    // 验证码前缀
    private static final String VERIFICATION_CODE_PREFIX = "verify:code:";
    // 验证码长度
    private static final int VERIFICATION_CODE_LENGTH = 6;
    // 验证码字符集
    private static final String VERIFICATION_CODE_CHARSET = "0123456789";
    // 安全随机数生成器（复用实例）
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 验证码过期时间（分钟），从配置文件读取
     */
    @Value("${verification.code.expire-minutes:5}")
    private int verificationCodeExpireMinutes;

    /**
     * 构造器注入
     * 
     * @param stringRedisTemplate StringRedisTemplate实例
     * @param userMapper UserMapper实例
     * @param adminMapper AdminMapper实例
     * @param mailService MailServiceImpl实例
     */
    @Autowired
    public VerificationCodeServiceImpl(StringRedisTemplate stringRedisTemplate, 
                                     UserMapper userMapper, 
                                     AdminMapper adminMapper, 
                                     MailServiceImpl mailService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.mailService = mailService;
    }

    /**
     * 生成6位纯数字格式的验证码
     * 
     * @return 6位纯数字验证码
     */
    public String generateVerificationCode() {
        StringBuilder code = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(VERIFICATION_CODE_CHARSET.length());
            code.append(VERIFICATION_CODE_CHARSET.charAt(index));
        }
        return code.toString();
    }

    /**
     * 验证邮箱与用户账号的绑定关系
     * 
     * @param userNo 学号/工号
     * @param mail 邮箱
     * @return 用户信息
     * @throws ServiceRuntimeException 验证失败异常
     */
    public Object verifyUserAndMail(String userNo, String mail) {
        // 处理特殊场景：当用户名以"admin"开头时查询t_admin表
        if (userNo.toLowerCase().startsWith("admin")) {
            log.info("特殊验证场景：用户输入以 'admin' 开头的用户名 '{}'，查询 t_admin 表", userNo);
            // 查询管理员表，验证输入的用户名是否存在
            LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.eq(Admin::getAdminName, userNo);
            Admin admin = adminMapper.selectOne(adminWrapper);
            
            // 检查管理员是否存在
            if (Objects.isNull(admin)) {
                log.warn("管理员不存在，用户名: {}", userNo);
                throw new ServiceRuntimeException("获取验证码失败");
            }
            
            // 检查邮箱是否与管理员绑定（严格验证邮箱一致性）
            if (Objects.isNull(admin.getMail())) {
                log.warn("管理员未绑定邮箱，管理员: {}", userNo);
                throw new ServiceRuntimeException("获取验证码失败");
            }
            if (!admin.getMail().equals(mail)) {
                log.warn("邮箱与管理员账号不匹配，管理员: {}, 输入邮箱: {}, 绑定邮箱: {}", 
                        userNo, mail, admin.getMail());
                throw new ServiceRuntimeException("获取验证码失败");
            }
            
            return admin;
        } else {
            // 普通用户验证流程
            log.info("普通用户验证：用户输入 '{}'，查询 t_user 表", userNo);
            // 根据学号/工号查询用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUserNo, userNo);
            User user = userMapper.selectOne(wrapper);
            
            // 检查用户是否存在
            if (Objects.isNull(user)) {
                log.warn("用户不存在，学号/工号: {}", userNo);
                throw new ServiceRuntimeException("获取验证码失败");
            }
            
            // 检查用户是否已注销
            if (user.getIsDeleted() == 1) {
                log.warn("用户已注销，学号/工号: {}", userNo);
                throw new ServiceRuntimeException("获取验证码失败");
            }
            
            // 检查邮箱是否与用户绑定（严格验证邮箱一致性）
            if (Objects.isNull(user.getMail())) {
                log.warn("用户未绑定邮箱，学号/工号: {}", userNo);
                throw new ServiceRuntimeException("获取验证码失败");
            }
            if (!user.getMail().equals(mail)) {
                log.warn("邮箱与用户账号不匹配，学号/工号: {}, 输入邮箱: {}, 绑定邮箱: {}", 
                        userNo, mail, user.getMail());
                throw new ServiceRuntimeException("获取验证码失败");
            }
            
            return user;
        }
    }

    /**
     * 发送验证码邮件
     * 
     * @param userNo 学号/工号
     * @param mail 邮箱
     * @throws ServiceRuntimeException 发送失败异常
     */
    public void sendVerificationCode(String userNo, String mail) {
        verifyUserAndMail(userNo, mail);
        
        // 生成验证码
        String verificationCode = generateVerificationCode();
        
        // 将验证码存储到Redis中，设置过期时间
        String key = VERIFICATION_CODE_PREFIX + userNo + ":" + mail;
        stringRedisTemplate.opsForValue().set(
            key, 
            verificationCode, 
            Duration.ofMinutes(verificationCodeExpireMinutes)
        );
        
        // 发送验证码邮件
        mailService.sendVerificationCode(mail, verificationCode, verificationCodeExpireMinutes);
        
        log.info("验证码发送成功，用户: {}, 邮箱: {}", userNo, mail);
    }

    /**
     * 验证验证码的有效性
     * 
     * @param userNo 学号/工号
     * @param mail 邮箱
     * @param verificationCode 验证码
     * @return 验证是否成功
     * @throws ServiceRuntimeException 验证失败异常
     */
    public boolean verifyVerificationCode(String userNo, String mail, String verificationCode) {
        // 构建Redis键
        String key = VERIFICATION_CODE_PREFIX + userNo + ":" + mail;
        
        // 从Redis中获取验证码
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        
        // 检查验证码是否存在
        if (Objects.isNull(storedCode)) {
            log.warn("验证码不存在或已过期，用户: {}, 邮箱: {}", userNo, mail);
            throw new ServiceRuntimeException("验证码不存在或已过期");
        }
        
        // 检查验证码是否正确
        if (!storedCode.equals(verificationCode)) {
            log.warn("验证码错误，用户: {}, 邮箱: {}, 输入验证码: {}, 存储验证码: {}", 
                     userNo, mail, verificationCode, storedCode);
            throw new ServiceRuntimeException("验证码错误");
        }
        
        // 验证成功，删除验证码（防止重复使用）
        stringRedisTemplate.delete(key);
        
        log.info("验证码验证成功，用户: {}, 邮箱: {}", userNo, mail);
        return true;
    }

    /**
     * 获取验证码过期时间（分钟）
     * 
     * @return 验证码过期时间
     */
    public int getVerificationCodeExpireMinutes() {
        return verificationCodeExpireMinutes;
    }

    /**
     * 验证普通用户邮箱绑定关系
     * 仅允许普通用户使用，禁止管理员使用
     *
     * @param userNo 学号/工号（普通用户）
     * @param mail 邮箱
     * @return 用户信息
     * @throws ServiceRuntimeException 验证失败异常
     */
    public User verifyUserMailForNormalUser(String userNo, String mail) {
        // 安全校验：禁止管理员使用此方法
        if (userNo != null && userNo.toLowerCase().startsWith("admin")) {
            log.warn("安全警告：尝试通过普通用户接口验证管理员邮箱，用户: {}", userNo);
            throw new ServiceRuntimeException("管理员请使用管理员专用接口");
        }

        log.info("普通用户邮箱验证：用户 '{}'", userNo);

        // 查询普通用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserNo, userNo);
        User user = userMapper.selectOne(wrapper);

        // 检查用户是否存在
        if (Objects.isNull(user)) {
            log.warn("用户不存在，学号/工号: {}", userNo);
            throw new ServiceRuntimeException("获取验证码失败");
        }

        // 检查用户是否已注销
        if (user.getIsDeleted() == 1) {
            log.warn("用户已注销，学号/工号: {}", userNo);
            throw new ServiceRuntimeException("获取验证码失败");
        }

        // 额外安全检查：确保不是管理员账户
        if (user.getRoleId() != null && user.getRoleId() == 0) {
            log.warn("安全警告：管理员账户尝试通过普通用户接口操作，用户: {}", userNo);
            throw new ServiceRuntimeException("管理员请使用管理员专用接口");
        }

        // 验证邮箱绑定
        validateUserMail(user, userNo, mail);

        return user;
    }

    /**
     * 验证管理员邮箱绑定关系
     * 仅允许管理员使用，禁止普通用户使用
     *
     * @param adminName 管理员用户名
     * @param mail 邮箱
     * @return 管理员信息
     * @throws ServiceRuntimeException 验证失败异常
     */
    public Admin verifyUserMailForAdmin(String adminName, String mail) {
        // 安全校验：仅允许管理员使用此方法
        if (adminName == null || !adminName.toLowerCase().startsWith("admin")) {
            log.warn("安全警告：尝试通过管理员接口验证普通用户邮箱，用户: {}", adminName);
            throw new ServiceRuntimeException("普通用户请使用普通用户专用接口");
        }

        log.info("管理员邮箱验证：管理员 '{}'", adminName);

        // 查询管理员
        LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(Admin::getAdminName, adminName);
        Admin admin = adminMapper.selectOne(adminWrapper);

        // 检查管理员是否存在
        if (Objects.isNull(admin)) {
            log.warn("管理员不存在，用户名: {}", adminName);
            throw new ServiceRuntimeException("获取验证码失败");
        }

        // 验证邮箱绑定
        validateAdminMail(admin, adminName, mail);

        return admin;
    }

    /**
     * 验证普通用户邮箱绑定
     *
     * @param user 用户对象
     * @param userNo 用户编号
     * @param mail 邮箱
     */
    private void validateUserMail(User user, String userNo, String mail) {
        if (Objects.isNull(user.getMail())) {
            log.warn("用户未绑定邮箱，学号/工号: {}", userNo);
            throw new ServiceRuntimeException("获取验证码失败");
        }
        if (!user.getMail().equals(mail)) {
            log.warn("邮箱与用户账号不匹配，学号/工号: {}, 输入邮箱: {}, 绑定邮箱: {}",
                    userNo, mail, user.getMail());
            throw new ServiceRuntimeException("获取验证码失败");
        }
    }

    /**
     * 验证管理员邮箱绑定
     *
     * @param admin 管理员对象
     * @param adminName 管理员用户名
     * @param mail 邮箱
     */
    private void validateAdminMail(Admin admin, String adminName, String mail) {
        if (Objects.isNull(admin.getMail())) {
            log.warn("管理员未绑定邮箱，管理员: {}", adminName);
            throw new ServiceRuntimeException("获取验证码失败");
        }
        if (!admin.getMail().equals(mail)) {
            log.warn("邮箱与管理员账号不匹配，管理员: {}, 输入邮箱: {}, 绑定邮箱: {}",
                    adminName, mail, admin.getMail());
            throw new ServiceRuntimeException("获取验证码失败");
        }
    }
}
