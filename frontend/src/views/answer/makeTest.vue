<template>
  <div class="container">
    <!-- 左侧题号信息栏 -->
    <div class="left-panel">
      <div class="card">
        <div class="content">
          <el-divider />
          <el-skeleton v-if="loading" animated :rows="6" />
          <div v-else>
            <p class="stats">
              共 <span class="highlight">{{ waitQuList.length }}</span> 道简答题, 共
              <span class="highlight">{{
                waitQuList.length > 0 ? waitQuList.reduce((sum, item) => sum + item.totalScore, 0) : 0
              }}</span>
              分
            </p>
            <div class="question-tags">
              <el-tag
                v-for="(item, index) in waitQuList"
                :key="index"
                :type="index === quIndex ? 'success' : ''"
                class="tag"
                @click="handleTag(index)"
              >
                {{ index + 1 }}
              </el-tag>
            </div>
            <el-button type="success" class="submit-btn" @click="subCorrect" :disabled="waitQuList.length === 0">提交批改</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧题目信息显示区域 -->
    <div class="right-panel">
      <el-card class="exam-card">
        <el-skeleton v-if="loading" animated :rows="10" />
        <div v-else class="right-content">
          <div v-if="waitQuList.length === 0" class="empty-state">
            <el-empty description="暂无作答信息" />
          </div>
          <div v-else class="questions-container" ref="questionsContainer">
            <template v-for="(item, index) in waitQuList" :key="item.quId">
              <div :class="['question-item', 'question-' + index]" :id="'question-' + index">
                <!-- 题目内容 -->
                <el-row :gutter="24">
                  <el-col :span="24" style="text-align: left">
                    <!-- 题目: 序号、类型、题干 -->
                    <div class="question-header">
                      <div class="question-content">
                        {{ index + 1 }}. {{ item.quTitle }}
                        <el-tag size="small" type="info" class="question-type-tag">
                          {{ getQuestionType(item.quType) }}
                        </el-tag>
                        <el-tag size="small" type="warning" class="question-score-tag">
                          {{ item.totalScore || 0 }} 分
                        </el-tag>
                      </div>
                    </div>
                    <div class="answer-content">
                      {{ item.answer || '无作答' }}
                    </div>
                    <!-- 题目解析 -->
                    <div class="analysis-section">
                      <el-card class="analysis-card">
                        <div class="score-section">
                          <span class="score-label">分数：</span>
                          <el-input
                            v-model="item.correctScore"
                            type="number"
                            class="score-input"
                            :min="0"
                            :max="item.totalScore"
                          />
                          <span
                            v-if="item.correctScore < 0 ||
                              item.correctScore > item.totalScore "
                            class="score-error"
                          >
                            评分只能在 0-{{ item.totalScore }}之间
                          </span>
                        </div>
                        <div class="reference-section">
                          <span class="reference-label">参考答案:</span>
                          <span class="reference-content">{{ item.refAnswer || '无参考答案' }}</span>
                        </div>
                      </el-card>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </template>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { answerDetail, correct } from '@/api/answer'
export default {
  name: 'AnswerGrading',
  data() {
    return {
      quIndex: -1,
      // 考试信息
      info: {},
      // 待批改试题
      waitQuList: [],
      scoreData: null,
      // 加载状态
      loading: false
    }
  },
  created() {
    this.info = JSON.parse(sessionStorage.getItem('answer_info'))
    if (this.info) {
      this.getUserAnswerDetail()
    } else {
      console.error('未获取到学生信息')
      this.$message.error('未获取到学生信息，请重新选择')
      this.$router.push({ name: 'answer-show' })
    }
  },
  mounted() {
    // 监听滚动事件，高亮当前可见的题目
    this.addScrollListener()
  },
  beforeUnmount() {
    // 移除滚动监听
    this.removeScrollListener()
  },
  methods: {
    // 添加滚动监听
    addScrollListener() {
      const container = this.$refs.questionsContainer
      if (container) {
        container.addEventListener('scroll', this.handleScroll)
      }
    },
    // 移除滚动监听
    removeScrollListener() {
      const container = this.$refs.questionsContainer
      if (container) {
        container.removeEventListener('scroll', this.handleScroll)
      }
    },
    // 处理滚动事件
    handleScroll() {
      const container = this.$refs.questionsContainer
      if (!container || this.waitQuList.length === 0) return
      
      const scrollTop = container.scrollTop
      const containerHeight = container.clientHeight
      
      // 找到当前可见的题目
      for (let i = 0; i < this.waitQuList.length; i++) {
        const questionEl = document.querySelector('.question-' + i)
        if (!questionEl) continue
        
        const offsetTop = questionEl.offsetTop
        
        // 如果题目在可视区域内
        if (offsetTop >= scrollTop - 50 && offsetTop < scrollTop + containerHeight - 100) {
          if (this.quIndex !== i) {
            this.quIndex = i
          }
          break
        }
      }
    },
    // 点击答题卡题号, 右侧题目滑动
    handleTag(index) {
      // 高亮选中的题目index标签
      this.quIndex = index
      
      // 获取容器和对应的题目元素
      const container = this.$refs.questionsContainer
      const questionEl = document.querySelector('.question-' + index)
      
      if (container && questionEl) {
        // 计算滚动位置
        const offsetTop = questionEl.offsetTop
        container.scrollTo({
          top: offsetTop,
          behavior: 'smooth' // 平滑滚动
        })
      }
    },
    // 获取题目类型名称
    getQuestionType(quType) {
      switch (quType) {
        case 1: return '单选题'
        case 2: return '多选题'
        case 3: return '判断题'
        case 4: return '简答题'
        default: return '未知题型'
      }
    },
    // 过滤出需要人工批改的题目（简答题）
    getManualQuestions() {
      return this.waitQuList.filter(item => item.quType === 4)
    },
    // 获取用户作答信息
    async getUserAnswerDetail() {
      this.loading = true
      try {
        console.log('获取作答信息参数:', this.info)
        const params = { userId: this.info.userId, examId: this.info.examId }
        console.log('调用API参数:', params)
        const res = await answerDetail(params)
        console.log('获取作答信息结果:', res)
        // 后端返回code为1表示成功
        if (res && res.code === 1) {
          // 即使data为空数组，也视为成功响应
          let allQuestions = res.data || []
          console.log('所有试题列表:', allQuestions)
          // 筛选出只需要批改的简答题（quType === 4）
          this.waitQuList = allQuestions.filter(item => item.quType === 4)
          console.log('待批改简答题列表:', this.waitQuList)
          // 为每个题目设置默认值，确保得分和本题分数正确显示
          this.waitQuList.forEach(item => {
            // 确保correctScore有值，默认为0
            if (item.correctScore === undefined || item.correctScore === null) {
              item.correctScore = 0
            }
            // 确保totalScore有值，默认为0
            if (item.totalScore === undefined || item.totalScore === null) {
              item.totalScore = 0
            }
          })
          console.log('处理后试题列表（含预设分数）:', this.waitQuList)
          // 默认选中第一题
          if (this.waitQuList.length > 0) {
            this.quIndex = 0
          } else {
            // 无作答信息时显示提示
            this.$message.info('该学生暂无需要批改的简答题')
          }
          
          // 数据加载完成后，等待DOM更新，重新计算滚动位置
          this.$nextTick(() => {
            this.addScrollListener()
          })
        } else {
          console.error('获取作答信息失败:', res)
          this.$message.error(`获取作答信息失败: ${res?.msg || '未知错误'}`)
          this.waitQuList = []
        }
      } catch (error) {
        console.error('获取作答信息出错:', error)
        this.$message.error(`获取作答信息出错: ${error.message}`)
        this.waitQuList = []
      } finally {
        this.loading = false
      }
    },
    subCorrect() {
      // 只提交简答题的评分
      const manualQuestions = this.getManualQuestions()
      const list = []
      
      // 校验合法
      for (let i = 0; i < manualQuestions.length; i++) {
        const element = manualQuestions[i]
        if (element.correctScore === undefined || element.correctScore === null) {
          // 显示警告的操作
          this.$message({
            message: `请先给第${i + 1}题评分`,
            type: 'error'
          })
          return
        }
        if (element.correctScore < 0 || element.correctScore > element.totalScore) {
          this.$message({
            message: `第${i + 1}题的评分只能在0-${element.totalScore}之间`,
            type: 'error'
          })
          return
        }
        const obj = {
          userId: element.userId,
          examId: element.examId,
          questionId: element.quId,
          score: element.correctScore
        }
        list.push(obj)
      }
      
      if (list.length === 0) {
        this.$message({
          message: '没有需要批改的简答题',
          type: 'info'
        })
        return
      }
      
      // 发送请求
      correct(list).then((res) => {
        if (res.code) {
          this.$notify({
            title: '成功',
            message: `${res.msg}`,
            type: 'success',
            duration: 2000
          })
          this.$router.push({ name: 'answer-show' })
        } else {
          this.$notify({
            title: '失败',
            message: `${res.msg}`,
            type: 'error',
            duration: 2000
          })
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
// 容器布局
.container {
  display: flex;
  height: calc(100vh - 60px);
  padding: 20px;
  gap: 20px;
  box-sizing: border-box;
  overflow: hidden; // 防止整个页面滚动
}

// 左侧面板
.left-panel {
  width: 250px;
  flex-shrink: 0;
  height: 100%;
  overflow: hidden; // 防止左侧面板溢出
  
  .card {
    width: 100%;
    height: 100%;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    .content {
      padding: 20px;
      flex: 1;
      overflow-y: auto; // 左侧内容滚动
      height: 100%;
      
      // 左侧滚动条样式
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #c1c1c1;
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb:hover {
        background: #a8a8a8;
      }
    }
  }
}

// 右侧面板
.right-panel {
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow: hidden;
  
  .exam-card {
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    :deep(.el-card__body) {
      height: 100%;
      overflow: hidden;
      padding: 20px;
      display: flex;
      flex-direction: column;
    }
  }
}

// 右侧内容容器
.right-content {
  flex: 1;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

// 统计信息
.stats {
  margin-bottom: 20px;
  line-height: 1.5;
  
  .highlight {
    color: #1890ff;
    font-weight: bold;
  }
}

// 题目标签
.question-tags {
  margin-bottom: 25px;
  
  .tag {
    margin-right: 8px;
    margin-bottom: 8px;
    cursor: pointer;
  }
}

// 提交按钮
.submit-btn {
  width: 100%;
  margin-top: 10px;
}

// 空状态
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 400px;
}

// 题目容器
.questions-container {
  flex: 1;
  overflow-y: auto; // 启用垂直滚动
  padding-right: 10px;
  height: 100%;
  
  // 滚动条样式
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

// 题目项
.question-item {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
  
  .question-header {
    .question-type-tag {
      margin-left: 10px;
    }
  }
}

// 题目类型标签
.question-type-tag {
  margin-left: 10px;
}

// 题目头部
.question-header {
  margin-bottom: 15px;
  
  .question-content {
    font-size: 16px;
    font-weight: 500;
    line-height: 1.5;
  }
}

// 作答内容
.answer-content {
  width: 100%;
  min-height: 100px;
  border: 1px solid #0a84ff;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
  background-color: #f9f9f9;
  font-size: 14px;
  line-height: 1.5;
  box-sizing: border-box;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

// 分析部分
.analysis-section {
  margin-bottom: 10px;
  
  .analysis-card {
    border-radius: 4px;
    
    .score-section {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .score-label {
        color: #e6a23c;
        font-weight: 500;
        margin-right: 15px;
      }
      
      .score-input {
        width: 100px;
        margin-right: 15px;
      }
      
      .score-error {
        color: #f56c6c;
        font-size: 12px;
      }
    }
    
    .reference-section {
        .reference-label {
          font-weight: 500;
          margin-right: 10px;
        }
        
        .reference-content {
          margin-bottom: 15px;
          line-height: 1.5;
        }
      }
  }
}

// 题目分数标签
.question-score-tag {
  margin-left: 10px;
}

// 响应式设计
@media (max-width: 1200px) {
  .container {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 60px);
    overflow: auto;
  }
  
  .left-panel {
    width: 100%;
    height: auto;
    
    .card {
      height: auto;
      
      .content {
        height: auto;
        max-height: 300px;
      }
    }
  }
  
  .right-panel {
    min-height: 600px;
    height: auto;
    
    .exam-card {
      height: auto;
      
      :deep(.el-card__body) {
        height: auto;
      }
    }
  }
  
  .questions-container {
    max-height: 800px;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 10px;
    gap: 10px;
  }
  
  .left-panel {
    .card {
      .content {
        padding: 15px;
      }
    }
  }
  
  .question-item {
    margin-bottom: 20px;
    padding-bottom: 15px;
  }
  
  .answer-content {
    min-height: 80px;
    padding: 10px;
  }
  
  .score-input {
    width: 80px;
  }
}
</style>