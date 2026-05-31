<template>
  <div class="app-container">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="题库名称:">
        <el-input v-model="searchTitle" placeholder="请输入查询内容" />
      </el-form-item>
      <el-form-item label="题库分类:">
        <el-select v-model="searchCategory" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchRepo">查询</el-button>
        <el-button type="primary" @click="addRepoDialogVisible = true">新增</el-button>
        <el-button type="primary" @click="categoryDialogVisible = true">分类管理</el-button>
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
      <el-table-column prop="title" label="题库名称" align="center" />
      <el-table-column prop="categoryName" label="题库分类" align="center" />
      <el-table-column label="题目数量" align="center">
        <template #default="{ row }">
          <span :style="{ color: row.questionCount === 0 ? '#F56C6C' : '' }">{{ row.questionCount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="realName" label="创建人" align="center" />
      <el-table-column prop="createTime" label="创建时间" align="center" />

      <el-table-column label="开启刷题" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isExercise === 1 ? 'success' : 'danger'" :effect="row.isExercise === 0 ? 'dark' : 'light'">
            {{ row.isExercise === 1 ? '已开启' : '未开启' }}
            <i v-if="row.isExercise === 0" style="margin-left: 2px;"></i>
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" align="center">
        <template #default="{ row }">
          <div class="btn-group">
            <el-button type="text" size="small" style="font-size: 14px" @click="updateRow(row)">编辑</el-button>
            <el-button type="text" size="small" style="color: red; font-size: 14px" @click="delRepo(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- <div class="pagination-container"> -->
      <el-pagination
        :current-page="data.current"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="data.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="data.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    <!-- </div> -->

    <!-- 新增题库 -->
    <el-dialog title="新增题库" v-model="addRepoDialogVisible" width="500px">
      <el-form :model="addRepoForm" label-width="100px">
        <el-form-item label="题库名称">
          <el-input v-model="addRepoForm.title" placeholder="请输入题库名称"></el-input>
        </el-form-item>
        <el-form-item label="题库分类">
          <el-select v-model="addRepoForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addRepoDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitAddRepo">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑题库 -->
    <el-dialog title="编辑题库" v-model="dialogFormVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="题库名称">
          <el-input v-model="form.title" placeholder="请输入题库名称"></el-input>
        </el-form-item>
        <el-form-item label="题库分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否开启刷题">
          <div style="display: flex; align-items: flex-start;">
            <el-switch v-model="form.isExercise" :active-value="1" :inactive-value="0" @change="handleExerciseToggle" style="margin-top: 4px;"></el-switch>
            <div v-if="form.isExercise === 1" class="exercise-courses" style="margin-left: 20px; flex: 1;">
              <el-form-item label="" style="margin-bottom: 0;">
                <div class="custom-select-container">
                  <!-- 自定义选择框 -->
                  <div class="custom-select-input" @click="handleSelectClick">
                    <div v-if="selectedCourses.length === 0" class="placeholder">请选择课程</div>
                    <div v-else class="selected-courses">
                      <div v-for="courseId in selectedCourses" :key="courseId" class="course-item" @click.stop>
                        {{ getCourseNameById(courseId) }}
                      </div>
                    </div>
                    <i class="el-select__caret el-input__icon el-icon-arrow-up"></i>
                  </div>
                  <!-- 隐藏的实际选择框 -->
                  <el-select
                    ref="courseSelect"
                    v-model="selectedCourses"
                    multiple
                    placeholder="请选择课程"
                    style="width: 100%; position: absolute; opacity: 0; pointer-events: none;"
                    popper-class="course-select-dropdown"
                    popper-append-to-body
                    @change="handleCourseChange"
                    @visible-change="handleMenuVisibleChange"
                  >
                    <!-- 全选选项 -->
                    <el-option
                      key="all"
                      :value="'all'"
                      :label="'全选'"
                      class="all-select-option"
                    >
                      <div style="display: flex; align-items: center; justify-content: space-between;" @click.stop>
                        <span>全选</span>
                        <el-checkbox v-model="isAllSelected" @change="handleAllSelectChange" style="margin-left: 10px;"></el-checkbox>
                      </div>
                    </el-option>
                    <!-- 课程选项 -->
                    <el-option
                      v-for="course in teacherCourses"
                      :key="course.id"
                      :value="course.id"
                      :label="course.subjectName"
                      class="course-option"
                    >
                      <div style="display: flex; align-items: center; justify-content: space-between;" @click.stop>
                        <span>{{ course.subjectName }}</span>
                        <el-checkbox v-model="courseSelection[course.id]" @change="(val) => handleCourseCheckboxChange(course.id, val)" style="margin-left: 10px;"></el-checkbox>
                      </div>
                    </el-option>
                  </el-select>
                </div>
              </el-form-item>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitEditRepo">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog title="题库分类管理" v-model="categoryDialogVisible" width="600px">
      <div class="category-header">
        <el-button type="primary" size="small" @click="addCategory">添加分类</el-button>
      </div>
      <el-table :data="categoryList" border style="width: 100%" row-key="id">
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="parentName" label="父级分类" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="text" size="small" @click="editCategory(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: red;" @click="deleteCategory(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加分类对话框 -->
    <el-dialog title="添加分类" v-model="addCategoryDialogVisible" width="400px" append-to-body>
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.categoryName" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" clearable>
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addCategoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddCategory">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑分类对话框 -->
    <el-dialog title="编辑分类" v-model="editCategoryDialogVisible" width="400px" append-to-body>
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.categoryName" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" clearable>
            <el-option
              v-for="item in categoryOptions.filter(opt => opt.id !== categoryForm.id)"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editCategoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditCategory">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { repoPaging, repoDel, repoUpdate, repoAdd, getTeacherCourses, updateRepoCourses, getRepoCourses } from '@/api/repo'
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/category'

export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      isExercise: true,
      data: {
        records: [],
        total: 0,
        size: 10,
        current: 1
      },
      addTitle: '',
      delVisible: false,
      searchTitle: '',
      searchCategory: '',
      Obj: {},
      formInline: {
        searchTitle: ''
      },
      cancle() { },

      diaTitle: '新增',
      form: {
        title: '',
        isExercise: 0,
        categoryId: ''
      },
      addRepoForm: {
        title: '',
        categoryId: null
      },
      formLabelWidth: '120px',
      dialogVisible: false,
      addRepoDialogVisible: false,
      dialogTableVisible: false,
      dialogFormVisible: false,
      categoryDialogVisible: false,
      addCategoryDialogVisible: false,
      editCategoryDialogVisible: false,
      categoryForm: {
        id: '',
        categoryName: '',
        parentId: null
      },
      categoryOptions: [],
      categoryList: [],
      selectedCourses: [],
      teacherCourses: [],
      isAllSelected: false,
      courseSelection: {}
    }
  },
  watch: {
    selectedCourses: {
      handler(newVal) {
        // 处理全选逻辑
        if (newVal.includes('all')) {
          // 如果包含全选，则选择所有课程
          const allCourseIds = this.teacherCourses.map(course => course.id)
          this.selectedCourses = allCourseIds
        }
      },
      deep: true
    }
  },
  created() {
    this.getRepoPage()
    this.fetchCategories()
    this.fetchTeacherCourses()
  },
  methods: {
    // 分页查询
    async getRepoPage(pageNum = this.pageNum, pageSize = this.pageSize, title = null, categoryId = null) {
      try {
        const params = {
          pageNum: pageNum,
          pageSize: pageSize,
          title: title,
          categoryId: categoryId
        }
        const res = await repoPaging(params)
        if (res.code) {
          this.data = res.data
        } else {
          this.$message.error(res.msg || '获取题库数据失败')
        }
      } catch (error) {
        console.error('获取题库数据失败:', error)
        this.$message.error('获取题库数据失败')
      }
    },
    // 获取分类列表
    async fetchCategories() {
      try {
        const res = await getCategoryTree()
        if (res.code) {
          this.categoryOptions = this.flattenCategoryTree(res.data)
          // 处理分类列表，添加父级分类名称
          this.categoryList = this.processCategoryList(res.data)
        } else {
          this.$message.error(res.msg || '获取分类数据失败')
        }
      } catch (error) {
        console.error('获取分类失败:', error)
        this.$message.error('获取分类数据失败')
      }
    },
    // 获取教师课程列表
    async fetchTeacherCourses() {
      try {
        // 这里需要调用API获取当前教师的课程列表
        // 假设API为 getTeacherCourses
        const res = await getTeacherCourses()
        if (res.code) {
          this.teacherCourses = res.data
        } else {
          this.$message.error(res.msg || '获取课程数据失败')
        }
      } catch (error) {
        console.error('获取课程失败:', error)
        this.$message.error('获取课程数据失败')
      }
    },
    // 处理开启刷题 toggle 事件
    handleExerciseToggle(value) {
      if (value === 1) {
        // 开启刷题时，重置课程选择
        this.selectedCourses = []
        this.isAllSelected = false
        this.courseSelection = {}
      }
    },
    // 处理课程选择变化
    handleCourseChange(val) {
      if (val.includes('all')) {
        // 移除全选标识
        const filteredVal = val.filter(item => item !== 'all')
        
        // 检查当前是否已经全选
        const allCourseIds = this.teacherCourses.map(course => course.id)
        const isAllSelected = allCourseIds.every(id => filteredVal.includes(id))
        
        if (isAllSelected) {
          // 如果已经全选，则取消全选
          this.selectedCourses = []
          this.isAllSelected = false
          // 明确将每个课程的状态设置为false，确保UI正确更新
          this.courseSelection = {}
          this.teacherCourses.forEach(course => {
            this.courseSelection[course.id] = false
          })
        } else {
          // 如果没有全选，则选择所有课程
          this.selectedCourses = allCourseIds
          this.isAllSelected = true
          // 更新courseSelection对象
          this.courseSelection = {}
          allCourseIds.forEach(id => {
            this.courseSelection[id] = true
          })
        }
      } else {
        // 更新全选状态
        this.isAllSelected = val.length === this.teacherCourses.length
        // 更新courseSelection对象
        this.courseSelection = {}
        val.forEach(id => {
          this.courseSelection[id] = true
        })
      }
    },
    // 处理全选勾选框变化
    handleAllSelectChange(val) {
      // 阻止事件冒泡，避免与el-select的默认行为冲突
      event.stopPropagation()
      
      if (val) {
        // 全选
        const allCourseIds = this.teacherCourses.map(course => course.id)
        this.selectedCourses = allCourseIds
        this.isAllSelected = true
        // 更新courseSelection对象
        this.courseSelection = {}
        allCourseIds.forEach(id => {
          this.courseSelection[id] = true
        })
      } else {
        // 取消全选
        this.selectedCourses = []
        this.isAllSelected = false
        // 明确将每个课程的状态设置为false，确保UI正确更新
        this.courseSelection = {}
        this.teacherCourses.forEach(course => {
          this.courseSelection[course.id] = false
        })
      }
    },
    // 检查课程是否被选中
    isCourseSelected(courseId) {
      return this.selectedCourses.includes(courseId)
    },
    // 处理课程勾选框变化
    handleCourseCheckboxChange(courseId, val) {
      // 阻止事件冒泡，避免与el-select的默认行为冲突
      event.stopPropagation()
      
      if (val) {
        // 选中课程
        if (!this.selectedCourses.includes(courseId)) {
          this.selectedCourses.push(courseId)
        }
        this.courseSelection[courseId] = true
      } else {
        // 取消选中课程
        const index = this.selectedCourses.indexOf(courseId)
        if (index > -1) {
          this.selectedCourses.splice(index, 1)
        }
        this.courseSelection[courseId] = false
      }
      // 更新全选状态
      this.isAllSelected = this.selectedCourses.length === this.teacherCourses.length
    },
    // 根据课程ID获取课程名称
    getCourseNameById(courseId) {
      const course = this.teacherCourses.find(course => course.id === courseId)
      return course ? course.subjectName : ''
    },
    // 处理选择框点击事件
    handleSelectClick() {
      // 直接切换菜单状态
      if (this.$refs.courseSelect) {
        this.$refs.courseSelect.toggleMenu()
      }
    },
    // 处理菜单显示/隐藏状态变化
    handleMenuVisibleChange() {
      // 可以在这里添加菜单状态变化的处理逻辑
      // 例如更新图标方向等
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
    // 处理分类列表，添加父级分类名称
    processCategoryList(tree, parentName = null, result = []) {
      if (!tree || !tree.length) return result

      tree.forEach(node => {
        result.push({
          id: node.id,
          name: node.name,
          parentId: node.parentId,
          parentName: parentName
        })
        if (node.children && node.children.length > 0) {
          this.processCategoryList(node.children, node.name, result)
        }
      })
      return result
    },
    searchRepo() {
      this.getRepoPage(this.pageNum, this.pageSize, this.searchTitle, this.searchCategory)
    },
    async updateRow(row) {
      this.dialogFormVisible = true
      this.form = { ...row }
      
      // 重置课程选择相关状态
      this.selectedCourses = []
      this.isAllSelected = false
      this.courseSelection = {}
      
      // 如果当前题库已开启刷题，需要加载已关联的课程
      if (this.form.isExercise === 1) {
        try {
          const res = await getRepoCourses(this.form.id)
          if (res.code) {
            // 加载已关联的课程
            const relatedCourses = res.data
            if (relatedCourses && relatedCourses.length > 0) {
              // 设置已选中的课程ID
              const courseIds = relatedCourses.map(course => course.id)
              this.selectedCourses = courseIds
              
              // 更新courseSelection对象
              this.courseSelection = {}
              courseIds.forEach(id => {
                this.courseSelection[id] = true
              })
              
              // 更新全选状态
              this.isAllSelected = courseIds.length === this.teacherCourses.length
            }
          }
        } catch (error) {
          console.error('获取已关联课程失败:', error)
          this.$message.error('获取已关联课程失败')
        }
      }
    },
    submitAddRepo() {
      if (!this.addRepoForm.title) {
        this.$message.warning('请输入题库名称')
        return
      }

      const data = {
        'title': this.addRepoForm.title,
        'categoryId': this.addRepoForm.categoryId
      }

      repoAdd(data)
        .then((res) => {
          if (res.code) {
            this.addRepoDialogVisible = false
            this.getRepoPage()
            this.$message({
              type: 'success',
              message: '添加成功'
            })
            // 重置表单
            this.addRepoForm = {
              title: '',
              categoryId: null
            }
          } else {
            this.$message({
              type: 'info',
              message: res.msg || '添加失败'
            })
          }
        })
        .catch((error) => {
          console.error('添加题库失败:', error)
          this.$message.error('添加题库失败')
        })
    },
    // 编辑题库
    submitEditRepo() {
      if (!this.form.title) {
        this.$message.warning('请输入题库名称')
        return
      }

      if (this.form.isExercise === 1 && this.selectedCourses.length === 0) {
        this.$message.warning('开启刷题功能时，请至少选择一个课程')
        return
      }

      const data = {
        'title': this.form.title,
        'isExercise': this.form.isExercise,
        'categoryId': this.form.categoryId
      }

      repoUpdate(this.form.id, data)
        .then((res) => {
          if (res.code) {
            // 如果开启了刷题功能，更新课程关联
            if (this.form.isExercise === 1) {
              return updateRepoCourses(this.form.id, this.selectedCourses)
            }
            return Promise.resolve({ code: 1 })
          }
          return Promise.reject(res)
        })
        .then(() => {
          this.getRepoPage()
          this.dialogFormVisible = false
          this.$message({
            type: 'success',
            message: '编辑成功!'
          })
        })
        .catch((error) => {
          console.error('编辑题库失败:', error)
          this.$message.error('编辑题库失败')
        })
    },
    // 删除题库
    delRepo(row) {
      this.$confirm('此操作将永久删除该题库, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          repoDel(row.id).then((res) => {
            if (res.code) {
              this.getRepoPage()
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
            } else {
              this.$message({
                type: 'info',
                message: res.msg || '删除失败'
              })
            }
          }).catch((error) => {
            console.error('删除题库失败:', error)
            this.$message.error('删除题库失败')
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    // 分类管理相关方法
    addCategory() {
      this.categoryForm = {
        name: '',
        parentId: null
      }
      this.addCategoryDialogVisible = true
    },
    editCategory(row) {
      this.categoryForm = {
        id: row.id,
        categoryName: row.name,
        parentId: row.parentId
      }
      this.editCategoryDialogVisible = true
    },
    submitAddCategory() {
      if (!this.categoryForm.categoryName) {
        this.$message.warning('请输入分类名称')
        return
      }

      const data = {
        categoryName: this.categoryForm.categoryName,
        parentId: this.categoryForm.parentId || 0
      }

      addCategory(data)
        .then((res) => {
          if (res.code) {
            this.$message.success('添加成功')
            this.addCategoryDialogVisible = false
            this.fetchCategories()
          } else {
            this.$message.error(res.msg || '添加分类失败')
          }
        })
        .catch((error) => {
          console.error('添加分类失败:', error)
          this.$message.error('添加分类失败')
        })
    },
    submitEditCategory() {
      if (!this.categoryForm.categoryName) {
        this.$message.warning('请输入分类名称')
        return
      }

      const data = {
        categoryName: this.categoryForm.categoryName,
        parentId: this.categoryForm.parentId || 0
      }

      updateCategory(this.categoryForm.id, data)
        .then((res) => {
          if (res.code) {
            this.$message.success('修改成功')
            this.editCategoryDialogVisible = false
            this.fetchCategories()
          } else {
            this.$message.error(res.msg || '修改分类失败')
          }
        })
        .catch((error) => {
          console.error('修改分类失败:', error)
          this.$message.error('修改分类失败')
        })
    },
    deleteCategory(id) {
      this.$confirm('确认删除该分类?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCategory(id)
          .then((res) => {
            if (res.code) {
              this.$message.success('删除成功')
              this.fetchCategories()
            } else {
              this.$message.error(res.msg || '删除分类失败')
            }
          })
          .catch((error) => {
            console.error('删除分类失败:', error)
            this.$message.error('删除分类失败')
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    handleClose(done) {
      done()
    },
    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val
      this.getRepoPage(this.pageNum, val, this.searchTitle, this.searchCategory)
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val
      this.getRepoPage(val, this.pageSize, this.searchTitle, this.searchCategory)
    }
  }
}
</script>

<style scoped>
.category-header {
  margin-bottom: 15px;
}
.pagination-container {
  margin-top: 20px;
  text-align: center;
}
/* 课程选择下拉框样式 */
.course-select-dropdown {
  min-width: 300px;
}
/* 全选选项样式 */
.all-select-option {
  font-weight: bold;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 8px;
  margin-bottom: 8px;
}
/* 课程选项样式 */
.course-option {
  position: relative;
}
/* 修复选择框布局 */
.exercise-courses {
  margin-left: 20px !important;
}

/* 自定义选择框样式 */
.custom-select-container {
  position: relative;
  width: 100%;
}

.custom-select-input {
  position: relative;
  width: 100%;
  min-height: 36px;
  padding: 6px 30px 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.custom-select-input:hover {
  border-color: #c0c4cc;
}

.custom-select-input:focus-within {
  border-color: #409eff;
  outline: 0;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.placeholder {
  color: #909399;
  line-height: 24px;
}

.selected-courses {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.course-item {
  line-height: 24px;
  padding: 2px 8px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  color: #409eff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  box-sizing: border-box;
}

.custom-select-input i {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  transition: transform 0.3s;
}

.custom-select-input:hover i {
  color: #909399;
}

/* 确保下拉菜单位置正确 */
.el-select-dropdown {
  z-index: 1001;
}

/* 移动端响应式 */
@media (max-width: 991px) {
  /* 表格横向滚动 */
  .el-table {
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  /* 单元格内容处理 */
  .el-table .cell {
    white-space: normal;
    word-break: break-word;
  }
}
</style>
