<script setup>
/**
 * 讨论详情页面
 * 使用 Composition API 重构，提升代码可维护性
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import Discussion from '@/components/discussion'
import SafeHtml from '@/components/SafeHtml'
import { getDiscussionRely, discussionDetail } from '@/api/discussion'
import { getDiscussionId, setDiscussionId } from '@/utils/auth'
import { getRole } from '@/utils/jwtUtils'
import { EventBus } from '@/utils/eventBus'
import { replyAdd, replyDel } from '@/api/reply'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 响应式数据
const currentRole = ref(null)
const currentDiscussionId = ref(null)
const radio = ref('2')
const relyData = ref({})
const data = ref({})
const form = ref({
  discussionId: null,
  content: ''
})
const errorMessage = ref('')

// 富文本编辑器配置
const editorOption = ref({
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ indent: '-1' }, { indent: '+1' }],
      [{ color: [] }, { background: [] }],
      ['clean']
    ]
  },
  placeholder: '请输入正文'
})

// 计算属性
const displayTitle = computed(() => {
  return data.value.title === '' || data.value.title == null ? '暂无标题' : data.value.title
})

/**
 * 获取讨论详情
 */
const getDiscussionDetailsFun = async () => {
  try {
    const res = await discussionDetail(currentDiscussionId.value)
    form.value.content = res.data.answer
    data.value = res.data
    await getDiscussionRelyFun(currentDiscussionId.value, 1)
    errorMessage.value = ''
  } catch (error) {
    errorMessage.value = error.message || '没有权限访问该资源'
    data.value = {}
  }
}

/**
 * 获取回复列表
 * @param {string} id 讨论ID
 * @param {number} order 排序方式
 */
const getDiscussionRelyFun = async (id, order = 1) => {
  try {
    const res = await getDiscussionRely(id, order)
    relyData.value = res.data
    errorMessage.value = ''
  } catch (error) {
    errorMessage.value = error.message || '没有权限访问该资源'
    relyData.value = {}
  }
}

/**
 * 处理排序方式变更
 * @param {string} value 排序方式
 */
const handleRadioChange = (value) => {
  getDiscussionRelyFun(currentDiscussionId.value, value)
}

/**
 * 处理 WebSocket 消息
 * @param {Object} res 消息对象
 */
const handleMessage = (res) => {
  if (res.type === 'DISCUSSION' && res.data.discussionId === currentDiscussionId.value) {
    getDiscussionRelyFun(currentDiscussionId.value, 2)
  }
}

/**
 * 投屏模式
 */
const projectionScreen = () => {
  router.push({ name: 'discussion-block', query: { discussionId: currentDiscussionId.value } })
}

/**
 * 删除回复
 * @param {number} id 回复ID
 */
const delReply = async (id) => {
  try {
    const res = await replyDel(id)
    if (res.code) {
      ElMessage({ type: 'success', message: res.msg })
      try {
        window.$sendMessage?.({ type: 'DISCUSSION', data: { discussionId: currentDiscussionId.value } })
      } catch (error) {
        console.log('WebSocket发送消息失败，已尝试重新连接:', error)
      }
    } else {
      errorMessage.value = res.msg
    }
  } catch (error) {
    errorMessage.value = error.message || '操作失败，请重试'
  }
}

/**
 * 确认删除回复
 * @param {number} replyId 回复ID
 */
const showIsDel = (replyId) => {
  ElMessageBox.confirm('此操作将永久删除该回复, 是否继续?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delReply(replyId)
  }).catch(() => {
    ElMessage({ type: 'info', message: '已取消删除' })
  })
}

/**
 * 提交回复
 */
const submitFun = async () => {
  if (!form.value.content || form.value.content.trim() === '<p><br></p>' || form.value.content.trim() === '') {
    ElMessage({ type: 'warning', message: '讨论内容不能为空' })
    return
  }

  form.value.discussionId = currentDiscussionId.value

  try {
    const res = await replyAdd(form.value)
    if (res && res.code) {
      ElMessage({ type: 'success', message: res.msg })
      try {
        window.$sendMessage?.({ type: 'DISCUSSION', data: { discussionId: currentDiscussionId.value } })
      } catch (error) {
        console.log('WebSocket发送消息失败，已尝试重新连接:', error)
      }
      await getDiscussionRelyFun(currentDiscussionId.value, radio.value)
      form.value.content = ''
    } else {
      errorMessage.value = res.msg || '操作失败，请重试'
    }
  } catch (error) {
    errorMessage.value = error.message || '操作失败，请重试'
  }
}

// 富文本编辑器事件
const onEditorBlur = () => {}
const onEditorFocus = () => {}
const onEditorReady = () => {}
const onEditorChange = ({ html }) => {
  form.value.content = html
}

// 生命周期钩子
onMounted(() => {
  currentDiscussionId.value = route.query.discussionId

  // 获取角色判断是否是教师和管理员
  const role = getRole()
  if (role === 3 || role === 2) {
    currentRole.value = 'teacher'
  }

  if (currentDiscussionId.value) {
    setDiscussionId(currentDiscussionId.value)
  } else {
    currentDiscussionId.value = getDiscussionId()
  }

  getDiscussionDetailsFun()

  // 监听 WebSocket 消息
  EventBus.on('websocket-message', handleMessage)
})

onUnmounted(() => {
  EventBus.off('websocket-message', handleMessage)
})
</script>

<template>
  <div class="subPageMain" style="background: none">
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

    <div class="noticeDetail_detail">
      <div class="noticeDetail_head">
        <div>{{ displayTitle }}</div>
        <div v-if="currentRole === 'teacher'">
          <el-button type="primary" @click="projectionScreen">投屏模式</el-button>
        </div>
      </div>
      <div class="noticeDetail_main">
        <SafeHtml v-if="data.content != null && data.content !== ''" :content="data.content" />
        <div v-else>此处暂无内容</div>
      </div>
    </div>

    <!-- 回复输入框 -->
    <div class="edit_main">
      <div class="replyEdit" style="margin-bottom: 50px;">
        <QuillEditor
          ref="myQuillEditor"
          v-model:content="form.content"
          content-type="html"
          :options="editorOption"
          class="my-quill-editor"
          @blur="onEditorBlur"
          @focus="onEditorFocus"
          @ready="onEditorReady"
          @change="onEditorChange"
          style="height: 120px;"
        />
      </div>
      <div class="replyEditBtnGroup">
        <div class="replyBtn" style="margin: 10px 0;">
          <el-button type="primary" @click="submitFun">发 送</el-button>
        </div>
      </div>
    </div>

    <!-- 排序选项 -->
    <div class="radio-class">
      <div style="margin-bottom: 10px;">全部回答</div>
      <div>
        <el-radio-group v-model="radio" @change="handleRadioChange">
          <el-radio label="2">按提交时间降序显示</el-radio>
          <el-radio label="1">按提交时间升序显示</el-radio>
          <el-radio label="4">按点赞数量降序显示</el-radio>
          <el-radio label="3">按点赞数量升序显示</el-radio>
        </el-radio-group>
      </div>
    </div>

    <!-- 回复列表 -->
    <div v-for="item in relyData" :key="item.id">
      <Discussion
        :discussionData="item"
        :delFun="showIsDel"
        :discussionId="data.id"
        :onConfirm="getDiscussionDetailsFun"
        style="margin: 10px 0;"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.radio-class {
  display: flex;
  margin-left: 10px;
  flex-direction: column;
  margin-bottom: 16px;
}

@media (max-width: 767px) {
  .radio-class .el-radio-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .radio-class .el-radio {
    margin-right: 0;
  }
}

.subPageMain {
  position: relative;
  padding-top: 25px;
  width: 1200px;
  max-width: 100%;
  min-height: 600px;
  margin: 30px auto;

  @media (max-width: 1199px) {
    width: 100%;
    padding: 15px;
    margin: 15px auto;
  }

  .noticeDetail_detail {
    background-color: #f5f6f8;
    margin: 16px auto;
    border-radius: 8px;

    .noticeDetail_head {
      display: flex;
      justify-content: space-between;
      padding: 25px 30px;
    }

    .noticeDetail_main {
      padding: 16px 30px 30px;
    }
  }

  .edit_main {
    margin: 16px auto;

    .replyEdit {
      height: 170px;

      .my-quill-editor {
        height: 80%;
      }

      .ql-editor {
        height: 120px;
      }
    }

    .replyEditBtnGroup {
      height: 36px;

      .replyBtn {
        float: right;
        margin-top: 20px;
      }
    }
  }
}
</style>
