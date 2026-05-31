<template>
  <div class="app-container">
    <!-- 课程信息 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程详情</span>
        </div>
      </template>
      <div class="course-info">
        <el-form :model="courseForm" label-width="120px">
          <el-form-item label="课程名称">
            <div style="display: flex; align-items: center; width: 100%;">
              <el-input 
                v-model="courseForm.subjectName" 
                placeholder="输入课程名称" 
                style="flex: 1; margin-right: 10px; min-width: 150px;"
              />
              <el-button 
                type="primary" 
                size="small" 
                @click="updateCourseName"
                style="flex: 0 0 auto; min-width: 80px;min-height: 40px;"
              >
                修改
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 课程用户列表 -->
    <el-card class="box-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>课程用户列表</span>
        </div>
      </template>
      <el-table
        :data="userList"
        border
        fit
        highlight-current-row
        row-key="userId"
        :header-cell-style="{
          background: '#f2f3f4',
          color: '#555',
          'font-weight': 'bold',
          'line-height': '32px',
        }"
      >
        <el-table-column label="序号" align="center" width="80px">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="userNo" label="学号" align="center" />
        <el-table-column prop="realName" label="姓名" align="center" />
        <el-table-column prop="joinTime" label="加入时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作">
          <template #default="{ row }">
            <el-button type="text" size="small" style="color: red; font-size: 14px" @click="removeUser(row)">
              移除用户
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getSubjectDetail, updateSubjectName, removeUserFromSubject } from '@/api/class_'

export default {
  data() {
    return {
      courseId: this.$route.query.id,
      courseForm: {
        subjectName: ''
      },
      userList: []
    }
  },
  created() {
    this.getCourseDetail()
  },
  methods: {
    // 获取课程详情
    async getCourseDetail() {
      const res = await getSubjectDetail(this.courseId)
      if (res.code) {
        this.courseForm = res.data.subject
        this.userList = res.data.userList
      }
    },
    // 修改课程名称
    updateCourseName() {
      updateSubjectName(this.courseId, { subjectName: this.courseForm.subjectName }).then((res) => {
        if (res.code) {
          this.$message({
            type: 'success',
            message: '修改成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    // 移除用户
    removeUser(row) {
      this.$confirm('此操作将该用户从课程中移除, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          removeUserFromSubject(this.courseId, row.userId).then((res) => {
            if (res.code) {
              this.getCourseDetail()
              this.$message({
                type: 'success',
                message: '移除成功!'
              })
            } else {
              this.$message({
                type: 'info',
                message: res.msg
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消移除'
          })
        })
    },
    // 格式化日期时间
    formatDate(date) {
      if (!date) return ''
      // 处理各种时间格式，包括ISO格式的LocalDateTime
      let d
      try {
        if (typeof date === 'string') {
          // 处理ISO格式的时间字符串
          d = new Date(date)
        } else {
          d = new Date(date)
        }
      } catch (error) {
        console.error('日期解析错误:', error)
        return ''
      }
      // 检查日期是否有效
      if (isNaN(d.getTime())) {
        return ''
      }
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
  }
}
</script>

<style scoped>
.box-card {
  width: 100%;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.course-info {
  margin-top: 20px;
}
</style>