<template>
  <div class="admin-login-container">
    <el-form
      ref="loginForm"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">管理员登录</h3>
        <p class="subtitle">仅限授权IP访问</p>
      </div>

      <el-form-item prop="userNo">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="userNo"
          v-model="loginForm.userNo"
          placeholder="管理员账号"
          name="userNo"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password"
          v-model="loginForm.password"
          :type="passwordType"
          placeholder="密码"
          name="password"
          tabindex="2"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
        <router-link style="color: #66b1ff;" to="/login">切换用户登录</router-link>
        <router-link style="color: #66b1ff; " to="/admin/forgot-password">忘记密码</router-link>
      </div>

      <el-form-item>
        <el-button
          :loading="loading"
          type="primary"
          style="width: 100%;font-size=16px"
          @click="handleLogin"
        >管理员登录</el-button>
      </el-form-item>
    </el-form>

    <!-- 滑块验证码组件 -->
    <SlideCaptcha v-model:show="showSlideCaptcha" @success="onSlideSuccess" />

  </div>
</template>

<script>
import { adminLogin } from '@/api/admin'
import { createSlideCaptcha, verifySlideCaptcha } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { useTagsViewStore } from '@/stores/tagsView'
import SlideCaptcha from '@/components/SlideCaptcha'
import { ElMessage } from 'element-plus'
import { getTokenInfo } from '@/utils/jwtUtils'

export default {
  name: 'AdminLogin',
  components: {
    SlideCaptcha
  },
  setup() {
    const userStore = useUserStore()
    const tagsViewStore = useTagsViewStore()
    return { userStore, tagsViewStore }
  },
  data() {
    const validateUserNo = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入管理员账号'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        userNo: '',
        password: ''
      },
      showSlideCaptcha: false,
      icpNumber: import.meta.env.VITE_APP_ICP_NUMBER,
      icpLink: import.meta.env.VITE_APP_ICP_LINK,
      loginRules: {
        userNo: [{ required: true, trigger: 'blur', validator: validateUserNo }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      passwordType: 'password'
    }
  },
  computed: {
    redirect() {
      return this.$route.query.redirect || '/index'
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs.userNo.focus()
    })
  },
  methods: {
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleLogin() {
      console.log('[AdminLogin] 点击管理员登录按钮')
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          console.log('[AdminLogin] 表单验证通过，尝试唤起滑块验证')
          // 触发滑块验证
          this.showSlideCaptcha = true
        } else {
          console.warn('[AdminLogin] 表单验证失败')
          return false
        }
      })
    },
    onSlideSuccess(verifyToken) {
      console.log('[AdminLogin] 滑块验证成功，准备提交登录，验证token:', verifyToken)
      // 验证成功，执行登录
      this.loading = true
      const loginData = {
        userNo: this.loginForm.userNo,
        password: this.loginForm.password,
        verifyToken: verifyToken  // 传递验证token
      }

      adminLogin(loginData)
        .then((response) => {
          console.log('[AdminLogin] 登录响应:', response)
          const token = response.data
          // 存储token
          this.userStore.setToken(token)

          // 解析token获取用户信息
          const userInfo = getTokenInfo(token)
          console.log('[AdminLogin] 解析的用户信息:', userInfo)

          // 设置用户角色信息
          this.userStore.setRoles(userInfo.roles || ['admin'])
          this.userStore.setUserInfo(userInfo)

          this.tagsViewStore.closeSidebar()

          // 跳转到管理后台首页
          this.$router.push('/index')
          ElMessage.success('管理员登录成功')
          this.loading = false
        })
        .catch((error) => {
          console.error('[AdminLogin] 登录请求失败:', error)
          this.loading = false

          // 获取错误信息
          const errorMsg = error.msg || error.message || '登录失败，请重试'
          console.log('[AdminLogin] 提取的错误信息:', errorMsg)

          // 检查错误类型并显示对应的提示
          if (errorMsg.includes('IP not in whitelist') || errorMsg.includes('Access denied')) {
            ElMessage.error('当前IP不在白名单中，无法访问管理员接口')
          } else if (errorMsg.includes('用户名或密码错误') || errorMsg.includes('该用户不存在')) {
            ElMessage.error('管理员账号或密码错误')
            this.$nextTick(() => {
              this.$refs.password.focus()
            })
          } else if (errorMsg.includes('请先通过滑块验证')) {
            console.warn('[AdminLogin] 验证过期，重新唤起滑块')
            ElMessage.error('验证过期，请重新验证')
            this.showSlideCaptcha = true
          } else {
            ElMessage.error(errorMsg)
          }
        })
    }
  }
}
</script>

<style lang="scss">
/* 重置element-ui样式 */
.admin-login-container {
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
      color: #fff;
      height: 50px;
      caret-color: #fff;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px #283443 inset !important;
        -webkit-text-fill-color: #fff !important;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.admin-login-container {
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
    .title-container .title {
      font-size: 22px;
      margin-bottom: 10px;
    }
  }

  @media (max-width: 479px) {
    .login-form {
      padding: 50px 15px 0;
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
      margin: 0px auto 10px auto;
      text-align: center;
      font-weight: bold;
    }

    .subtitle {
      font-size: 14px;
      color: $dark_gray;
      text-align: center;
      margin-bottom: 40px;
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

  .ip-notice {
    margin-top: 20px;

    .el-alert {
      background-color: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);

      :deep(.el-alert__title) {
        color: #e6a23c;
      }

      :deep(.el-alert__description) {
        color: #ccc;
      }
    }
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
