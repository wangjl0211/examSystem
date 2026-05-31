<template>
  <div class="app-container">
    <!-- form -->

    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="课程名称">
        <el-input v-model="formInline.searchTitle" placeholder="输入课程名称" />
      </el-form-item>
      <el-form-item label="创建用户">
        <el-input v-model="formInline.searchUserName" placeholder="输入创建用户" />
      </el-form-item>
      <el-form-item label="创建日期">
        <el-date-picker
          v-model="formInline.searchDate"
          type="date"
          placeholder="选择创建日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchExam">查询</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button v-if="role==0 || role==1" :title="diaTitle" type="primary" @click="dialogTableVisible = true">
          创建课程</el-button>
        <el-button v-if="role==2" type="primary" @click="joinClassVisible = true">
          加入课程</el-button>
      </el-form-item>
    </el-form>

    <!-- table -->

    <el-table
      :data="data.records"
      border
      fit
      highlight-current-row
      row-key="id"
      :header-cell-style="{
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px',
      }"
    >
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column label="序号" align="center" width="80px">
        <template #default="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="subjectName" label="课程名称" align="center" />
      <el-table-column prop="subjectCount" label="课程人数" align="center" />
      <el-table-column v-if="role==0 || role==1" prop="code" label="课程口令" align="center" />
      <el-table-column prop="userName" label="创建用户" align="center" />
      <el-table-column v-if="role==0 || role==1" prop="createTime" label="创建时间" align="center">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column v-if="role==2" prop="joinTime" label="加入时间" align="center">
        <template #default="scope">
          {{ formatDate(scope.row.joinTime) }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作">
        <template #default="{ row }">
          <div class="btn-group">
            <!-- 教师按钮 -->
            <el-button
              v-if="role==1"
              type="text"
              size="small"
              style="font-size: 14px"
              @click="updateRow(row)"
            >管理</el-button>

            <el-button
              v-if="role==0 || role==1"
              type="text"
              size="small"
              style="color: red; font-size: 14px"
              @click="delClass(row)"
            >删除</el-button>
            <!-- 学生按钮 -->
            <el-button
              v-if="role==2"
              type="text"
              size="small"
              style="color: red; font-size: 14px"
              @click="exitClass(row)"
            >退出课程</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="data.current"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="data.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="data.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!--新增弹窗-->
    <el-dialog :title="diaTitle" v-model="dialogTableVisible" width="35%">
      <el-row>
        <el-form :model="addForm">
          <el-form-item label="课程名称" :label-width="formLabelWidth" :error="addFormError.subjectName">
            <el-input v-model="addForm.subjectName" autocomplete="off">
              <template #append>
                <el-button type="primary" @click="addClass">确 定</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </el-row>
    </el-dialog>

    <el-dialog title="加入课程" v-model="joinClassVisible" width="35%">
      <el-row>
        <el-form :model="teacharForm">
          <el-form-item label="课程代码：" :label-width="formLabelWidth">
            <el-input v-model="teacharForm.classCode" autocomplete="off">
              <template #append>
                <el-button type="primary" @click="joinClass">确 定</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </el-row>
    </el-dialog>

    <!--编辑弹窗-->
    <el-dialog title="编辑" v-model="dialogFormVisible" width="35%">
      <el-row>
        <el-form :model="form">
          <el-form-item label="课程名称" :label-width="formLabelWidth">
            <el-input v-model="form.subjectName" autocomplete="off">
              <template #append>
                <el-button type="primary" @click="updateClass">确 定</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </el-row>
    </el-dialog>
  </div>
</template>

<script>
import { joinSubject, exitSubject, classPaging, classDel, classUpdate, classAdd } from '@/api/class_'
import { getRole } from '@/utils/jwtUtils'
export default {
  data() {
    return {
      teacharForm: {
        classCode: ''
      },
      role: 0,
      pageNum: 1,
    pageSize: 20,
      data: {},
      diaTitle: '创建课程',
      joinClassVisible: false,
      dialogTableVisible: false,
      dialogFormVisible: false,
      addForm: {
        subjectName: ''
      },
      addFormError: {
        subjectName: ''
      },
      formInline: {
        searchTitle: '',
        searchUserName: '',
        searchDate: ''
      },
      form: {
        subjectName: ''
      },
      formLabelWidth: '110px'
    }
  },

  created() {
    this.role = getRole()
    // 从localStorage加载之前的搜索条件，实现记忆功能
    const savedSearchParams = localStorage.getItem('classSearchParams')
    if (savedSearchParams) {
      this.formInline = JSON.parse(savedSearchParams)
    }
    // 获取分页数据
    this.getClassPage()
  },
  methods: {
    // 格式化日期时间
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    joinClass() {
      const params = { code: this.teacharForm.classCode }
      joinSubject(params).then((res) => {
        if (res.code) {
          this.joinClassVisible = false
          this.getClassPage(this.pageNum, this.pageSize, this.formInline)
          this.$message({
            type: 'success',
            message: '加入成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    exitClass(row) {
      const classId = row['id']
      exitSubject(classId).then((res) => {
        if (res.code) {
          this.getClassPage(this.pageNum, this.pageSize, this.formInline)
          this.$message({
            type: 'success',
            message: '退出成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    // 分页查询
    async getClassPage(pageNum, pageSize, searchParams = null) {
      // 如果没有传递searchParams，使用当前formInline的值
      const params = {
        pageNum: pageNum || this.pageNum,
        pageSize: pageSize || this.pageSize || 20,
        subjectName: searchParams?.searchTitle || this.formInline.searchTitle,
        userName: searchParams?.searchUserName || this.formInline.searchUserName,
        createDate: searchParams?.searchDate || this.formInline.searchDate
      }
      const res = await classPaging(params)
      this.data = res.data
    },
    addClass() {
      // 非空验证
      if (!this.addForm.subjectName || this.addForm.subjectName.trim() === '') {
        this.addFormError.subjectName = '请输入课程名称'
        return
      }
      
      // 验证通过，清空错误信息
      this.addFormError.subjectName = ''
      
      const data = { subjectName: this.addForm.subjectName }
      classAdd(data).then((res) => {
        if (res.code) {
          this.addForm.subjectName = ''
          // 清空搜索条件，确保能看到所有课程，包括刚创建或恢复的课程
          this.formInline = {
            searchTitle: '',
            searchUserName: '',
            searchDate: ''
          }
          // 清除localStorage中的查询条件
          localStorage.removeItem('classSearchParams')
          this.getClassPage(this.pageNum, this.pageSize, this.formInline)
          this.dialogTableVisible = false
          this.$message({
            type: 'success',
            message: res.msg || '新增成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    delClass(row) {
      this.$confirm('此操作将永久删除该课程, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          classDel(row.id).then((res) => {
            if (res.code) {
              this.getClassPage(this.pageNum, this.pageSize, this.formInline)
              this.$message({
                type: 'success',
                message: '删除成功!'
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
            message: '已取消删除'
          })
        })
    },
    updateClass() {
      classUpdate(this.form.id, { subjectName: this.form.subjectName })
        .then((res) => {
          if (res.code) {
            this.getClassPage(this.pageNum, this.pageSize, this.formInline)
            this.dialogFormVisible = false
            this.$message({
              type: 'success',
              message: '编辑成功!'
            })
          } else {
            this.$message({
              type: 'info',
              message: res.msg
            })
          }
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '取消编辑'
          })
        })
    },
    updateRow(row) {
      this.$router.push({
        path: '/class-detail',
        query: { id: row.id }
      })
    },
    searchExam() {
      // 保存查询条件到localStorage，实现记忆功能
      localStorage.setItem('classSearchParams', JSON.stringify(this.formInline))
      this.getClassPage(this.pageNum, this.pageSize, this.formInline)
    },
    // 重置筛选条件
    resetForm() {
      this.formInline = {
        searchTitle: '',
        searchUserName: '',
        searchDate: ''
      }
      // 清除localStorage中的查询条件
      localStorage.removeItem('classSearchParams')
      this.getClassPage(this.pageNum, this.pageSize, this.formInline)
    },
    handleClick(row) {
      (row)
    },
    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val
      this.getClassPage(this.pageNum, val, this.formInline)
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val
      this.getClassPage(val, this.pageSize, this.formInline)
    }
  }
}
</script>

<style>
.el-table--border,
.el-table--group {
  border: 1px solid #b3b3b3;
}

.bj {
  margin-top: 40px;
  margin-left: 30px;
}

/* 移动端响应式 */
@media (max-width: 991px) {
  /* 表格横向滚动 */
  .el-table {
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .bj {
    margin-top: 20px;
    margin-left: 0;
  }
}
</style>
