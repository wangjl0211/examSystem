<template>
  <div class="item-contain">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>个人信息</span>
          <el-button
            v-if="data.subjectName != null"
            size="mini"
            style="float: right; padding: 3px 0; margin-right: 15px"
            type="text"
            @click="exitsubject"
          >
            退出课程</el-button>
          <el-button
            type="text"
            size="mini"
            style="float: right; padding: 3px 0; margin-right: 15px"
            @click="fileDialogVisible = true"
          >编辑头像</el-button>
        </div>
      </template>
      <div class="card-body">
        <div class="left">
          <div>
            <span class="label">{{ (data.roleId === 0,1 || data.roleId === 2) ? '工号：' : '学号：' }}</span>
            <span class="value">{{ data.userNo }}</span>
          </div>
          <div>
            <span class="label">姓名：</span>
            <span class="value">{{ data.realName }}</span>
          </div>
        </div>
        <el-dialog
          width="400px"
          :show-close="false"
          :close-on-click-modal="false"
          title="上传头像"
          v-model="fileDialogVisible"
        >
          <el-upload
            class="upload-demo"
            drag
            action="xxxxxx"
            multiple
            :limit="1"
            accept=".png, .jpg, .jpeg, .bmp"
            :auto-upload="false"
            :on-remove="handleRemove"
            :on-change="handleFileChange"
            :file-list="fileList"
          >
            <el-icon class="el-icon-upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传"png", "jpg", "jpeg",
                "bmp"文件，且不超过2MB。注:右上角头像，重新登录更新
              </div>
            </template>
          </el-upload>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="fileDialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="importAvatar">确 定</el-button>
            </div>
          </template>
        </el-dialog>
        <div class="right">
          <img
            style="
              width: 150px;
              height: 150px;
              border-radius: 200px;
            "
            :src="data.avatar"
            alt=""
          >
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { exitUseSubject, getInfo, uploadAvatar } from '@/api/user'
import { getRole } from '@/utils/jwtUtils'
export default {
  data() {
    return {
      fileDialogVisible: false,
      fileList: [],
      data: {},
      isAdmin: false
    }
  },
  created() {
    // 获取角色判断是否是教师和管理员
    const role = getRole()
    if (role === 3 || role === 2) {
      this.isAdmin = true
    }
    this.getInfoFun()
  },
  methods: {
    // 退出课程逻辑
    exitsubject() {
      this.$confirm('退出课程, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          exitUseSubject()
            .then((res) => {
              if (res.code) {
                this.getInfoFun()
                this.$message({
                  type: 'success',
                  message: '退出成功!'
                })
              } else {
                this.$message({
                  type: 'error',
                  message: res.msg
                })
              }
            })
            .catch(() => {
              this.$message({
                type: 'info',
                message: '已取消退出'
              })
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消退出'
          })
        })
    },
    // 获取个人系信息
    async getInfoFun() {
      const res = await getInfo()
      if (res.code) {
        this.data = res.data
      } else {
        this.$message.error('获取个人信息失败')
      }
    },
    // 修改文件逻辑
    handleFileChange(file, fileList) {
      this.fileList = fileList // 收集文件信息
    },
    // 移除文件处理方法
    handleRemove(file, fileList) {
      if (fileList.length === 0) {
        this.hasFiles = false
      }
    },
    // 上传文件逻辑
    importAvatar() {
      if (this.fileList.length > 0) {
        const formData = new FormData() // 创建FormData对象
        formData.append('file', this.fileList[0].raw) // 添加文件到formData
        uploadAvatar(formData)
          .then((res) => {
            if (res.code) {
              this.getInfoFun()
              this.$message.success('文件上传成功！')
              this.fileDialogVisible = false // 关闭对话框
              // 可以在这里处理成功后的逻辑，如刷新数据等
            }
          })
          .catch((error) => {
            console.error('文件上传失败：', error)
            this.$message.error('文件上传失败！')
          })
      } else {
        this.$message.warning('导入失败，请确定题库和文件格式是否正确~')
      }
    },

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
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

.card-body {
  height: 29em;
  display: flex;
  justify-content: space-evenly;
}
.left {
  display: flex;
  flex-direction: column;
  width: 380px;
  height: 100%;
  padding: 60px;
  border-right: 1px solid rgb(228, 232, 235);
  
  & > div {
    margin-bottom: 60px;
    font-size: 18px;
    display: flex;
    align-items: center;
    
    .label {
      display: inline-block;
      width: 80px;
      flex-shrink: 0;
    }
    
    .value {
      display: inline-block;
      flex: 1;
      min-width: 160px; /* 确保至少能显示八个字 */
    }
  }
}
.right{
  padding: 60px;
}

@media (max-width: 991px) {
  .item-contain {
    padding: 15px;
    height: auto;
  }
  .box-card {
    width: 100% !important;
  }
  .card-body {
    flex-direction: column;
    height: auto;
  }
  .left {
    width: 100%;
    padding: 20px;
    border-right: none;
    border-bottom: 1px solid rgb(228, 232, 235);
    & > div {
      margin-bottom: 20px;
    }
  }
  .right {
    padding: 20px;
    text-align: center;
    & img {
      width: 100px !important;
      height: 100px !important;
    }
  }
}
</style>
