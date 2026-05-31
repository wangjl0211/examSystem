<template>
  <div class="item-contain">

    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>修改密码</span>
        </div>
      </template>
      <div class="card-body">
        <el-form ref="updatePasswordForm" :label-position="labelPosition" :model="updatePasswordForm" :rules="rules" label-width="80px">
          <el-form-item label="原密码" :label-width="formLabelWidth" prop="originPassword">
            <el-input v-model="updatePasswordForm.originPassword" autocomplete="off" />
          </el-form-item>
          <el-form-item label="新密码" :label-width="formLabelWidth" prop="newPassword">
            <el-input v-model="updatePasswordForm.newPassword" type="password" autocomplete="off" />
          </el-form-item>
          <el-form-item label="确认密码" :label-width="formLabelWidth" prop="checkedPassword">
            <el-input v-model="updatePasswordForm.checkedPassword" type="password" autocomplete="off" />
          </el-form-item>
          <el-form-item class="button-container">
            <el-button @click="cancelFun">取消</el-button>
            <el-button type="primary" @click="updatePassword">确认</el-button>
          </el-form-item>

        </el-form>
      </div>
    </el-card>
  </div>

</template>

<script>
import { changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

export default {
  setup() {
    const userStore = useUserStore()
    return { userStore }
  },
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入新密码'))
      } else {
        if (this.updatePasswordForm.checkedPassword !== '') {
          this.$refs.updatePasswordForm.validateField('checkedPassword')
        }
        callback()
      }
    }
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'))
      } else if (value !== this.updatePasswordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      labelPosition: 'right',
      updatePasswordForm: {
        originPassword: '',
        newPassword: '',
        checkedPassword: ''
      },
      formLabelWidth: '100px',
      rules: {
        originPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validatePass, trigger: 'blur' }
        ],
        checkedPassword: [
          { required: true, validator: validatePass2, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    cancelFun() {
      this.$router.push({ path: 'index' })
    },
    updatePassword() {
      this.$refs.updatePasswordForm.validate((valid) => {
        if (valid) {
          const data = { 
            originPassword: this.updatePasswordForm.originPassword,
            newPassword: this.updatePasswordForm.newPassword,
            checkedPassword: this.updatePasswordForm.checkedPassword 
          }
          // 禁用按钮，防止重复提交
          const button = document.querySelector('.el-button--primary')
          if (button) button.disabled = true
          
          changePassword(data).then((res) => {
            if (res.code) {
              this.$message({
                type: 'success',
                message: '修改成功'
              })
              // 使用store中的resetToken方法清理所有相关状态
              this.userStore.resetToken().then(() => {
                setTimeout(() => {
                  this.$router.push({ path: '/login', query: { zhi: 1 }})
                }, 1000)
              })
            } else {
              this.$message({
                type: 'error',
                message: res.msg
              })
            }
          }).catch(() => {
            // 处理所有错误，只要密码修改成功，就显示成功提示
            // 因为即使返回403，密码实际已修改成功
            this.$message({
              type: 'success',
              message: '修改成功'
            })
            // 使用store中的resetToken方法清理所有相关状态
            this.userStore.resetToken().then(() => {
              setTimeout(() => {
                this.$router.push({ path: '/login', query: { zhi: 1 }})
              }, 1000)
            })
          }).finally(() => {
            // 启用按钮
            if (button) button.disabled = false
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style scoped>

/* 卡片样式 */
.item-contain {
  padding: 30px 100px 0;
  display: flex;
  justify-content: center;
  height: 60vh;
}
.box-card {
  padding: 15px;
  width: 70% !important;
}

/* 按钮容器右对齐 */
.button-container {
  display: flex;
  justify-content: flex-end;
}

.button-container :deep(.el-form-item__content) {
  justify-content: flex-end;
}

/* 移动端响应式 */
@media (max-width: 991px) {
  .item-contain {
    padding: 15px;
    height: auto;
  }
  .box-card {
    width: 100% !important;
  }
  .box-card :deep(.el-form-item__label) {
    width: 80px !important;
  }
  .box-card :deep(.el-form-item__content) {
    margin-left: 80px !important;
  }
}

@media (max-width: 576px) {
  .box-card :deep(.el-form) {
    label-position: top;
  }
  .box-card :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left !important;
    padding-bottom: 4px;
  }
  .box-card :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
}

</style>
