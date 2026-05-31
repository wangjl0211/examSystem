<template>
  <!-- 忘记密码页面组件 -->
  <div class="login-container">
    <el-form
      ref="forgotForm"
      :model="forgotForm"
      :rules="forgotRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">忘记密码</h3>
      </div>

      <!-- 学号/工号输入框 -->
      <el-form-item prop="userNo">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="userNo"
          v-model="forgotForm.userNo"
          placeholder="学号/工号"
          name="userNo"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <!-- 邮箱输入框 -->
      <el-form-item prop="mail" :error="mailError">
        <span class="svg-container">
          <svg-icon icon-class="email" />
        </span>
        <el-input
          v-model="forgotForm.mail"
          placeholder="邮箱"
          name="mail"
          type="email"
          tabindex="2"
          auto-complete="on"
        />
      </el-form-item>

      <!-- 验证码输入框 -->
      <el-form-item prop="verificationCode" :error="codeError">
        <div style="display: flex; align-items: center; width: 100%;">
          <span class="svg-container" style="flex-shrink: 0;">
            <svg-icon icon-class="captcha" />
          </span>
          <el-input
            v-model="forgotForm.verificationCode"
            placeholder="验证码"
            name="verificationCode"
            type="text"
            tabindex="3"
            auto-complete="on"
            style="width: 65%; flex-grow: 1;"
          />
          <!-- 获取验证码按钮 -->
          <el-button
            type="primary"
            @click="getVerificationCode"
            :disabled="countdown > 0"
            :loading="getCodeLoading"
            style="width: 30%; flex-shrink: 0; height: 45px; padding: 0 10px;"
          >
            {{ countdown > 0 ? `${countdown}秒后重新获取` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 新密码输入框 -->
      <el-form-item prop="newPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="newPassword"
          v-model="forgotForm.newPassword"
          :type="passwordType"
          placeholder="新密码"
          name="newPassword"
          tabindex="4"
          auto-complete="off"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <!-- 确认密码输入框 -->
      <el-form-item prop="confirmPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="confirmPasswordType"
          ref="confirmPassword"
          v-model="forgotForm.confirmPassword"
          :type="confirmPasswordType"
          placeholder="确认密码"
          name="confirmPassword"
          tabindex="5"
          auto-complete="off"
        />
        <span class="show-pwd" @click="showConfirmPwd">
          <svg-icon :icon-class="confirmPasswordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <!-- 返回登录链接 -->
      <div style="display: flex; align-items: center; justify-content: flex-end; margin-bottom: 20px;">
        <router-link style="color: #66b1ff" to="/login"> 登录 </router-link>
      </div>

      <!-- 重置密码按钮区域 -->
      <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;">
        <el-button
          type="primary"
          @click="confirmChange"
          :loading="loading"
          style="height: 45px;font-size=16px;width: 100%"
        >
          重 置 密 码
        </el-button>
      </div>
    </el-form>

    <!-- 滑块验证码组件 -->
    <SlideCaptcha v-model:show="showSlideCaptcha" @success="onSlideSuccess" />

  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import SlideCaptcha from '@/components/SlideCaptcha'
import { sendUserVerificationCode, resetUserPassword } from '@/api/user'

/**
 * 忘记密码页面组件
 * 提供用户重置密码功能，包括：
 * - 学号/工号和邮箱验证
 * - 邮箱验证码获取和验证
 * - 新密码设置
 * - 滑块验证码验证
 */
export default {
  name: 'ForgotPassword',
  components: {
    SlideCaptcha
  },
  data() {
    // 验证学号/工号规则
    const validateUserNo = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入学号/工号'))
      } else {
        callback()
      }
    }

    // 验证邮箱格式规则
    const validateMail = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入邮箱'))
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('邮箱格式不正确'))
      } else {
        callback()
      }
    }

    // 验证验证码规则（6位数字）
    const validateVerificationCode = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入验证码'))
      } else if (!/^\d{6}$/.test(value)) {
        callback(new Error('验证码必须是6位数字'))
      } else {
        callback()
      }
    }

    // 验证新密码规则
    const validateNewPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入新密码'))
      } else if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }

    // 验证确认密码规则（需要与新密码一致）
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请确认新密码'))
      } else if (value !== this.forgotForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      icpNumber: import.meta.env.VITE_APP_ICP_NUMBER,
      icpLink: import.meta.env.VITE_APP_ICP_LINK,
      // 忘记密码表单数据
      forgotForm: {
        userNo: '',
        mail: '',
        verificationCode: '',
        newPassword: '',
        confirmPassword: ''
      },
      // 表单验证规则
      forgotRules: {
        userNo: [{ required: true, trigger: 'blur', validator: validateUserNo }],
        mail: [{ required: true, trigger: 'blur', validator: validateMail }],
        verificationCode: [{ required: true, trigger: 'blur', validator: validateVerificationCode }],
        newPassword: [{ required: true, trigger: 'blur', validator: validateNewPassword }],
        confirmPassword: [{ required: true, trigger: 'blur', validator: validateConfirmPassword }]
      },
      // 邮箱输入框错误提示
      mailError: '',
      // 验证码输入框错误提示
      codeError: '',
      // 重置密码按钮加载状态
      loading: false,
      // 获取验证码按钮加载状态
      getCodeLoading: false,
      // 新密码输入框显示类型
      passwordType: 'password',
      // 确认密码输入框显示类型
      confirmPasswordType: 'password',
      // 验证码倒计时（秒）
      countdown: 0,
      // 是否显示滑块验证码
      showSlideCaptcha: false
    }
  },

  methods: {
    // 切换新密码显示/隐藏
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.newPassword.focus()
      })
    },
    // 切换确认密码显示/隐藏
    showConfirmPwd() {
      this.confirmPasswordType = this.confirmPasswordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.confirmPassword.focus()
      })
    },
    // 获取邮箱验证码
    getVerificationCode() {
      // 重置错误提示
      this.mailError = ''
      this.codeError = ''
      
      // 手动验证学号/工号和邮箱是否为空
      if (!this.forgotForm.userNo) {
        this.$refs.forgotForm.validateField('userNo')
        return
      }
      if (!this.forgotForm.mail) {
        this.$refs.forgotForm.validateField('mail')
        return
      }
      
      console.log('发送验证码请求:', this.forgotForm.userNo, this.forgotForm.mail)
      
      // 构造请求数据
      const requestData = {
        userNo: this.forgotForm.userNo,
        mail: this.forgotForm.mail
      }
      
      this.getCodeLoading = true
      
      // 调用发送验证码API
      sendUserVerificationCode(requestData).then((res) => {
        console.log('发送验证码响应:', res)
        this.getCodeLoading = false
        if (res && (res.code === 1 || res.code === 200)) {
          ElMessage({
            message: '验证码发送成功，请查收邮件',
            type: 'success',
            duration: 3000
          })
          // 开始倒计时
          this.startCountdown()
        } else {
          if (res?.msg === '获取验证码失败') {
            this.codeError = '获取失败，用户名或邮箱不正确'
          } else {
            ElMessage({
              message: res?.msg || '发送验证码失败，请稍后重试',
              type: 'error',
              duration: 3000
            })
          }
        }
      }).catch((error) => {
        console.error('发送验证码失败:', error)
        this.getCodeLoading = false
        if (error.message && error.message.includes('获取验证码失败')) {
          this.codeError = '获取失败，用户名或邮箱不正确'
        } else if (error.message && error.message.includes('Authorization为空')) {
          // 忽略Authorization错误，忘记密码页面不需要登录
          console.warn('Authorization为空，但这是正常的，因为忘记密码页面不需要登录')
        } else {
          ElMessage({
            message: error.msg || error.message || '发送验证码失败，请稍后重试',
            type: 'error',
            duration: 3000
          })
        }
      })
    },
    // 开始倒计时（5分钟）
    startCountdown() {
      this.countdown = 300
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    },
    // 确认重置密码
    confirmChange() {
      this.$refs.forgotForm.validate((valid) => {
        if (valid) {
          // 验证通过，触发滑块验证
          this.showSlideCaptcha = true
        }
      })
    },
    // 滑块验证成功回调
    onSlideSuccess(verifyToken) {
      this.loading = true
      console.log('提交密码修改请求:', this.forgotForm, ', verifyToken:', verifyToken)
      
      // 构造重置密码请求数据
      const requestData = {
        userNo: this.forgotForm.userNo,
        mail: this.forgotForm.mail,
        verificationCode: this.forgotForm.verificationCode,
        newPassword: this.forgotForm.newPassword,
        confirmPassword: this.forgotForm.confirmPassword,
        verifyToken: verifyToken
      }
      
      // 调用重置密码API
      resetUserPassword(requestData).then((res) => {
        console.log('重置密码响应:', res)
        this.loading = false
        if (res && (res.code === 1 || res.code === 200)) {
          ElMessage({
            message: '密码重置成功',
            type: 'success',
            duration: 2000
          })
          
          // 2秒后跳转到登录页面
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          ElMessage({
            message: res?.msg || '密码重置失败，请稍后重试',
            type: 'error',
            duration: 3000
          })
        }
      }).catch((error) => {
        console.error('重置密码失败:', error)
        this.loading = false
        ElMessage({
          message: error.msg || error.message || '密码重置失败，请稍后重试',
          type: 'error',
          duration: 3000
        })
      })
    }
  }
}
</script>

<style lang="scss">
/* 修复input背景不协调和光标变色 */
/* 详见：https://github.com/PanJiaChen/vue-element-admin/pull/927 */

$bg: #283443;
$light_gray: #fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.login-container {
    .el-input {
      display: inline-block;
      height: 50px;
      width: 85%;

      .el-input__wrapper {
        padding: 0;
        background: transparent;
        box-shadow: none !important;
        width: 100%;
        height: 100%;
      }

      & input {
        background: transparent;
        border: 0px;
        -webkit-appearance: none;
        border-radius: 0px;
        padding: 12px 5px 12px 15px;
        color: $light_gray;
        height: 50px;
        caret-color: $cursor;
        &:-webkit-autofill {
          box-shadow: 0 0 0px 1000px $bg inset !important;
          -webkit-text-fill-color: $cursor !important;
        }
      }
    }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
    margin-bottom: 20px;
  }

  .el-button {
    height: 50px;
  }

  .user-type-notice {
    margin-bottom: 20px;

    .el-alert {
      background-color: rgba(64, 158, 255, 0.1);
      border: 1px solid rgba(64, 158, 255, 0.2);
    }
  }
}
</style>

<style lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.login-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;

  .login-form {
    position: relative;
    width: 520px;
    max-width: 100%;
    padding: 160px 35px 0;
    margin: 0 auto;
    overflow: hidden;
  }

  @media (max-width: 991px) {
    .login-form {
      padding: 80px 20px 0;
    }
  }
  @media (max-width: 479px) {
    .login-form {
      padding: 50px 15px 0;
    }
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    & span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }



  .icp-info {
    position: absolute;
    bottom: 20px;
    width: 100%;
    text-align: center;

    & a {
      color: $dark_gray;
      font-size: 12px;
      text-decoration: none;
      
      &:hover {
        color: $light_gray;
        text-decoration: underline;
      }
    }
  }
}
</style>