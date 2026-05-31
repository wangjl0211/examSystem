<template>
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

      <!-- 提示信息 -->
      <div class="user-type-notice">
        <el-alert
          title="此页面仅支持管理员用户进行重置密码"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>

      <!-- 1. 管理员用户名输入框 -->
      <el-form-item prop="adminName">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="adminName"
          v-model="forgotForm.adminName"
          placeholder="用户名"
          name="adminName"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <!-- 2. 邮箱输入框 -->
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

      <!-- 3. 验证码输入框 -->
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

      <!-- 4. 新密码输入框 -->
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

      <!-- 5. 确认密码输入框 -->
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

      <div style="display: flex; align-items: center; justify-content: flex-end; margin-bottom: 20px;">
        <router-link style="color: #66b1ff" to="/admin/login"> 登录 </router-link>
      </div>

      <!-- 7. 按钮区域 -->
      <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;">
        <el-button
          type="primary"
          @click="confirmChange"
          :loading="loading"
          style="height: 45px;width: 100%"
        >
          重置密码
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
import { sendAdminVerificationCode, resetAdminPassword } from '@/api/user'

export default {
  name: 'AdminForgotPassword',
  components: {
    SlideCaptcha
  },
  data() {
    const validateAdminName = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入管理员用户名'))
      } else if (!value.toLowerCase().startsWith('admin')) {
        callback(new Error('仅限管理员用户名使用'))
      } else {
        callback()
      }
    }

    const validateMail = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入邮箱'))
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('邮箱格式不正确'))
      } else {
        callback()
      }
    }

    const validateVerificationCode = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入验证码'))
      } else if (!/^\d{6}$/.test(value)) {
        callback(new Error('验证码必须是6位数字'))
      } else {
        callback()
      }
    }

    const validateNewPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入新密码'))
      } else if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }

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
      forgotForm: {
        adminName: '',
        mail: '',
        verificationCode: '',
        newPassword: '',
        confirmPassword: ''
      },
      forgotRules: {
        adminName: [{ required: true, trigger: 'blur', validator: validateAdminName }],
        mail: [{ required: true, trigger: 'blur', validator: validateMail }],
        verificationCode: [{ required: true, trigger: 'blur', validator: validateVerificationCode }],
        newPassword: [{ required: true, trigger: 'blur', validator: validateNewPassword }],
        confirmPassword: [{ required: true, trigger: 'blur', validator: validateConfirmPassword }]
      },
      mailError: '',
      codeError: '',
      loading: false,
      getCodeLoading: false,
      passwordType: 'password',
      confirmPasswordType: 'password',
      countdown: 0,
      showSlideCaptcha: false
    }
  },

  methods: {
    showPwd() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.newPassword.focus()
      })
    },
    showConfirmPwd() {
      this.confirmPasswordType = this.confirmPasswordType === 'password' ? '' : 'password'
      this.$nextTick(() => {
        this.$refs.confirmPassword.focus()
      })
    },
    getVerificationCode() {
      // 重置错误提示
      this.mailError = ''
      this.codeError = ''
      
      // 手动验证管理员用户名和邮箱是否为空
      if (!this.forgotForm.adminName) {
        this.$refs.forgotForm.validateField('adminName')
        return
      }
      if (!this.forgotForm.mail) {
        this.$refs.forgotForm.validateField('mail')
        return
      }
      
      // 验证通过，发送获取验证码请求
      console.log('发送管理员验证码请求:', this.forgotForm.adminName, this.forgotForm.mail)
      
      // 调用后端API发送验证码（管理员专用接口）
      const requestData = {
        userNo: this.forgotForm.adminName,
        mail: this.forgotForm.mail
      }
      
      // 添加加载状态
      this.getCodeLoading = true
      
      sendAdminVerificationCode(requestData).then((res) => {
        console.log('发送验证码响应:', res)
        this.getCodeLoading = false
        if (res && (res.code === 1 || res.code === 200)) {
          // 发送成功，开始倒计时
          ElMessage({
            message: '验证码发送成功，请查收邮件',
            type: 'success',
            duration: 3000
          })
          this.startCountdown()
        } else {
          // 发送失败
          if (res?.msg === '获取验证码失败') {
            this.codeError = '获取失败，管理员用户名或邮箱不正确'
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
          this.codeError = '获取失败，管理员用户名或邮箱不正确'
        } else if (error.message && error.message.includes('Authorization为空')) {
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
    startCountdown() {
      this.countdown = 300 // 5分钟
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    },
    confirmChange() {
      this.$refs.forgotForm.validate((valid) => {
        if (valid) {
          // 触发滑块验证
          this.showSlideCaptcha = true
        }
      })
    },
    onSlideSuccess(verifyToken) {
      // 滑块验证成功，执行密码修改
      this.loading = true
      console.log('提交管理员密码修改请求:', this.forgotForm, ', verifyToken:', verifyToken)
      
      // 调用后端API重置密码（管理员专用接口）
      const requestData = {
        adminName: this.forgotForm.adminName,
        mail: this.forgotForm.mail,
        verificationCode: this.forgotForm.verificationCode,
        newPassword: this.forgotForm.newPassword,
        confirmPassword: this.forgotForm.confirmPassword,
        verifyToken: verifyToken
      }
      
      resetAdminPassword(requestData).then((res) => {
        console.log('重置密码响应:', res)
        this.loading = false
        if (res && (res.code === 1 || res.code === 200)) {
          // 重置成功
          ElMessage({
            message: '密码重置成功',
            type: 'success',
            duration: 2000
          })
          
          // 2秒后跳转到管理员登录页面
          setTimeout(() => {
            this.$router.push('/admin/login')
          }, 2000)
        } else {
          // 重置失败
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
      background-color: rgba(230, 162, 60, 0.1);
      border: 1px solid rgba(230, 162, 60, 0.2);
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
