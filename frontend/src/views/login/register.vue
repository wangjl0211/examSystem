<template>
  <!-- 注册页面组件 -->
  <div class="login-container">
    <el-form
      ref="registerForm"
      :model="registerForm"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">注册</h3>
      </div>

      <!-- 身份类型下拉选框 -->
      <el-form-item prop="identity">
        <span class="svg-container">
          <svg-icon icon-class="type" />
        </span>
        <el-select
          v-model="registerForm.identity"
          placeholder="身份类型"
          name="identity"
          tabindex="1"
          @change="onIdentityChange"
          :teleported="false"
        >
          <el-option label="学生" value="student" />
          <el-option label="教师" value="teacher" />
        </el-select>
      </el-form-item>

      <!-- 教师资格证编号输入框（仅教师身份显示） -->
      <el-form-item v-if="registerForm.identity === 'teacher'" prop="teacherCertNo">
        <span class="svg-container">
          <svg-icon icon-class="number" />
        </span>
        <el-input
          v-model="registerForm.teacherCertNo"
          placeholder="教师资格证编号"
          name="teacherCertNo"
          type="text"
          tabindex="2"
          @blur="checkTeacherCert"
        />
      </el-form-item>

      <!-- 姓名输入框 -->
      <el-form-item prop="realName">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="realName"
          v-model="registerForm.realName"
          placeholder="姓名"
          name="realName"
          type="text"
          tabindex="3"
          auto-complete="on"
        />
      </el-form-item>

      <!-- 邮箱输入框 -->
      <el-form-item prop="mail">
        <span class="svg-container">
          <svg-icon icon-class="email" />
        </span>
        <el-input
          v-model="registerForm.mail"
          placeholder="邮箱"
          name="mail"
          type="email"
          tabindex="4"
          auto-complete="on"
        />
      </el-form-item>

      <!-- 密码输入框 -->
      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password"
          v-model="registerForm.password"
          :type="passwordType"
          placeholder="密码"
          name="password"
          tabindex="5"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <!-- 确认密码输入框 -->
      <el-form-item prop="checkedPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="checkedPasswordType"
          ref="checkedPassword"
          v-model="registerForm.checkedPassword"
          :type="checkedPasswordType"
          placeholder="确认密码"
          name="checkedPassword"
          tabindex="6"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd2">
          <svg-icon
            :icon-class="checkedPasswordType === 'password' ? 'eye' : 'eye-open'"
          />
        </span>
      </el-form-item>

      <div
        style="
          display: flex;
          align-items: center;
          justify-content: flex-end;
          margin-bottom: 20px;
        "
      >
        <router-link style="color: #66b1ff" to="/login"> 登录 </router-link>
      </div>

      <el-button
        :loading="loading"
        type="primary"
        style="width: 100%; margin-bottom: 30px; font-size: 16px;"
        @click="registerFn"
      >注  册</el-button>
    </el-form>
    
    <!-- 滑块验证码组件 -->
    <SlideCaptcha v-model:show="showSlideCaptcha" @success="onSlideSuccess" />

    <!-- 注册成功Modal -->
    <el-dialog
      title="注册成功"
      v-model="showSuccessModal"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :show-close="true"
      width="400px"
      center
    >
      <div style="text-align: center; padding: 30px 0;">
        <div style="font-size: 48px; color: #67c23a; margin-bottom: 20px;">
          <el-icon><SuccessFilled /></el-icon>
        </div>
        <h3 style="color: #303133; margin-bottom: 24px;">注册成功</h3>
        <p style="color: #606266; margin-bottom: 18px;">
          您的{{ registerForm.identity === 'teacher' ? '工号' : '学号' }}为：
          <span style="color: #409eff; font-weight: bold;">{{ userNo }}</span>
        </p>
        <p style="color: #909399; font-size: 14px;">请妥善保管您的账号信息</p>
      </div>
      <template #footer>
        <div class="dialog-footer" style="text-align: center;">
          <el-button type="primary" @click="onSuccessConfirm" style="width: 120px;">确认</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import { register, checkTeacherCert } from '@/api/user'
import { ElMessage } from 'element-plus'
import SlideCaptcha from '@/components/SlideCaptcha'

/**
 * 注册页面组件
 * 提供用户注册功能，包括：
 * - 学生/教师身份选择
 * - 教师注册需要教师资格证验证
 * - 邮箱注册
 * - 滑块验证码验证
 */
export default {
  name: 'Register',
  components: {
    SlideCaptcha
  },
  data() {
    // 验证身份类型规则
    const validateIdentity = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请选择身份类型'))
      } else {
        callback()
      }
    }
    // 验证教师资格证编号规则
    const validateTeacherCertNo = (rule, value, callback) => {
      if (this.registerForm.identity === 'teacher') {
        if (!value) {
          callback(new Error('教师资格证编号不能为空'))
        } else if (!/^\d{17}$/.test(value)) {
          callback(new Error('您输入的编号不合规'))
        } else if (this.teacherCertError) {
          callback(new Error(this.teacherCertError))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }
    // 验证姓名规则
    const validateRealName = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入姓名'))
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
    // 验证确认密码规则
    const validateCheckedPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('输入密码不一致'))
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
    return {
      enableRegister: import.meta.env.VITE_APP_ENABLE_REGISTER === 'true',
      icpNumber: import.meta.env.VITE_APP_ICP_NUMBER,
      icpLink: import.meta.env.VITE_APP_ICP_LINK,
      // 注册表单数据
      registerForm: {
        identity: '',
        teacherCertNo: '',
        realName: '',
        mail: '',
        password: '',
        checkedPassword: ''
      },
      // 是否显示滑块验证码
      showSlideCaptcha: false,
      // 是否显示成功模态框
      showSuccessModal: false,
      // 注册成功后分配的用户编号
      userNo: '',
      // 注册按钮加载状态
      loading: false,
      // 密码输入框显示类型
      passwordType: 'password',
      // 确认密码输入框显示类型
      checkedPasswordType: 'password',
      // 重定向路径
      redirect: undefined,
      // 教师资格证验证错误信息
      teacherCertError: '',
      // 表单验证规则
      loginRules: {
        identity: [{ required: true, trigger: 'change', validator: validateIdentity }],
        teacherCertNo: [{ required: false, trigger: ['blur', 'change'], validator: validateTeacherCertNo }],
        realName: [{ required: true, trigger: 'blur', validator: validateRealName }],
        mail: [{ required: true, trigger: 'blur', validator: validateMail }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        checkedPassword: [{ required: true, trigger: 'blur', validator: validateCheckedPassword }]
      }
    }
  },
  watch: {
    // 监听路由变化，获取重定向路径
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  methods: {
    onIdentityChange() {
      // 身份类型改变时，清空教师证件编号和错误信息
      if (this.registerForm.identity !== 'teacher') {
        this.registerForm.teacherCertNo = ''
        this.teacherCertError = ''
      }
    },
    checkTeacherCert() {
      // 失焦时校验教师证件编号唯一性
      if (this.registerForm.identity === 'teacher' && this.registerForm.teacherCertNo) {
        // 先进行本地验证
        if (!/^\d{17}$/.test(this.registerForm.teacherCertNo)) {
          // 本地验证失败，通过表单验证规则提示
          this.teacherCertError = ''
          this.$refs.registerForm.validateField('teacherCertNo')
          return
        }
        
        // 本地验证通过后，进行服务器验证
        checkTeacherCert({ teacherCertNo: this.registerForm.teacherCertNo }).then(res => {
          console.log('[Register] 校验教师资格证编号响应:', res)
          if (res.code === 1 || res.code === 200) {
            // 验证通过，清空错误信息
            this.teacherCertError = ''
            console.log('[Register] 教师资格证编号验证通过')
          } else {
            // 设置服务器验证错误信息
            this.teacherCertError = res.msg || '该证件已被注册'
            // 触发验证，显示错误信息
            this.$refs.registerForm.validateField('teacherCertNo')
            console.log('[Register] 教师资格证编号验证失败:', this.teacherCertError)
          }
        }).catch(error => {
          console.error('校验教师资格证编号失败:', error)
          // 清空错误信息，避免因网络问题导致错误信息残留
          this.teacherCertError = ''
        })
      }
    },
    registerFn() {
      console.log('[Register] 点击注册按钮')
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          console.log('[Register] 表单验证通过，尝试唤起滑块验证')
          // 触发滑块验证
          this.showSlideCaptcha = true
        } else {
          console.warn('[Register] 表单验证失败')
          ElMessage({
            message: '请填写完整的注册信息',
            type: 'warning',
            duration: 5 * 1000
          })
        }
      })
    },
    onSlideSuccess(verifyToken) {
      console.log('[Register] 滑块验证成功，准备提交注册, verifyToken:', verifyToken)
      // 验证成功，执行注册
      const registerData = {
        identity: this.registerForm.identity,
        teacherCertNo: this.registerForm.teacherCertNo,
        realName: this.registerForm.realName,
        mail: this.registerForm.mail,
        password: this.registerForm.password,
        checkedPassword: this.registerForm.checkedPassword,
        verifyToken: verifyToken
      }
      this.loading = true
      console.log('[Register] 提交注册数据:', registerData)
      register(registerData).then((res) => {
        console.log('[Register] 注册响应数据:', res)
        this.loading = false
        
        // 检查响应数据结构
        if (res && (res.code === 1 || res.code === 200)) { // 只将code为1或200视为成功
          console.log('[Register] 注册成功，响应码:', res.code)
          
          // 确保获取到用户编号，处理不同的数据结构
          let userNo = ''
          if (res.data && res.data.no) {
            userNo = res.data.no
            console.log('[Register] 从 res.data.no 获取用户编号:', userNo)
          } else if (res.data && res.data.data && res.data.data.no) {
            // 兼容嵌套数据结构
            userNo = res.data.data.no
            console.log('[Register] 从 res.data.data.no 获取用户编号:', userNo)
          } else if (res.no) {
            // 兼容直接返回的结构
            userNo = res.no
            console.log('[Register] 从 res.no 获取用户编号:', userNo)
          } else {
            console.error('[Register] 未获取到用户编号:', res)
            // 如果没有获取到用户编号，认为注册失败
            ElMessage({
              message: '注册失败，未获取到用户编号',
              type: 'error',
              duration: 5 * 1000
            })
            return
          }
          
          this.userNo = userNo
          console.log('[Register] 准备显示成功模态框，userNo:', this.userNo)
          
          // 确保显示模态框
          this.showSuccessModal = true
          console.log('[Register] 模态框显示状态设置为:', this.showSuccessModal)
          
          // 强制更新UI
          this.$nextTick(() => {
            console.log('[Register] 强制更新UI后，模态框显示状态:', this.showSuccessModal)
          })
          
        } else {
          console.warn('[Register] 注册失败:', res?.msg || '未知错误')
          ElMessage({
            message: res?.msg || '注册失败',
            type: 'error',
            duration: 5 * 1000
          })
        }
      }).catch((error) => {
        console.error('[Register] 注册请求异常:', error)
        this.loading = false
        if (error.message && error.message.includes('请先通过滑块验证')) {
            console.warn('[Register] 验证过期，重新唤起滑块')
            this.showSlideCaptcha = true
        }
        ElMessage({
          message: error.msg || error.message || '注册失败，请重试',
          type: 'error',
          duration: 5 * 1000
        })
      })
    },
    onSuccessConfirm() {
      // 点击确认按钮后跳转至登录页
      this.showSuccessModal = false
      this.$router.push({ path: '/login' })
    },
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
    showPwd2() {
      if (this.checkedPasswordType === 'password') {
        this.checkedPasswordType = ''
      } else {
        this.checkedPasswordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.checkedPassword.focus()
      })
    }
  }
}
</script>

<style lang="scss">
$bg: #283443;
$light_gray: #fff;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.login-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;
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

  .el-select {
    display: inline-block;
    height: 50px;
    width: 90%;

    .el-select__wrapper {
      background-color: transparent;
      box-shadow: none !important;
      padding: 0 0 0 10px;
      width: 100%;
      height: 100%;

      .el-select__selected-item {
        color: $light_gray;
      }

      .el-select__placeholder {
        color: #889aa4; // $dark_gray
      }
    }

    .el-select__suffix {
      .el-icon {
        color: #889aa4; // $dark_gray
      }
    }

    .el-input {
      width: 100%;

      & input {
        color: $light_gray;
      }

      .el-input__icon {
        color: $dark_gray;
      }
    }

    .el-select-dropdown {
      background-color: $bg;
      border: 1px solid rgba(255, 255, 255, 0.1);

      .el-select-dropdown__item {
        color: $light_gray;

        &:hover,
        &:focus {
          background-color: rgba(255, 255, 255, 0.1);
        }

        &.el-select-dropdown__item.selected {
          color: #66b1ff;
        }
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
  /* ... rest of styles ... */
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
  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
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
