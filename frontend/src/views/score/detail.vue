<template>
  <div class="app-container">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="真实姓名">
        <el-input v-model="realName" placeholder="真实姓名" />
      </el-form-item>
      <!-- <el-form-item label="所属课程">
        <el-input v-model="input1" placeholder="所属课程"></el-input>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
        <el-button type="primary" @click="getExportScores">导出</el-button>
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
      <el-table-column fixed label="序号" align="center" width="80">
        <template #default="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="title" label="试卷名称" align="center" />
      <el-table-column prop="realName" label="真实姓名" align="center" />
      <el-table-column prop="userScore" label="得分" align="center" />
      <el-table-column prop="count" label="切屏次数" align="center" />
      <el-table-column label="用时" align="center">
        <template #default="scope">
          {{ formatUserTime(scope.row.userTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="limitTime" label="提交时间" align="center" />
      
      <el-table-column fixed="right" label="操作" align="center">
        <template #default="{ row }">
          <el-button
            type="text"
            size="small"
            style="font-size: 14px"
            @click="updateRow(row)"
            >详情</el-button
          >
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
  </div>
</template>

<script>
import { scorePaging, exportScores } from '@/api/score'
export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      subjectId: '',
      examId: '',
      realName: '',
      examTitle: '',
      subjectName: '',
      data: {
        records: [],
        current: 1,
        size: 10,
        total: 0
      },
      formInline: {
        user: '',
        region: ''
      },
      input: '',
      input1: '',

      form: {
        name: ''
      },
      diaTitle: '',
      dialogTableVisible: false,
      dialogFormVisible: false,

      formLabelWidth: '120px'
    }
  },
  computed: {
    tables() {
      // 在你的数据表格中定义tabels
      const input = this.input
      const input1 = this.input1
      if (input) {
        //  ("input输入的搜索内容：" + this.input)
        return this.tableData.filter((data) => {
          //  ("object:" + Object.keys(data));
          return Object.keys(data).some((key) => {
            return String(data[key]).toLowerCase().indexOf(input) > -1
          })
        })
      }
      if (input1) {
        return this.tableData.filter((data) => {
          //  ("object:" + Object.keys(data));
          return Object.keys(data).some((key) => {
            return String(data[key]).toLowerCase().indexOf(input1) > -1
          })
        })
      }

      return this.tableData
    }
  },
  created() {
    this.examId = localStorage.getItem('examId')
    this.subjectId = localStorage.getItem('subjectId')
    this.examTitle = localStorage.getItem('examTitle')
    this.subjectName = localStorage.getItem('subjectName')
    this.getScorePage()
  },

  methods: {
    updateRow(row) {
        row.type= 1;
        console.log(row)
        localStorage.setItem('record_exam_examId', row.examId)
        localStorage.setItem('record_exam_userId', row.userId)
        this.$router.push({ name: 'exam-record-detail', query: { type: 1, userId: row.userId }})
      },
    // 分页查询
    async getScorePage() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        examId: this.examId,
        subjectId: this.subjectId,
        realName: this.realName
      }
      try {
        const res = await scorePaging(params)
        this.data = res.data || {
          records: [],
          current: this.pageNum,
          size: this.pageSize,
          total: 0
        }
      } catch (error) {
        console.error('获取成绩数据失败:', error)
        this.data = {
          records: [],
          current: this.pageNum,
          size: this.pageSize,
          total: 0
        }
      }
    },
    getExportScores() {
      exportScores(this.examId, this.subjectId).then(res => {
        // 检查响应是否为有效的Blob（非JSON错误响应）
        if (res && res instanceof Blob) {
          // 检查Blob类型：如果是application/json，说明是错误响应
          if (res.type === 'application/json') {
            // 读取JSON错误信息
            const reader = new FileReader()
            reader.onload = () => {
              try {
                const errorData = JSON.parse(reader.result)
                this.$message.error(errorData.msg || '导出失败')
              } catch (e) {
                this.$message.error('导出失败，请联系管理员')
              }
            }
            reader.readAsText(res)
            return
          }
          // 有效的Excel文件，执行下载
          if (res.size > 0) {
            var elink = document.createElement('a')
            var filename = '成绩导出.xlsx'
            if (this.subjectName && this.examTitle) {
              filename = this.subjectName + '-' + this.examTitle + '.xlsx'
            }
            elink.download = filename
            elink.style.display = 'none'
            elink.href = URL.createObjectURL(res)
            document.body.appendChild(elink)
            elink.click()
            document.body.removeChild(elink)
            URL.revokeObjectURL(elink.href)
          } else {
            this.$message.error('导出数据为空，请检查筛选条件')
          }
        } else {
          // 非Blob响应，可能是直接返回的JSON数据
          if (res && res.code && res.code !== 1) {
            this.$message.error(res.msg || '导出失败')
          } else {
            this.$message.error('导出数据为空，请检查筛选条件')
          }
        }
      }).catch(err => {
        console.error('导出失败:', err)
        this.$message.error(err.message || '导出失败，请联系管理员')
      })
    },

    onSubmit() {
      this.getScorePage()
      //  ("submit!");
    },
    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val
      this.getScorePage(this.pageNum, val)
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val
      this.getScorePage(val, this.pageSize)
    },
    
    // 格式化用户用时为 xx分xx秒
    formatUserTime(seconds) {
      if (!seconds || seconds < 0) return '0分0秒'
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes}分${remainingSeconds}秒`
    }

  }
}
</script>
<style></style>
