package cn.org.wang.exam.service;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.auth.LoginForm;
import cn.org.wang.exam.model.form.auth.UserForgotPasswordForm;
import cn.org.wang.exam.model.form.auth.AdminForgotPasswordForm;
import cn.org.wang.exam.model.form.user.UserForm;
import cn.org.wang.exam.utils.captcha.SlideCaptchaUtil;
import jakarta.servlet.http.HttpServletRequest;


/**
 * 权限管理接口
 * 
 * 变更说明：
 * 1. 已移除过时的resetPassword方法，该方法存在安全风险，允许跨角色修改密码
 * 2. 密码重置功能已重构为两个独立方法：
 *    - resetUserPassword: 仅用于普通用户密码重置，禁止修改管理员密码
 *    - resetAdminPassword: 仅用于管理员密码重置，禁止修改普通用户密码
 * 3. 这种分离设计提高了系统安全性，防止权限越界操作
 *
 * @Author Wang
 * @Version 2.0
 * @Date 2026/3/28 1:33 PM
 */
public interface IAuthService {

    /**
     * 登录
     *
     * @param request
     * @param loginForm
     * @return
     */
    Result<String> login(HttpServletRequest request, LoginForm loginForm);

    /**
     * 管理员登录
     *
     * @param request
     * @param loginForm
     * @return
     */
    Result<String> adminLogin(HttpServletRequest request, LoginForm loginForm);

    /**
     * 用户注销
     *
     * @param request request对象，需要清除session里面的内容
     * @return 响应结果
     */
    Result<String> logout(HttpServletRequest request);

    /**
     * 创建滑块验证码
     *
     * @return 验证码数据
     */
    Result<SlideCaptchaUtil.SlideCaptchaData> createSlideCaptcha();

    /**
     * 校验滑块验证码
     *
     * @param request request对象
     * @param token   验证码token
     * @param xPos    用户滑动的X坐标
     * @return 校验结果
     */
    Result<String> verifySlideCaptcha(HttpServletRequest request, String token, Integer xPos);

    /**
     * 注册用户
     *
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return 响应结果
     */
    Result<java.util.Map<String, String>> register(HttpServletRequest request, UserForm userForm);

    /**
     * 记录学生登录时间
     *
     * @param request
     * @return
     */
    Result<String> sendHeartbeat(HttpServletRequest request);

    /**
     * 发送普通用户忘记密码验证码
     *
     * @param userNo 学号/工号
     * @param mail 邮箱
     * @return 响应结果
     */
    Result<String> sendUserForgotPasswordCode(String userNo, String mail);

    /**
     * 重置普通用户密码
     *
     * @param request 请求对象
     * @param form 忘记密码请求表单
     * @return 响应结果
     */
    Result<String> resetUserPassword(HttpServletRequest request, UserForgotPasswordForm form);

    /**
     * 发送管理员忘记密码验证码
     *
     * @param adminName 管理员用户名
     * @param mail 邮箱
     * @return 响应结果
     */
    Result<String> sendAdminForgotPasswordCode(String adminName, String mail);

    /**
     * 重置管理员密码
     *
     * @param request 请求对象
     * @param form 忘记密码请求表单
     * @return 响应结果
     */
    Result<String> resetAdminPassword(HttpServletRequest request, AdminForgotPasswordForm form);

    /**
     * 发送忘记密码验证码 (旧接口，保留兼容性)
     *
     * @param userNo 学号/工号
     * @param mail 邮箱
     * @return 响应结果
     * @deprecated 请使用 sendUserForgotPasswordCode 或 sendAdminForgotPasswordCode
     */
    @Deprecated
    Result<String> sendForgotPasswordCode(String userNo, String mail);
}
