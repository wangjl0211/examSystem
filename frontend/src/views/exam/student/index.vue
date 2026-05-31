<template>
  <div class="app-container">
    <el-form :inline="true" :model="formInline">
      <el-form-item label="试卷名称：      ">
        <el-input v-model="searchTitle" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchExamStu">查询</el-button>
      </el-form-item>
    </el-form>
    <div class="sort-switch-container">
      <span class="sort-label">创建时间：</span>
      <el-switch
        v-model="isASC"
        active-text="升序"
        inactive-text="降序"
        active-color="#13ce66"
        inactive-color="#409EFF"
        @change="toggleSort"
      />
    </div>

    <!-- table -->
    <el-table
      v-loading="loading"
      :data="data.records"
      border
      fit
      highlight-current-row
      :header-cell-style="{
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px',
      }"
    >
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column fixed label="序号" align="center" width="80">
        <template #default="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="title" label="试卷名称" align="center" />
      <el-table-column prop="examDuration" label="考试时长（分钟）" align="center" />
      <el-table-column prop="grossScore" label="总分" align="center" />
      <el-table-column prop="passedScore" label="及格分" align="center" />
      <el-table-column label="考试时间" align="center">
        <template #default="scope">
          {{ scope.row.startTime }} - {{ scope.row.endTime }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" align="center" width="120">
        <template #default="{ row }">
          <el-button
            :type="getExamStatus(row).type"
            :disabled="getExamStatus(row).disabled"
            plain
            size="small"
            @click="screenInfo(row)"
            :class="{
              'status-not-started': getExamStatus(row).text === '未开始',
              'status-in-progress': getExamStatus(row).text === '开始考试' || getExamStatus(row).text === '正在考试',
              'status-pending': getExamStatus(row).text === '待批阅',
              'status-completed': getExamStatus(row).text === '查看分数' || getExamStatus(row).text === '已结束'
            }"
          >{{ getExamStatus(row).text }}</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- 确认对话框 -->
    <el-dialog
      title="确认进入考试"
      v-model="confirmDialogVisible"
      width="600px"
      center
    >
      <div class="exam-info-container">
        <div class="exam-warning">
          <i class="el-icon-warning"></i>
          <span>点击"确定"后将自动进入考试，考试将立即开始计时，请诚信考试！</span>
        </div>
        
        <div class="exam-info-content">
          <div class="info-row">
            <span class="info-label">考试名称:</span>
            <span class="info-value">{{ currentRow?.title || '' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">考试时长:</span>
            <span class="info-value">{{ currentRow?.examDuration ? (currentRow.examDuration + ' 分钟') : '' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">试卷总分:</span>
            <span class="info-value">{{ currentRow?.grossScore || 0 }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">及格分数:</span>
            <span class="info-value">{{ currentRow?.passedScore || 0 }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">考试时间:</span>
            <span class="info-value">{{ currentRow?.startTime }} - {{ currentRow?.endTime }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">单选题:</span>
            <span class="info-value">{{ currentRow?.radioCount || 0 }} 题 ({{ currentRow?.radioScore || 0 }}分)</span>
          </div>
          <div class="info-row">
            <span class="info-label">多选题:</span>
            <span class="info-value">{{ currentRow?.multiCount || 0 }} 题 ({{ currentRow?.multiScore || 0 }}分)</span>
          </div>
          <div class="info-row">
            <span class="info-label">判断题:</span>
            <span class="info-value">{{ currentRow?.judgeCount || 0 }} 题 ({{ currentRow?.judgeScore || 0 }}分)</span>
          </div>
          <div class="info-row">
            <span class="info-label">简答题:</span>
            <span class="info-value">{{ currentRow?.saqCount || 0 }} 题 ({{ currentRow?.saqScore || 0 }}分)</span>
          </div>
        </div>
        
        <div class="exam-notice">
          <p>请确保网络连接稳定，并在规定时间内完成考试。</p>
          <p>考试过程中请勿切换页面，否则可能被系统判定为作弊。</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEnterExam">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getsubjectExamList, examStart } from '@/api/exam'
export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      searchTitle: '',
      isASC: false, // 默认为降序
      loading: false,
      confirmDialogVisible: false,
      currentRow: null,
      formInline: {
        user: '',
        region: ''
      },
      dialogTableVisible: false,
      dialogFormVisible: false,
      form: {
        name: '',
        region: '',
        date1: '',
        date2: '',
        delivery: false,
        type: [],
        resource: '',
        desc: ''
      },
      formLabelWidth: '120px'
    }
  },
  created() {
    this.getExamsubjectPage()
  },
  methods: {
    // 分页查询
    async getExamsubjectPage(pageNum, pageSize, searchTitle = null) {
      this.loading = true
      try {
        const params = { pageNum: pageNum || this.pageNum, pageSize: pageSize || this.pageSize, title: searchTitle || this.searchTitle, isASC: this.isASC }
        const res = await getsubjectExamList(params)
        if (res.code) {
          this.data = res.data
        } else {
          this.$message.error('获取考试列表失败: ' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('获取考试列表异常:', error)
        this.$message.error('网络错误，获取考试列表失败')
      } finally {
        this.loading = false
      }
    },

    // 切换排序方式
    toggleSort() {
      this.getExamsubjectPage(this.pageNum, this.pageSize, this.searchTitle)
    },

    // 考试状态判断
    getExamStatus(row) {
      const now = new Date().getTime()
      const endTime = new Date(row.endTime).getTime()
      const startTime = new Date(row.startTime).getTime()

      if (now > endTime) {
        // 考试已结束，检查是否已提交
        if (row.state === 1) {
          // 已提交，检查是否包含简答题且未评分
          const hasSaq = row.saqCount && row.saqCount > 0
          
          if (hasSaq && row.whetherMark !== 1) {
            // 包含简答题且未评分，显示待批阅
            return {
              text: '待批阅',
              type: 'info',
              disabled: true,
              status: 'pending-review'
            }
          } else {
            // 无简答题或已评分，显示查看分数
            return {
              text: '查看分数',
              type: 'primary',
              disabled: false,
              status: 'completed'
            }
          }
        } else {
          // 未提交，显示已结束
          return {
            text: '已结束',
            type: 'info',
            disabled: true,
            status: 'completed'
          }
        }
      } else if (now < startTime) {
        // 考试未开始
        return {
          text: '未开始',
          type: 'warning',
          disabled: true,
          status: 'not-started'
        }
      } else {
        // 考试进行中，检查是否已提交
        if (row.state === 1) {
          // 已提交，显示待批阅
          return {
            text: '待批阅',
            type: 'info',
            disabled: true,
            status: 'pending-review'
          }
        } else if (row.state === 0 || row.state === '0') {
          // 服务端已有进行中记录（state=0），以数据库为准，不依赖本机 localStorage
          return {
            text: '正在考试',
            type: 'danger',
            disabled: false,
            status: 'in-progress-continue'
          }
        } else {
          // state 为 null：尚未开考
          return {
            text: '开始考试',
            type: 'primary',
            disabled: false,
            status: 'in-progress-start'
          }
        }
      }
    },
    searchExamStu() {
      this.getExamsubjectPage(this.pageNum, this.pageSize, this.searchTitle)
    },
    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val
      this.getExamsubjectPage(this.pageNum, val,this.searchTitle)
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val
      this.getExamsubjectPage(val, this.pageSize,this.searchTitle)
    },
    // 检查网络连接状态
    checkNetworkConnection() {
      return navigator.onLine
    },
    
    // 处理操作按钮点击
    screenInfo(row) {
      const status = this.getExamStatus(row)
      if (status.disabled) {
        return
      }
      
      // 检查网络连接状态
      if (!this.checkNetworkConnection()) {
        this.$message.error('网络连接不稳定，请检查网络后重试')
        return
      }
      
      if (status.status === 'in-progress-start') {
        // 首次进入考试，开始考试
        this.currentRow = row
        this.confirmDialogVisible = true
      } else if (status.status === 'in-progress-continue') {
        // 再次进入考试，继续考试（恢复之前的答题进度）
        this.continueExam(row)
      } else if (status.status === 'completed' && status.text === '查看分数') {
        localStorage.setItem('record_exam_examId', row.id)
        this.$router.push({ name: 'exam-record-detail', query: { zhi: row }})
      }
    },
    
    // 继续考试（恢复答题进度）
    continueExam(row) {
      try {
        // 保存考试信息
        localStorage.setItem('examInfo_examId', row.id)
        
        // 检查考试会话是否过期
        const now = new Date().getTime()
        const endTime = new Date(row.endTime).getTime()
        
        if (now > endTime) {
          this.$message.error('考试时间已结束，无法继续考试')
          this.getExamsubjectPage()
          return
        }
        
        // 检查是否已提交
        if (row.state === 1) {
          this.$message.error('考试已提交，无法继续答题')
          this.getExamsubjectPage()
          return
        }
        
        // 导航到考试页面（会自动恢复之前的答题进度）
        this.$router.push({ name: 'start-exam', params: { id: row.id }})
      } catch (error) {
        console.error('继续考试异常:', error)
        this.$message.error('继续考试失败，请重试')
      }
    },
    
    // 确认进入考试
    confirmEnterExam() {
      if (!this.currentRow) {
        return
      }
      
      const row = this.currentRow
      this.confirmDialogVisible = false
      
      // 再次检查网络连接
      if (!this.checkNetworkConnection()) {
        this.$message.error('网络连接不稳定，请检查网络后重试')
        return
      }
      
      // 检查考试时间是否有效
      const now = new Date().getTime()
      const endTime = new Date(row.endTime).getTime()
      const startTime = new Date(row.startTime).getTime()
      
      if (now < startTime) {
        this.$message.error('考试尚未开始')
        this.getExamsubjectPage()
        return
      }
      
      if (now > endTime) {
        this.$message.error('考试时间已结束')
        this.getExamsubjectPage()
        return
      }
      
      // 检查是否已提交
      if (row.state === 1) {
        this.$message.error('考试已提交，无法再次进入')
        this.getExamsubjectPage()
        return
      }
      
      // 开始考试
      localStorage.setItem('examInfo_examId', row.id)
      
      // 调用后端API开始考试
      examStart(row.id).then((res) => {
        if (res.code) {
          // 记录用户已点击开始考试（本机标记，辅助展示）
          this.recordExamStarted(row.id)
          const isResume = res.msg && res.msg.includes('继续')
          if (isResume) {
            this.$message.success('继续考试')
          }
          console.log('开始考试API调用成功，准备跳转到考试页面，考试ID:', row.id)
          
          // 直接跳转到答题页面，不再经过考试信息页面
          this.$router.push({ name: 'start-exam', params: { id: row.id }}).then(() => {
            console.log('路由跳转成功')
          }).catch((error) => {
            console.error('路由跳转失败:', error)
            this.$message.error('页面跳转失败，请刷新页面后重试')
            // 备用跳转方案：使用URL跳转
            window.location.href = `/start-exam/${row.id}`
          })
        } else {
          this.$message.error('开始考试失败: ' + (res.msg || '未知错误'))
        }
      }).catch((error) => {
        console.error('开始考试异常:', error)
        this.$message.error('网络错误，开始考试失败，请检查网络后重试')
      })
    },
    
    // 记录用户已点击开始考试
    recordExamStarted(examId) {
      // 从localStorage获取已开始的考试列表
      let startedExams = localStorage.getItem('startedExams')
      if (startedExams) {
        startedExams = JSON.parse(startedExams)
      } else {
        startedExams = []
      }
      
      // 如果该考试未在列表中，则添加
      if (!startedExams.includes(examId)) {
        startedExams.push(examId)
        localStorage.setItem('startedExams', JSON.stringify(startedExams))
      }
    }
  }
}
</script>

<style>
.el-table .cell {
  white-space: nowrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.el-table .cell {
  white-space: nowrap;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.sort-switch-container {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

/* 状态按钮样式 */
.status-not-started {
  opacity: 0.6;
  cursor: not-allowed !important;
}

.status-in-progress {
  transition: all 0.3s ease;
  position: relative;
}

.status-in-progress:not(:disabled) {
  cursor: pointer;
}

.status-in-progress:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
  filter: brightness(1.1);
}

.status-in-progress:not(:disabled):active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
  filter: brightness(0.95);
}

.status-pending {
  opacity: 0.6;
  cursor: not-allowed !important;
}

.status-completed {
  transition: all 0.3s ease;
  position: relative;
}

.status-completed:not(:disabled) {
  cursor: pointer;
}

.status-completed:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
  filter: brightness(1.1);
}

.status-completed:not(:disabled):active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
  filter: brightness(0.95);
}

/* 确认对话框样式 */
.exam-info-container {
  padding: 10px 0;
}

.exam-warning {
  background-color: #fef0f0;
  color: #e43b3b;
  padding: 12px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.exam-warning i {
  margin-right: 8px;
  font-size: 16px;
}

.exam-info-content {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.exam-notice {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 12px 15px;
  border-radius: 4px;
  font-size: 13px;
}

.exam-notice p {
  margin: 5px 0;
  line-height: 1.6;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table-column {
    padding: 0 5px;
  }
  
  .pagination-container {
    margin-top: 15px;
    font-size: 12px;
  }
}
</style>
