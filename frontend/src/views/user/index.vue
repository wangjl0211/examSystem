<template>
  <div class="app-container">
    <!-- 筛选栏 -->
    <el-form :inline="true" :model="searchForm" class="demo-form-inline">
      <el-form-item label="学号/工号">
        <el-input v-model="searchForm.searchUserNo" placeholder="输入学号/工号" />
      </el-form-item>
      <el-form-item label="真实姓名">
        <el-input v-model="searchForm.searchRealName" placeholder="输入姓名" />
      </el-form-item>
      <el-form-item label="注册日期">
        <el-date-picker
          v-model="searchForm.searchDate"
          type="date"
          placeholder="选择注册日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item label="用户角色">
        <el-select v-model="searchForm.searchRoleId" placeholder="选择用户角色">
          <el-option label="教师" value="1" />
          <el-option label="学生" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchUser">查询</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="data.records" border fit highlight-current-row :header-cell-style="{
      background: '#f2f3f4',
      color: '#555',
      'font-weight': 'bold',
      'line-height': '32px',
    }">
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column label="序号" align="center" width="80px">
        <template #default="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="userNo" label="学号" align="center" />
      <el-table-column prop="realName" label="真实姓名" align="center" />
      <el-table-column prop="roleId" label="角色名称" align="center" >
      <template #default="{ row }">
        <span v-if="row.roleId == 2">学生</span>
        <span v-if="row.roleId == 1">教师</span>
        <span v-if="row.roleId == 0">管理员</span>
      </template>
      </el-table-column>

      <el-table-column label="注册时间" align="center">
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作">
        <template #default="{ row }">
          <el-button v-if="role == 'admin'" type="text" size="small" style="color: red; font-size: 14px"
            @click="delUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>



    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination :current-page="data.current" :page-sizes="[10, 20, 30, 40]" :page-size="data.size"
        layout="total, sizes, prev, pager, next, jumper" :total="data.total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script>
import { userPaging, userDel } from '@/api/user'
import { getRoleFromStorage } from '@/utils/auth'

export default {

  data() {
    return {
      // 当前登录角色
      role: '',
      pageNum: 1,
      pageSize: 20,
      // 分页查找的数据
      data: {},
      // 筛选栏表单
      searchForm: {
        searchUserNo: '',
        searchRealName: '',
        searchDate: '',
        searchRoleId: ''
      },
       
      formLabelWidth: '80px'
    }
  },
  created() {
    // 获取用户角色（使用正确的key从localStorage获取）
    this.role = getRoleFromStorage()
    console.log('[用户管理] 读取到的角色:', this.role)
    
    // 从localStorage加载之前的搜索条件，实现记忆功能
    const savedSearchParams = localStorage.getItem('userSearchParams')
    if (savedSearchParams) {
      this.searchForm = JSON.parse(savedSearchParams)
    }
    // 获取分页数据
    this.getUserPage()
  },
  methods: {
    // 分页查询用户
    async getUserPage(pageNum, pageSize, searchParams = null) {
      // 如果没有传递searchParams，使用当前searchForm的值
      const params = {
        pageNum: pageNum || this.pageNum,
        pageSize: pageSize || this.pageSize,
        userNo: searchParams?.searchUserNo || this.searchForm.searchUserNo,
        realName: searchParams?.searchRealName || this.searchForm.searchRealName,
        createDate: searchParams?.searchDate || this.searchForm.searchDate,
        roleId: searchParams?.searchRoleId || this.searchForm.searchRoleId
      }
      const res = await userPaging(params)
      this.data = res.data
    },
    // 搜索功能用户
    searchUser() {
      // 保存查询条件到localStorage，实现记忆功能
      localStorage.setItem('userSearchParams', JSON.stringify(this.searchForm))
      this.getUserPage(this.pageNum, this.pageSize, this.searchForm)
    },
    // 重置筛选条件
    resetForm() {
      this.searchForm = {
        searchUserNo: '',
        searchRealName: '',
        searchDate: '',
        searchRoleId: ''
      }
      // 清除localStorage中的查询条件
      localStorage.removeItem('userSearchParams')
      this.getUserPage(this.pageNum, this.pageSize, this.searchForm)
    },
    // 设置每页多少条逻辑
    handleSizeChange(val) {
      this.pageSize = val
      this.getUserPage(this.pageNum, val, this.searchForm)
    },
    // 设置当前页逻辑
    handleCurrentChange(val) {
      this.pageNum = val
      this.getUserPage(val, this.pageSize, this.searchForm)
    },

    // 删除用户方法
    delUser(row) {
      this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          userDel(row.id).then((res) => {
            if (res.code) {
              // 刷新页面数据
              this.getUserPage(this.pageNum, this.pageSize)
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
    }
  }
}
</script>
<style>
.el-table--border,
.el-table--group {
  border: 1px solid #b3b3b3;
}
</style>
