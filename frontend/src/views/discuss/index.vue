<template>
  <div class="app-container">
    <!-- 错误提示 -->
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      :closable="true"
      @close="errorMessage = ''"
      show-icon
      style="margin-bottom: 20px"
    />
    
    <el-form :inline="true" :mode="searchForm" class="demo-form-inline">
      <el-form-item label="讨论名称">
        <el-input v-model="searchForm.searchTitle" placeholder="讨论名称" />
      </el-form-item>
      <el-form-item label="课程" v-if="currentRole === 'teacher'">
        <ClassSelect v-model="searchForm.subjectId" :is-multiple="false" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchDiscussion">查询</el-button>
        <el-button type="primary" @click="visible = true" v-if="currentRole === 'teacher'||currentRole === 'admin'"
          >新建话题</el-button
        >
      </el-form-item>
    </el-form>
    <!-- 表格 -->
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
      <el-table-column prop="title" label="标题" align="center" />
      <el-table-column prop="sender" label="创建人" align="center" />
      <el-table-column prop="subjectName" label="所属课程" align="center" />
      <el-table-column prop="createTime" label="创建时间" align="center" />
      <el-table-column fixed="right" label="操作" align="center">
        <template #default="{ row }">
          <div class="btn-group">
            <el-button
              type="text"
              size="small"
              style="color: green; font-size: 14px"
              @click="showRow(row)"
              >查看</el-button
            >
            <el-button
            type="text"
            size="small"
            style="color: red; font-size: 14px"
            @click="handleDel(row.id)"
            v-if="currentRole === 'teacher' || currentRole === 'admin'"
            >删除</el-button
          >
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

     <!-- 发布讨论对话框 -->
     <el-dialog title="发布讨论" v-model="visible" width="30%">
      <el-row :gutter="20">
        <el-col>
          <el-form :model="discussionForm" label-position="top" :rules="rules" ref="discussionFormRef">
            <el-form-item label="讨论标题:" prop="title">
              <el-input
                v-model="discussionForm.title"
                 placeholder="请输入标题"
                autocomplete="off"
                style="width: 80%"
                @input="handleInput('title')"
              />
            </el-form-item>
            <el-form-item label="讨论内容:" prop="content">
              <el-input
              type="textarea"
              :rows="6"
              placeholder="请输入内容"
              clearable
              resize="none"
              v-model="discussionForm.content"
              @input="handleInput('content')">
            </el-input>
            </el-form-item>
            <el-form-item label="课程:" prop="subjectId">
              <ClassSelect v-model="discussionForm.subjectId"  :is-multiple="false" @change="handleInput('subjectId')" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="visible = false">取 消</el-button>
          <el-button type="primary" @click="handleConfirm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>

</template>

<script>
import { discussionpageOwner,discussionpageStudent,discussionAdd,discussionDel } from "@/api/discussion";
import ClassSelect from "@/components/ClassSelect";
import { EventBus } from '@/utils/eventBus';
export default {
  components: {
    ClassSelect,
  },
  data() {
    return {
      visible:false,
      currentRole:null,
      pageNum: 1,
      pageSize: 10,
      data: {},
      searchForm: {
        searchTitle: "",
        subjectId: null,
      },
      discussionForm:{
        title:null,
        content:null,
        subjectId:null
      },
      errorMessage: '',
      rules: {
        title: [
          { required: true, message: '请输入内容', trigger: ['blur', 'input'] }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: ['blur', 'input'] }
        ],
        subjectId: [
          { required: true, message: '请输入内容', trigger: 'change' }
        ]
      },
    };
  },
  created() {

    this.currentRole =  localStorage.getItem('roles')
    this.getDiscussionPage();
    
    // 监听websocket收到消息发送来的事件
    EventBus.on('websocket-message', this.handleMessage)
  },
  beforeUnmount() {
    // 组件卸载时取消监听
    EventBus.off('websocket-message', this.handleMessage)
  },
  methods: {
    //弹出提示框
    handleDel(id){
        this.$confirm('此操作将永久删除该讨论, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
         this.delDiscussion(id)
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
    },
    //删除讨论
    delDiscussion(id){
      discussionDel(id).then(res=>{
        if(res.code){
          this.$message({
              type: 'success',
              message: res.msg
            })
            this.getDiscussionPage(this.pageNum, this.pageSize,this.title,this.subjectId)
        }else{
          this.errorMessage = res.msg
        }
      }).catch(error => {
        this.errorMessage = error.message || '操作失败，请重试'
      })
    },
    //提交表单
    handleConfirm(){
      // 触发表单验证
      this.$refs.discussionFormRef.validate((valid) => {
        if (valid) {
          // 验证通过，执行讨论创建操作
          discussionAdd(this.discussionForm).then(res=>{
            if(res.code){
              this.discussionForm.title = null
              this.discussionForm.content = null
              this.discussionForm.subjectId = null
              this.visible = false
              this.getDiscussionPage()
              this.$message({
                  type: 'success',
                  message: res.msg
                })
            }else {
                this.errorMessage = res.msg
              }
          }).catch(error => {
            this.errorMessage = error.message || '操作失败，请重试'
          })
        } else {
          // 验证失败，阻止讨论创建操作
          return false
        }
      })
    },
    showRow(row) {
      // this.$router.push({name: 'discussion-detail',query: { row: row }})
      this.$router.push({name: 'discussion-detail',query:{discussionId: row.id}})
    },
    searchDiscussion() {
      this.getDiscussionPage(
        this.pageNum,
        this.pageSize,
        this.searchForm.searchTitle,
        this.searchForm.subjectId
      );
    },
    // 分页查询
    async getDiscussionPage(pageNum, pageSize, title = null, subjectId = null) {
      const params = {
        currentPage: pageNum,
        size: pageSize,
        title: title,
        subjectId: subjectId,
      };
      
      try {
        //教师分页获取讨论
        if(this.currentRole === 'teacher'){
          const res = await discussionpageOwner(params)
          this.data = res.data
        }else if(this.currentRole === 'student'){
          //学生分页获取讨论
          delete params.subjectId
          const res = await discussionpageStudent(params)
          this.data = res.data
        }
        this.errorMessage = '' // 清除错误信息
      } catch (error) {
        this.errorMessage = error.message || '没有权限访问该资源'
        this.data = { records: [], total: 0, size: this.pageSize, current: this.pageNum }
      }
    },
    handleSizeChange(val) {
      // 设置每页多少条逻辑
      this.pageSize = val;
      this.getDiscussionPage(this.pageNum, val,this.searchForm.searchTitle,
      this.searchForm.subjectId);
    },
    handleCurrentChange(val) {
      // 设置当前页逻辑
      this.pageNum = val;
      this.getDiscussionPage(val, this.pageSize,this.searchForm.searchTitle,
      this.searchForm.subjectId);
    },
    // 收到websocket消息的方法
    handleMessage(res) {
      if(res.type === 'DISCUSSION') {
        // 刷新讨论列表
        this.getDiscussionPage();
      }
    },
    // 处理输入事件，触发实时验证
    handleInput(field) {
      this.$refs.discussionFormRef.validateField(field);
    },
  },
};
</script>

<style scoped>
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
