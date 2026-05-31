<template>
  <div class="app-container">
    <!-- form -->

    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="试题名称">
        <el-input v-model="searchName" placeholder="输入试题名称" />
      </el-form-item>
      <el-form-item label="所属题库">
        <repo-select
          v-model="selectedRepoSingleSearch"
          @change="handleRepoChangeSingle"
        />
      </el-form-item>
      <el-form-item label="题目类型">
        <el-select v-model="selValue" placeholder="请选择">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <div class="btn-group">
          <el-button type="primary" @click="searchQu">查询</el-button>
          <el-button type="primary" @click="screenInfo()">新增</el-button>
          <el-button type="primary" @click="fileDialogVisible = true">导入</el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- 文件上传 -->
    <el-dialog
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
      title="上传文件"
      v-model="fileDialogVisible"
    >
      <!-- v-model="scope.row.repoId" -->
      <!-- @change="repoChange($event, scope.row)" -->
      请选择题库：
      <repo-select
        v-model="selectedRepoSingle"
        style="margin-bottom: 10px"
        @change="handleRepoChangeSingle"
      />
      <el-upload
        class="upload-demo"
        drag
        action="xxxxxx"
        multiple
        :limit="1"
        accept=".xlsx, .xls"
        :auto-upload="false"
        :on-remove="handleRemove"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传xls/xlsx文件，且不超过500kb</div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="fileDialogVisible = false">取 消</el-button>
          <el-button type="success" plain @click="startDownload">下载模板</el-button>
          <el-button type="primary" @click="importQu" :disabled="isImporting" :loading="isImporting">
            {{ isImporting ? '正在导入' : '确 定' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    <!-- table -->

    <el-table
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
      <el-table-column label="序号" align="center" width="80">
        <template #default="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="content" label="题干" align="center" />
      <el-table-column label="题目类型" align="center">
        <template #default="scope">
          <span v-if="scope.row.quType == 1">单选题</span>
          <span v-else-if="scope.row.quType == 2">多选题</span>
          <span v-else-if="scope.row.quType == 3">判断题</span>
          <span v-else-if="scope.row.quType == 4">简答题</span>
        </template>
      </el-table-column>
      <el-table-column prop="repoTitle" label="所属题库" align="center" />
      <el-table-column prop="createTime" label="创建时间" align="center" />
      <el-table-column align="center" label="操作">
        <template #default="{ row }">
          <div class="btn-group">
            <el-button
              type="text"
              size="small"
              style="font-size: 14px"
              @click="updateRow(row)"
            >编辑</el-button>
            <el-button
              type="text"
              size="small"
              style="color: red; font-size: 14px"
              @click="delQu(row)"
            >删除</el-button>
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
    <!--编辑弹窗-->

    <!-- <el-dialog title="编辑" :visible.sync="dialogFormVisible">
      <el-row>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="题干" :label-width="formLabelWidth">
              <el-input v-model="form.content" autocomplete="off"></el-input>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="所属题库" :label-width="formLabelWidth">
              <el-input v-model="form.repoTitle" autocomplete="off"></el-input>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="updateQu">确 定</el-button>
      </div>
    </el-dialog> -->
  </div>
</template>

<script>
import { quPaging, quDel, quUpdate, importQue } from '@/api/question'
import RepoSelect from '@/components/RepoSelect'

export default {
  components: { RepoSelect },
  data() {
    return {
      options: [
        {
          value: null,
          label: '全部类型'
        },
        {
          value: 1,
          label: '单选题'
        },
        {
          value: 2,
          label: '多选题'
        },
        {
          value: 3,
          label: '判断题'
        },
        {
          value: 4,
          label: '简答题'
        }
      ],
      length: '',
      fileList: [],
      selValue: '',
      searchName: '',
      pageNum: 1,
      pageSize: 10,
      data: {},
      fileDialogVisible: false,
      // 导入状态管理
      isImporting: false,

      selectedRepoSingle: '',
      selectedRepoSingleSearch: '',
      input: '',
      input1: '',
      formInline: {
        user: '',
        region: ''
      },
      cancle() {},
      dialogTableVisible: false,
      dialogFormVisible: false,
      hasFiles: null,
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
      formLabelWidth: '110px'
    }
  },
  computed: {
    // tables() {
    //   //在你的数据表格中定义tabels
    //   const input = this.input;
    //   const input1 = this.input1;
    //   if (input) {
    //     //  ("input输入的搜索内容：" + this.input)
    //     return this.tableData.filter((data) => {
    //        ("object:" + Object.keys(data));
    //       return Object.keys(data).some((key) => {
    //         return String(data[key]).toLowerCase().indexOf(input) > -1;
    //       });
    //     });
    //   }
    //   if (input1) {
    //     //  ("input输入的搜索内容：" + this.input)
    //     return this.tableData.filter((data) => {
    //        ("object:" + Object.keys(data));
    //       return Object.keys(data).some((key) => {
    //         return String(data[key]).toLowerCase().indexOf(input1) > -1;
    //       });
    //     });
    //   }
    //   return this.tableData;
    // },
  },
  created() {
    this.getQuPage()
  },
  methods: {
    handleRepoChangeSingle(repo) {
      ('单选题库变化:', repo)
      // 这里可以进一步处理repo对象，比如更新UI或发送网络请求等
    },
    updateRow(row) {
      localStorage.setItem('quId', row.id)
      this.$router.push({ name: 'questions-add' })
    },
    importQu() {
      // 防止重复导入
      if (this.isImporting) {
        console.log('正在导入中，请勿重复操作')
        return
      }

      if (this.fileList && this.fileList.length > 0 && this.selectedRepoSingle != '') {
        // 设置导入状态为进行中
        this.isImporting = true

        const formData = new FormData()
        formData.append('file', this.fileList[0].raw)
        importQue(this.selectedRepoSingle, formData)
          .then((response) => {
            if (response.code) {
              this.$message.success('导入成功！')
              this.getQuPage(this.pageNum, this.pageSize)
              this.fileDialogVisible = false
              this.selectedRepoSingle = ''
              this.fileList = []
            } else {
              this.$message({
                type: 'error',
                message: response.msg
              })
              this.fileList = []
            }
          })
          .catch((error) => {
            console.error('文件上传失败：', error)
            this.$message.error('文件上传失败！')
            this.fileList = []
          })
          .finally(() => {
            // 无论成功失败，都恢复按钮状态
            this.isImporting = false
          })
      } else {
        this.$message.warning('导入失败，请确定题库和文件格式是否正确~')
      }
    },
    handleFileChange(file, fileList) {
      this.fileList = fileList // 收集文件信息
    },
    // 移除文件处理方法
    handleRemove(file, fileList) {
      if (fileList.length === 0) {
        this.hasFiles = false
      }
    },
    // 分页查询
    async getQuPage(pageNum, pageSize, content = null, repoId = null, type = null) {
      const params = {
        pageNum: pageNum,
        pageSize: pageSize,
        content: content,
        repoId: repoId,
        type: type
      }
      const res = await quPaging(params)
      this.data = res.data
    },
    // 编辑题库
    updateQu() {
      quUpdate(this.form.id, { title: this.form.title })
        .then((res) => {
          if (res.code) {
            this.getQuPage(
              this.pageNum,
              this.pageSize,
              this.searchName,
              this.selectedRepoSingleSearch,
              this.selValue
            )
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
    // 删除试题
    delQu(row) {
      this.$confirm('此操作将永久删除该试题, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          quDel(row.id).then((res) => {
            if (res.code) {
              this.getQuPage(
                this.pageNum,
                this.pageSize,
                this.searchName,
                this.selectedRepoSingleSearch,
                this.selValue
              )
              // this.tableData.splice(index, 1)
              // this.getQuPage(1);
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
    searchQu() {
      this.getQuPage(
        this.pageNum,
        this.pageSize,
        this.searchName,
        this.selectedRepoSingleSearch,
        this.selValue
      )
    },

    screenInfo(row) {
      this.$router.push({ name: 'questions-add', query: { zhi: row }})
    },

    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val
      this.getQuPage(this.pageNum, val,this.searchName,
        this.selectedRepoSingleSearch,
        this.selValue)
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val
      this.getQuPage(val, this.pageSize,this.searchName,
        this.selectedRepoSingleSearch,
        this.selValue)
    },
    // 下载模板
    async startDownload() {
      const a = document.createElement('a')
      a.href = './template/ImportQuestionTemplate.xlsx'
      a.download = '导入试题模板.xlsx'
      // 障眼法藏起来a标签
      a.style.display = 'none'
      // 将a标签追加到文档对象中
      document.body.appendChild(a)
      // 模拟点击了<a>标签,会触发<a>标签的href的读取,浏览器就会自动下载了
      a.click()
      // 一次性的,用完就删除a标签
      a.remove()
    }
  }
}
</script>

<style scoped>
/* 移动端响应式 */
@media (max-width: 991px) {
  /* 按钮区域布局优化 */
  .demo-form-inline {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .demo-form-inline .el-form-item {
    margin-bottom: 10px;
  }

  /* 按钮在同一行显示 */
  .demo-form-inline .el-form-item:last-child,
  .demo-form-inline .el-form-item:nth-last-child(2),
  .demo-form-inline .el-form-item:nth-last-child(3) {
    flex: 0 0 auto;
  }

  /* 表格横向滚动 */
  .el-table {
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  /* 题干列内容显示优化 */
  .el-table .cell {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 150px;
  }
}
</style>
