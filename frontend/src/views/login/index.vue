<template>
  <!-- 登录页面组件 -->
  <div class="login-container">
    <el-form
      ref="loginForm"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">登录</h3>
      </div>

      <el-form-item prop="userNo">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="userNo"
          v-model="loginForm.userNo"
          placeholder="学号/工号"
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

      <div
        v-if="enableRegister"
        style="
          display: flex;
          align-items: center;
          justify-content: space-between; 
          margin-bottom: 20px;
        "
      >
        <router-link style="color: #66b1ff; margin-right: 20px;" to="/forgot-password"> 忘记密码 </router-link>
        <router-link style="color: #66b1ff" to="/register"> 注册 </router-link>
      </div>
      <el-form-item>
        <el-button
          :loading="loading"
          type="primary"
          style="width: 100%; font-size: 16px;"
          @click="handleLogin"
        >登  录</el-button>
      </el-form-item>

    </el-form>
    
    <!-- 滑块验证码组件 -->
    <SlideCaptcha v-model:show="showSlideCaptcha" @success="onSlideSuccess" />

  </div>
</template>

<script>
import { useUserStore } from '@/stores/user'
import { useTagsViewStore } from '@/stores/tagsView'
import SlideCaptcha from '@/components/SlideCaptcha'
import { ElMessage } from 'element-plus'

/**
 * 登录页面组件
 * 提供用户登录功能，包括：
 * - 学号/工号和密码输入
 * - 滑块验证码验证
 * - 登录状态管理
 */
export default {
  name: 'Login',
  components: {
    SlideCaptcha
  },
  // 使用 Composition API 初始化 Store
  setup() {
    const userStore = useUserStore()
    const tagsViewStore = useTagsViewStore()
    return { userStore, tagsViewStore }
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
    // 验证密码规则
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }
    return {
      // 登录表单数据
      loginForm: {
        userNo: '',
        password: ''
      },
      // 是否显示滑块验证码
      showSlideCaptcha: false,
      // 是否启用注册功能（从环境变量读取）
      enableRegister: import.meta.env.VITE_APP_ENABLE_REGISTER === 'true',
      icpNumber: import.meta.env.VITE_APP_ICP_NUMBER,
      icpLink: import.meta.env.VITE_APP_ICP_LINK,
      // 表单验证规则
      loginRules: {
        userNo: [{ required: true, trigger: 'blur', validator: validateUserNo }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      // 登录按钮加载状态
      loading: false,
      // 密码输入框显示类型（password或text）
      passwordType: 'password'
    }
  },
  computed: {
    // 登录成功后的重定向路径
    redirect() {
      return this.$route.query.redirect || '/index'
    }
  },
  mounted() {
    // 页面加载完成后自动聚焦到学号/工号输入框
    this.$nextTick(() => {
      this.$refs.userNo.focus()
    })
  },
  methods: {
    // 切换密码显示/隐藏
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
    // 处理登录按钮点击
    handleLogin() {
      console.log('[Login] 点击登录按钮')
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          console.log('[Login] 表单验证通过，尝试唤起滑块验证')
          // 触发滑块验证
          this.showSlideCaptcha = true
        } else {
          console.warn('[Login] 表单验证失败')
          return false
        }
      })
    },
    // 滑块验证成功回调
    onSlideSuccess(verifyToken) {
      console.log('[Login] 滑块验证成功，准备提交登录，验证token:', verifyToken)
      // 验证成功，执行登录
      this.loading = true
      const loginData = {
        userNo: this.loginForm.userNo,
        password: this.loginForm.password,
        verifyToken: verifyToken
      }
      this.userStore.login(loginData).then((result) => {
        console.log('[Login] 登录成功，角色:', result.role)
        this.tagsViewStore.closeSidebar()
        
        // 根据角色跳转到首页（所有角色都跳转到首页）
        let redirectPath = '/index'
        if (result.role === 'admin') {
          redirectPath = '/index'
        } else if (result.role === 'teacher') {
          redirectPath = '/index'
        } else if (result.role === 'student') {
          redirectPath = '/index'
        }
        
        this.$router.push(redirectPath)
        this.loading = false
      }).catch((error) => {
        console.error('[Login] 登录请求失败:', error)
        this.loading = false
        // 获取错误信息，兼容不同的错误对象结构
        const errorMsg = error.msg || error.message || '登录失败，请重试'
        
        console.log('[Login] 提取的错误信息:', errorMsg)
        
        // 检查错误类型并显示对应的错误提示
        if (errorMsg.includes('学号或密码错误，请重新输入') || errorMsg.includes('该用户不存在') || errorMsg.includes('该用户已注销')) {
          ElMessage.error('用户名或密码输入错误，请重新输入')
          // 根据错误类型聚焦到相应的输入框
          if (errorMsg.includes('该用户不存在') || errorMsg.includes('该用户已注销')) {
            this.$nextTick(() => {
              this.$refs.userNo.focus()
            })
          } else {
            this.$nextTick(() => {
              this.$refs.password.focus()
            })
          }
        } else if (errorMsg.includes('请先通过滑块验证')) {
          console.warn('[Login] 验证过期，重新唤起滑块')
          ElMessage.error('验证过期，请重新验证')
          this.showSlideCaptcha = true
        } else {
          // 其他错误，显示通用错误提示
          ElMessage.error(errorMsg)
        }
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

/* 重置element-ui样式 */
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
    .el-button {
      height: 50px;
    }
    .el-form-item {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(0, 0, 0, 0.1);
      border-radius: 5px;
      color: #454545;
      margin-bottom: 20px;  // 确保表单项间距一致
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
    .title-container .title {
      font-size: 22px;
      margin-bottom: 30px;
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
  .but {
    width: 220px;
    height: 39px;
    color: #fff;
    background-color: #409eff;
    border-color: #409eff;
    /* border: 1px solid; */
    padding: 12px 20px;
    font-size: 14px;
    border-radius: 4px;
    font-weight: 500;
    text-align: center;
    font-family: sans-serif;
    padding-top: 10px;
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
