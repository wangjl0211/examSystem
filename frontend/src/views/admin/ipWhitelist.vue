<template>
  <div class="ip-whitelist-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>IP白名单管理</h2>
      <p class="description">管理允许访问管理员接口的IP地址列表</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加规则
      </el-button>
      <el-button @click="handleRefreshCache">
        <el-icon><Refresh /></el-icon>
        刷新缓存
      </el-button>
      <el-button @click="loadData">
        <el-icon><RefreshRight /></el-icon>
        刷新列表
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="ipAddress" label="IP地址/网段" min-width="150">
        <template #default="{ row }">
          <el-tag type="info">{{ row.ipAddress }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ipType" label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getIpTypeTag(row.ipType)">
            {{ getIpTypeLabel(row.ipType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="150" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            编辑
          </el-button>
          <el-popconfirm
            title="确定要删除这条规则吗？"
            @confirm="handleDelete(row)"
          >
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="IP类型" prop="ipType">
          <el-select v-model="form.ipType" placeholder="请选择IP类型" style="width: 100%">
            <el-option label="单个IP" :value="1" />
            <el-option label="CIDR格式" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input
            v-model="form.ipAddress"
            :placeholder="ipTypePlaceholder"
          />
          <div class="form-tip">
            {{ ipTypeTip }}
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Plus, Refresh, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getWhitelistPage,
  addWhitelist,
  updateWhitelist,
  deleteWhitelist,
  toggleWhitelistStatus,
  refreshWhitelistCache
} from '@/api/admin'

export default {
  name: 'IpWhitelist',
  components: {
    Plus,
    Refresh,
    RefreshRight
  },
  data() {
    // IP地址验证规则
    const validateIpAddress = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入IP地址'))
        return
      }

      const ipType = this.form.ipType

      if (ipType === 1) {
        // 单个IP格式验证
        const ipRegex = /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/
        if (!ipRegex.test(value)) {
          callback(new Error('请输入正确的IP地址格式'))
          return
        }
      } else if (ipType === 3) {
        // CIDR格式验证
        const cidrRegex = /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)\/(\d|[12]\d|3[0-2])$/
        if (!cidrRegex.test(value)) {
          callback(new Error('请输入正确的CIDR格式，如：192.168.1.0/24'))
          return
        }
      }

      callback()
    }

    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '添加IP白名单规则',
      isEdit: false,
      form: {
        id: null,
        ipAddress: '',
        ipType: 1,
        description: ''
      },
      rules: {
        ipType: [
          { required: true, message: '请选择IP类型', trigger: 'change' }
        ],
        ipAddress: [
          { required: true, validator: validateIpAddress, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ipTypePlaceholder() {
      return this.form.ipType === 1 ? '请输入IP地址，如：192.168.1.100' : '请输入CIDR格式，如：192.168.1.0/24'
    },
    ipTypeTip() {
      return this.form.ipType === 1
        ? '单个IP地址格式：xxx.xxx.xxx.xxx'
        : 'CIDR格式：xxx.xxx.xxx.xxx/xx，如：192.168.0.0/16 表示192.168.x.x的所有IP'
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        const res = await getWhitelistPage({
          page: this.pagination.page,
          size: this.pagination.size
        })
        if (res.code === 1) {
          this.tableData = res.data.records || []
          this.pagination.total = res.data.total || 0
        } else {
          ElMessage.error(res.msg || '获取数据失败')
        }
      } catch (error) {
        console.error('获取IP白名单列表失败:', error)
        ElMessage.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },

    // 获取IP类型标签
    getIpTypeLabel(type) {
      const typeMap = {
        1: '单个IP',
        2: '网段',
        3: 'CIDR'
      }
      return typeMap[type] || '未知'
    },

    // 获取IP类型标签样式
    getIpTypeTag(type) {
      const tagMap = {
        1: '',
        2: 'warning',
        3: 'success'
      }
      return tagMap[type] || 'info'
    },

    // 添加规则
    handleAdd() {
      this.dialogTitle = '添加IP白名单规则'
      this.isEdit = false
      this.dialogVisible = true
    },

    // 编辑规则
    handleEdit(row) {
      this.dialogTitle = '编辑IP白名单规则'
      this.isEdit = true
      this.form = {
        id: row.id,
        ipAddress: row.ipAddress,
        ipType: row.ipType,
        description: row.description
      }
      this.dialogVisible = true
    },

    // 删除规则
    async handleDelete(row) {
      try {
        const res = await deleteWhitelist(row.id)
        if (res.code === 1) {
          ElMessage.success('删除成功')
          this.loadData()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除IP白名单规则失败:', error)
        ElMessage.error('删除失败')
      }
    },

    // 状态变更
    async handleStatusChange(row) {
      try {
        const res = await toggleWhitelistStatus(row.id, row.status)
        if (res.code === 1) {
          ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
        } else {
          ElMessage.error(res.msg || '操作失败')
          // 回滚状态
          row.status = row.status === 1 ? 0 : 1
        }
      } catch (error) {
        console.error('更新状态失败:', error)
        ElMessage.error('操作失败')
        // 回滚状态
        row.status = row.status === 1 ? 0 : 1
      }
    },

    // 提交表单
    async handleSubmit() {
      try {
        const valid = await this.$refs.formRef.validate()
        if (!valid) return

        this.submitLoading = true

        let res
        if (this.isEdit) {
          res = await updateWhitelist(this.form)
        } else {
          res = await addWhitelist(this.form)
        }

        if (res.code === 1) {
          ElMessage.success(this.isEdit ? '更新成功' : '添加成功')
          this.dialogVisible = false
          this.loadData()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('提交表单失败:', error)
        ElMessage.error('操作失败')
      } finally {
        this.submitLoading = false
      }
    },

    // 刷新缓存
    async handleRefreshCache() {
      try {
        const res = await refreshWhitelistCache()
        if (res.code === 1) {
          ElMessage.success('缓存刷新成功')
        } else {
          ElMessage.error(res.msg || '刷新失败')
        }
      } catch (error) {
        console.error('刷新缓存失败:', error)
        ElMessage.error('刷新失败')
      }
    },

    // 重置表单
    resetForm() {
      this.form = {
        id: null,
        ipAddress: '',
        ipType: 1,
        description: ''
      }
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields()
      }
    },

    // 分页大小变更
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.loadData()
    },

    // 页码变更
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.ip-whitelist-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px 0;
      font-size: 20px;
      color: #303133;
    }

    .description {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .action-bar {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  :deep(.el-table) {
    .el-tag {
      font-family: monospace;
    }
  }
}
</style>
