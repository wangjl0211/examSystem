<template>
  <div class="app-container">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="试卷名称">
        <el-input v-model="examTitle" placeholder="试卷名称" />
      </el-form-item>
      <el-form-item label="所属课程">
        <ClassSelect v-model="subjectId" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- table -->

    <el-table
      :data="data.records"
      border
      fit
      highlight-current-row
      row-key="examId"
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
      <el-table-column prop="examTitle" label="考试名称" align="center" />
      <el-table-column prop="subjectName" label="课程" align="center" />
      <el-table-column prop="maxScore" label="最高分" align="center" />
      <el-table-column prop="minScore" label="最低分" align="center" />
      <el-table-column prop="avgScore" label="平均分" align="center" />
      <el-table-column prop="attendNum" label="参考人数" align="center" />
      <!-- <el-table-column prop="limitTime" label="提交时间" align="center" /> -->

      <el-table-column fixed="right" label="操作" align="center">
        <template #default="{ row }">
          <el-button
            type="text"
            size="small"
            :disabled="row.maxScore == null"
            style="font-size: 14px"
            @click="updateRow(row)"
          >详情</el-button>
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
import ClassSelect from '@/components/ClassSelect'
import { getExamScore } from '@/api/score'
export default {
  components: { ClassSelect },
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      subjectId: '',
      examTitle: '',
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
      cancle() {},
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
    this.getScorePage()
  },
  methods: {
    // 分页查询
    async getScorePage() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        subjectId: this.subjectId,
        examTitle: this.examTitle
      }
      try {
        const res = await getExamScore(params)
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
    onSubmit() {
      this.getScorePage()
    },
    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val
      this.getScorePage()
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val
      this.getScorePage()
    },

    updateRow(row) {
      localStorage.setItem('examId', row.examId)
      localStorage.setItem('subjectId', row.subjectId)
      localStorage.setItem('examTitle', row.examTitle)
      localStorage.setItem('subjectName', row.subjectName)
      this.$router.push({ name: 'user-score' })
    },

  }
}
</script>
<style></style>
