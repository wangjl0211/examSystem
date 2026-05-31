package cn.org.wang.exam.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.org.wang.exam.common.group.UserGroup;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.auth.LoginForm;
import cn.org.wang.exam.model.form.auth.UserForgotPasswordForm;
import cn.org.wang.exam.model.form.auth.AdminForgotPasswordForm;
import cn.org.wang.exam.model.form.auth.SendVerificationCodeForm;
import cn.org.wang.exam.model.form.user.UserForm;
import cn.org.wang.exam.service.IAuthService;
import cn.org.wang.exam.utils.captcha.SlideCaptchaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理控制器
 * 处理用户认证、登录、注册、密码重置等权限相关操作
 * 包含滑块验证码验证、心跳检测等安全功能
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/25 11:05 AM
 */
@Tag(name = "权限管理接口")
@RestController
@RequestMapping("/api/auths")
public class AuthController {


    @Resource
    private IAuthService iAuthService;

    /**
     * 用户登录
     *
     * @param request request对象，用户获取sessionId
     * @return token
     */
    @Operation(summary ="用户登录")
    @PostMapping("/login")
    public Result<String> login(HttpServletRequest request,
                                @Validated @RequestBody LoginForm loginForm) {
        return iAuthService.login(request, loginForm);
    }

    /**
     * 用户注销
     *
     * @param request request对象，需要清除session里面的内容
     * @return 响应结果
     */
    @Operation(summary ="用户注销")
    @DeleteMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        return iAuthService.logout(request);
    }

    /**
     * 用户注册
     *
     * @param request  request对象，用于获取sessionId
     * @param userForm 用户信息
     * @return 响应结果
     */
    @Operation(summary ="用户注册")
    @PostMapping("/register")
    public Result<java.util.Map<String, String>> register(HttpServletRequest request,
                                   @RequestBody @Validated(UserGroup.RegisterGroup.class) UserForm userForm) {
        return iAuthService.register(request, userForm);
    }

    /**
     * 获取滑块验证码
     */
    @Operation(summary = "获取滑块验证码")
    @GetMapping("/captcha/slide/create")
    public Result<SlideCaptchaUtil.SlideCaptchaData> createSlideCaptcha() {
        return iAuthService.createSlideCaptcha();
    }

    /**
     * 校验滑块验证码
     */
    @Operation(summary = "校验滑块验证码")
    @PostMapping("/captcha/slide/verify")
    public Result<String> verifySlideCaptcha(HttpServletRequest request, 
                                             @Valid @RequestBody SlideCaptchaVerifyForm form) {
        // 1. 手动做空值兜底判断（双重保障）
        if (form.getToken() == null || form.getXPos() == null) {
            return Result.failed("参数不能为空");
        }

        // 2. 调用服务层验证
        return iAuthService.verifySlideCaptcha(request, form.getToken(), form.getXPos());
    }

    /**
     * 记录学生登录时间
     *
     * @param request
     * @return
     */
    @Operation(summary ="记录学生登录时间")
    @PostMapping("/track-presence")
    public Result<String> trackPresence(HttpServletRequest request) {
        return iAuthService.sendHeartbeat(request);
    }

    /**
     * 发送忘记密码验证码 (旧接口，保留兼容性)
     *
     * @param form 发送验证码请求表单
     * @return 响应结果
     * @deprecated 请使用 sendUserVerificationCode 或 sendAdminVerificationCode
     */
    @Deprecated
    @Operation(summary ="发送忘记密码验证码(旧)")
    @PostMapping("/forgot-password/send-code")
    public Result<String> sendVerificationCode(@Validated @RequestBody SendVerificationCodeForm form) {
        return iAuthService.sendForgotPasswordCode(form.getUserNo(), form.getMail());
    }

    /**
     * 发送普通用户忘记密码验证码
     * 仅允许普通用户使用，禁止管理员使用此接口
     *
     * @param form 发送验证码请求表单
     * @return 响应结果
     */
    @Operation(summary ="发送普通用户忘记密码验证码")
    @PostMapping("/forgot-password/user/send-code")
    public Result<String> sendUserVerificationCode(@Validated @RequestBody SendVerificationCodeForm form) {
        return iAuthService.sendUserForgotPasswordCode(form.getUserNo(), form.getMail());
    }

    /**
     * 重置普通用户密码
     * 仅允许修改普通用户密码，禁止修改管理员密码
     *
     * @param request 请求对象
     * @param form 普通用户忘记密码请求表单
     * @return 响应结果
     */
    @Operation(summary ="重置普通用户密码")
    @PostMapping("/forgot-password/user/reset")
    public Result<String> resetUserPassword(HttpServletRequest request, 
                                           @Validated @RequestBody UserForgotPasswordForm form) {
        return iAuthService.resetUserPassword(request, form);
    }

    /**
     * 发送管理员忘记密码验证码
     * 仅允许管理员使用，禁止普通用户使用此接口
     *
     * @param form 发送验证码请求表单
     * @return 响应结果
     */
    @Operation(summary ="发送管理员忘记密码验证码")
    @PostMapping("/forgot-password/admin/send-code")
    public Result<String> sendAdminVerificationCode(@Validated @RequestBody SendVerificationCodeForm form) {
        return iAuthService.sendAdminForgotPasswordCode(form.getUserNo(), form.getMail());
    }

    /**
     * 重置管理员密码
     * 仅允许修改管理员密码，禁止修改普通用户密码
     *
     * @param request 请求对象
     * @param form 管理员忘记密码请求表单
     * @return 响应结果
     */
    @Operation(summary ="重置管理员密码")
    @PostMapping("/forgot-password/admin/reset")
    public Result<String> resetAdminPassword(HttpServletRequest request, 
                                            @Validated @RequestBody AdminForgotPasswordForm form) {
        return iAuthService.resetAdminPassword(request, form);
    }

    /**
     * 滑块验证码验证表单
     * 用于接收前端传递的验证码Token和滑块位置信息
     */
    public static class SlideCaptchaVerifyForm {
        /** 验证码Token，用于标识验证码会话 */
        @NotNull(message = "验证Token不能为空")
        @JsonProperty("token")
        private String token;
        /** 滑块横坐标位置，用于验证用户拖动位置 */
        @NotNull(message = "滑块横坐标xPos不能为空")
        @JsonProperty("xPos")
        private Integer xPos;

        /**
         * 获取验证码Token
         * @return 验证码Token字符串
         */
        public String getToken() {
            return token;
        }

        /**
         * 设置验证码Token
         * @param token 验证码Token字符串
         */
        public void setToken(String token) {
            this.token = token;
        }

        /**
         * 获取滑块横坐标位置
         * @return 滑块横坐标整数值
         */
        public Integer getXPos() {
            return xPos;
        }

        /**
         * 设置滑块横坐标位置
         * @param xPos 滑块横坐标整数值
         */
        public void setXPos(Integer xPos) {
            this.xPos = xPos;
        }
    }

}
