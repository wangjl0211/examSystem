
<template>
  <div class="app-container">
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item label="选择课程：">
        <el-select v-model="selectedSubjectId" placeholder="请选择课程" @change="queryRepo()">
          <el-option
            v-for="item in subjectOptions"
            :key="item.id"
            :label="item.subjectName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="题库名称：">
        <el-input v-model="repoTitle" />
      </el-form-item>
      <el-form-item label="题库分类：">
        <el-select v-model="categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="queryRepo()">查询</el-button>
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
      <el-table-column prop="repoTitle" label="题库标题" align="center" />
      <el-table-column prop="categoryName" label="题库分类" align="center">
        <template #default="{ row }">
          <span v-if="row.parentCategoryName">{{ row.parentCategoryName }} / </span>
          <span>{{ row.categoryName || '未分类' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalCount" label="试题总数" align="center" />

      <el-table-column fixed="right" label="操作" align="center">
        <template #default="{ row }">
          <div class="btn-group">
            <el-button
              type="success"
              plain
              :disabled="row.totalCount == 0"
              size="small"
              @click="screenInfo(row.id, row.repoTitle)"
            >开始刷题</el-button>
            <el-button
              type="danger"
              plain
              size="small"
              @click="clearRecord(row.id, row.repoTitle)"
            >清除记录</el-button>
          </div>
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
import { exercisePaging } from '@/api/exercise'
import { getCategoryTree } from '@/api/category'
import { fetchClasses } from '@/api/class_'

export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      repoTitle: '',
      categoryId: '',
      selectedSubjectId: '',
      categoryOptions: [],
      subjectOptions: [],
      dialogTableVisible: false,
      dialogFormVisible: false,
      formLabelWidth: '120px'
    }
  },

  created() {
    this.fetchSubjects()
    this.fetchCategories()
  },
  methods: {
    queryRepo() {
      this.getExercisePage(this.pageNum, this.pageSize, this.repoTitle, this.categoryId, this.selectedSubjectId)
    },
    // 分页查询
    async getExercisePage(pageNum, pageSize, title = null, categoryId = null, subjectId = null) {
      const params = { 
        pageNum: pageNum, 
        pageSize: pageSize, 
        title: title,
        categoryId: categoryId,
        subjectId: subjectId
      }
      try {
        const res = await exercisePaging(params)
        if (res.code) {
          this.data = res.data
        } else {
          this.$message.error(res.msg || '获取题库列表失败')
          this.data = { records: [], total: 0, size: 10, current: 1 }
        }
      } catch (error) {
        console.error('获取题库列表失败:', error)
        this.$message.error('获取题库列表失败')
        this.data = { records: [], total: 0, size: 10, current: 1 }
      }
    },
    // 获取课程列表
    async fetchSubjects() {
      try {
        const res = await fetchClasses()
        if (res.code) {
          this.subjectOptions = res.data
          // 默认选择第一个课程
          if (this.subjectOptions.length > 0) {
            this.selectedSubjectId = this.subjectOptions[0].id
            this.getExercisePage()
          }
        } else {
          this.$message.error(res.msg || '获取课程数据失败')
        }
      } catch (error) {
        console.error('获取课程失败:', error)
        this.$message.error('获取课程数据失败')
      }
    },
    // 获取分类列表
    async fetchCategories() {
      try {
        const res = await getCategoryTree()
        if (res.code) {
          this.categoryOptions = this.flattenCategoryTree(res.data)
        } else {
          this.$message.error(res.msg || '获取分类数据失败')
        }
      } catch (error) {
        console.error('获取分类失败:', error)
        this.$message.error('获取分类数据失败')
      }
    },
    // 将分类树扁平化为列表
    flattenCategoryTree(tree, result = []) {
      if (!tree || !tree.length) return result

      tree.forEach(node => {
        result.push({
          id: node.id,
          name: node.name
        })
        if (node.children && node.children.length > 0) {
          this.flattenCategoryTree(node.children, result)
        }
      })
      return result
    },
    screenInfo(id, repoTitle) {
      // 先验证用户是否有权限访问该题库
      import('@/api/exercise').then(({ getQuestion }) => {
        getQuestion(id).then(res => {
          if (res.code) {
            // 有权限，跳转到刷题页面
            this.$router.push({ name: 'start-exercise', query: { repoId: id, repoTitle: repoTitle }})
          } else {
            // 无权限，显示错误信息
            this.$message.error(res.msg || '您无权访问该题库')
          }
        }).catch(error => {
          console.error('验证权限失败:', error)
          this.$message.error('验证权限失败')
        })
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getExercisePage(this.pageNum, val, this.repoTitle, this.categoryId)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getExercisePage(val, this.pageSize, this.repoTitle, this.categoryId)
    },
    handleClick(row) {
      console.log(row)
    },
    clearRecord(repoId, repoTitle) {
      this.$confirm(`确定要清除「${repoTitle}」的刷题记录吗？此操作将删除您在此题库的所有答题历史，无法恢复。`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用清除记录API
        import('@/api/exercise').then(({ clearRecord }) => {
          clearRecord(repoId).then(res => {
            if (res.code) {
              this.$message.success(res.msg || '清除记录成功')

              // 清除本地存储的答题记录
              try {
                localStorage.removeItem(`submittedAnswers_${repoId}`)
              } catch (error) {
                console.error('清除本地存储失败:', error)
              }

              // 刷新题库列表
              this.queryRepo()
            } else {
              this.$message.error(res.msg || '清除记录失败')
            }
          }).catch(error => {
            console.error('清除记录失败:', error)
            this.$message.error('清除记录失败')
          })
        })
      }).catch(() => {
        // 取消操作
      })
    }
  }
}
</script>

<style>
.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 移动端响应式 */
@media (max-width: 991px) {
  /* 表格横向滚动 */
  .el-table {
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
}
</style>
