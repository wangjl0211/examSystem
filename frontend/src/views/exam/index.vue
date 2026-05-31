<template>
  <div style="width: 100%; min-height: 100vh; background-color: #f8f9fa; padding: 20px 0 20px">
    <!-- Header区域 -->
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card style="margin-bottom: 10px">
          距离考试结束还有：
          <exam-timer 
            v-if="examStartTime > 0 && examDuration > 0"
            :start-time="examStartTime"
            :duration="examDuration"
            :sync-interval="30000"
            @timeout="doHandler(true)"
            @tick="handleTimerTick"
          />
          <span v-else style="color: #999">加载中...</span>
        </el-card>
      </el-col>

      <!-- 答题卡区域 -->
      <el-col :span="5" :xs="24" style="margin-bottom: 10px">
        <el-card class="content-h">
          <p class="card-title">答题卡</p>
          <el-row :gutter="24" class="card-line" style="padding-left: 10px">
            <el-tag type="info">未作答</el-tag>
            <el-tag type="success">已作答</el-tag>
          </el-row>

          <!-- 单选题答题卡 -->
          <question-card-section
            v-if="hasQuestions(paperData.radioList)"
            title="单选题"
            :questions="paperData.radioList"
            :answer-store="answerStore"
            @select-question="handleCardClick"
          />

          <!-- 多选题答题卡 -->
          <question-card-section
            v-if="hasQuestions(paperData.multiList)"
            title="多选题"
            :questions="paperData.multiList"
            :answer-store="answerStore"
            @select-question="handleCardClick"
          />

          <!-- 判断题答题卡 -->
          <question-card-section
            v-if="hasQuestions(paperData.judgeList)"
            title="判断题"
            :questions="paperData.judgeList"
            :answer-store="answerStore"
            @select-question="handleCardClick"
          />

          <!-- 简答题答题卡 -->
          <question-card-section
            v-if="hasQuestions(paperData.saqList)"
            title="简答题"
            :questions="paperData.saqList"
            :answer-store="answerStore"
            @select-question="handleCardClick"
          />
        </el-card>
      </el-col>

      <!-- 所有题目区域 -->
      <el-col :span="19" :xs="24">
        <el-card class="qu-content">
          <!-- 提交前汇总对话框 -->
          <exam-summary-dialog
            v-model:visible="examPreVisible"
            :record-data="recordData"
            @close="handleClose"
            @confirm="doHandler"
          />
          
          <exam-question-section
            title="单选题"
            :questions="paperData.radioList"
            :qu-type="1"
            :answers="answers"
            @answer-change="handleAnswerChange"
            @input-change="handleInputChange"
          />
          <exam-question-section
            title="多选题"
            :questions="paperData.multiList"
            :qu-type="2"
            :answers="answers"
            @answer-change="handleAnswerChange"
            @input-change="handleInputChange"
          />
          <exam-question-section
            title="判断题"
            :questions="paperData.judgeList"
            :qu-type="3"
            :answers="answers"
            @answer-change="handleAnswerChange"
            @input-change="handleInputChange"
          />
          <exam-question-section
            title="简答题"
            :questions="paperData.saqList"
            :qu-type="4"
            :answers="answers"
            @answer-change="handleAnswerChange"
            @input-change="handleInputChange"
          />
          
          <!-- 提交按钮 -->
          <div style="margin-top: 30px; text-align: center">
            <el-button type="primary" size="large" @click="handHandExamPre()">
              {{ handleText }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 切屏弹窗 -->
    <el-dialog
      title="切屏提示"
      v-model="tipsFlag"
      width="480px"
      class="commonDialog multi clickLight"
      center
      :close-on-click-modal="false"
    >
      {{ examMeg }}
    </el-dialog>
  </div>
</template>

<script>
import {
  handExam,
  fillAnswer,
  examCollect,
  examCheat,
  examQuList,
  getServerTime
} from '@/api/exam'
import { ElMessage, ElMessageBox, ElRadio, ElRadioGroup, ElCheckbox, ElCheckboxGroup } from 'element-plus'
import ExamTimer from '@/components/ExamTimer'
import QuestionCardSection from './components/QuestionCardSection'
import ExamSummaryDialog from './components/ExamSummaryDialog'
import ExamQuestionSection from '@/components/exam/ExamQuestionSection'
import { EventBus } from '@/utils/eventBus'
import { debounce } from 'lodash-es'
import { hasQuestions, numberToLetter } from '@/utils/questionFormat'
import { useExamAntiCheat } from '@/composables/useExamAntiCheat'

export default {
  name: 'ExamTaking',
  components: {
    ExamTimer,
    QuestionCardSection,
    ExamSummaryDialog,
    ExamQuestionSection,
    ElRadio,
    ElRadioGroup,
    ElCheckbox,
    ElCheckboxGroup
  },
  data() {
    return {
      examId: '',
      receivedRow: null,
      // 全屏/不全屏
      isFullscreen: false,
      showPrevious: false,
      showNext: true,
      loading: false,
      handleText: '交卷',
      pageLoading: false,
      // 试卷ID
      paperId: '',
      allItem: [],
      tipsFlag: false,
      examPreVisible: false,
      testData: {},
      pkExam: null,
      examMeg: '',
      // 试卷信息
      paperData: {
        leftSeconds: 99999,
        radioList: [],
        multiList: [],
        judgeList: [],
        saqList: []
      },
      // 所有题目的答案
      answers: {},
      // 已答ID
      answeredIds: [],
      recordData: null,
      //
      submittedAnswers: {},
      // 题目作答状态
      answerStatus: {},
      // 统一的答题数据存储（标准化数据结构）
      answerStore: {},
      // 用户进入考试时间
      enteredAt: Date.now(),
      // 自动保存定时器
      autoSaveTimer: null,
      // 预设的总考试时长（秒）
      totalExamSeconds: 0,
      // 服务器时间偏移量（服务器时间 - 本地时间）
      serverTimeOffset: 0,
      // 是否已同步服务器时间
      isServerTimeSynced: false,
      // 考试开始时间的时间戳（毫秒）- 用于计时器
      examStartTime: 0,
      // 考试时长（秒）- 用于计时器
      examDuration: 0,
    }
  },
  created() {
    // 从多个来源获取examId，优先级：路由参数 > localStorage
    this.examId = this.$route.params.id || localStorage.getItem('examInfo_examId') || localStorage.getItem('examId')
    this.paperId = this.examId
    console.log('=== 考试页面初始化 ===')
    console.log('考试ID:', this.examId)
    console.log('路由参数:', this.$route.params)
    if (this.examId) {
      // 记录用户已进入答题页面
      this.recordUserEnteredExam(this.examId)
      this.startExam(this.examId)
      this.fetchData(this.examId)
    } else {
      console.error('=== 考试ID不存在 ===')
      this.$router.push({ name: 'text-center' })
    }
    
    this._antiCheat = useExamAntiCheat(() => this.examId, (res) => this.handleCheatResponse(res))

    // 监听WebSocket消息
    this.wsMessageHandler = this.handleWebSocketMessage.bind(this)
    EventBus.on('websocket-message', this.wsMessageHandler)
  },
  mounted() {
    this.$nextTick(() => {
      const body = document.querySelector('body')
      body.style.overflow = 'auto'
    })

    if (this._antiCheat) {
      this._antiCheat.enableAntiCheat()
    }
    
    // 启动自动保存定时器（每30秒保存一次）
    this.autoSaveTimer = setInterval(() => {
      this.saveAnswersToLocalStorage()
      this.syncAllUnsavedAnswers()
      console.log('[自动保存] 执行定期保存')
    }, 30000)
  },
  beforeUnmount() {
    clearInterval(this.countdownTime)

    if (this._antiCheat) {
      this._antiCheat.disableAntiCheat()
    }
    
    // 清理自动保存定时器
    if (this.autoSaveTimer) {
      clearInterval(this.autoSaveTimer)
    }
    
    // 页面离开前强制保存所有数据
    this.saveAnswersToLocalStorage()
    this.syncAllUnsavedAnswers()
    
    // 移除WebSocket事件监听
    if (this.wsMessageHandler) {
      EventBus.off('websocket-message', this.wsMessageHandler)
    }
  },
  methods: {
    numberToLetter,
    handleCheatResponse(res) {
      if (res.code) {
        this.examMeg = res.msg
        this.tipsFlag = true
        if (res.data) {
          this.$router.push({
            name: 'text-center',
            params: { id: this.paperId }
          })
        }
      }
    },

    
    // 处理WebSocket消息
    handleWebSocketMessage(message) {
      if (message.type === 'EXAM_DELETED') {
        ElMessageBox.alert(
          '您正在参加的考试已被教师删除，将跳转到试卷中心页面',
          '考试通知',
          {
            confirmButtonText: '确定',
            type: 'error',
            callback: () => {
              window.location.href = '/text-center'
            }
          }
        )
      }
    },
    // 检查问题列表是否存在
    hasQuestions,

    // 检查选项是否被选中
    isCheck(myOption, sort) {
      if (!myOption) return false
      const arr = myOption.split(',').map(Number)
      return arr.includes(sort)
    },

    // 处理对话框关闭
    handleClose() {
      this.examPreVisible = false
    },

    /**
     * 处理输入变化
     * @param {string} val - 输入值
     * @param {number} questionId - 题目ID
     */
    handleInputChange(val, questionId) {
      // 这里可以添加额外的输入处理逻辑
      console.log(`[输入变化] 题目${questionId}:`, val ? val.length : 0)
    },

    /**
     * 统一答案处理核心方法
     * @param {Object} payload - 答案数据
     * @param {number} payload.questionId - 题目ID
     * @param {any} payload.answer - 原始答案
     * @param {number} payload.quType - 题目类型
     * @param {boolean} payload.saveToServer - 是否保存到服务器
     */
    handleAnswerChange(payload) {
      const { questionId, answer, quType, saveToServer = true } = payload
      
      // 1. 获取题目信息
      const question = this.allItem.find(q => q.questionId === questionId)
      if (!question) return
      
      // 2. 长度验证（仅针对简答题）
      if (quType === 4) {
        const maxLength = 500
        if (answer.length > maxLength) {
          console.warn(`[长度超限] 题目${questionId}: 输入长度${answer.length}超过限制${maxLength}`)
          // 截取到最大长度
          const trimmedAnswer = answer.substring(0, maxLength)
          this.answers[questionId] = trimmedAnswer
          return
        }
      }
      
      // 3. 判断是否已作答
      const hasAnswer = this.checkHasAnswer(answer, quType)
      
      // 4. 转换显示格式
      const { selectedKey, selectedValue } = this.formatAnswerForDisplay(answer, quType, question.options)
      
      // 5. 更新统一数据源
      this.answerStore[questionId] = {
        questionId,
        quType,
        selectedKey: hasAnswer ? selectedKey : '',
        selectedValue: hasAnswer ? selectedValue : '',
        selectedRaw: answer,
        isAnswered: hasAnswer,
        lastUpdateTime: Date.now(),
        syncedWithServer: false
      }
      
      // 6. 同步更新answers用于v-model绑定
      this.answers[questionId] = answer
      
      // 7. 立即更新答题卡状态
      this.updateQuestionCheckoutStatus(questionId, hasAnswer)
      
      // 8. 强制更新视图
      this.$forceUpdate()
      
      // 9. 保存到本地存储
      this.saveAnswersToLocalStorage()
      
      // 10. 保存到服务器
      if (saveToServer && hasAnswer) {
        this.debouncedSaveToServer(questionId, answer, quType)
      }
      
      console.log(`[答案更新] 题目${questionId}:`, this.answerStore[questionId])
    },

    /**
     * 检查是否有效答案
     */
    checkHasAnswer(answer, quType) {
      if (answer === null || answer === undefined) return false
      if (quType === 4) return typeof answer === 'string' && answer.trim() !== ''
      if (quType === 2) return Array.isArray(answer) && answer.length > 0
      return answer !== ''
    },

    /**
     * 格式化答案用于显示
     */
    formatAnswerForDisplay(answer, quType, options = []) {
      let selectedKey = ''
      let selectedValue = ''
      
      if (!this.checkHasAnswer(answer, quType)) {
        return { selectedKey, selectedValue }
      }
      
      const optionMap = new Map(options.map(opt => [opt.id, opt]))
      
      if (quType === 4) {
        // 简答题
        selectedKey = answer.trim()
        selectedValue = answer.trim()
      } else if (quType === 2) {
        // 多选题
        const selectedOptions = answer
          .map(id => optionMap.get(id))
          .filter(opt => opt !== undefined)
          .sort((a, b) => a.sort - b.sort)
        
        selectedKey = selectedOptions.map(opt => this.numberToLetter(opt.sort)).join(',')
        selectedValue = selectedOptions.map(opt => opt.content).join(',')
      } else {
        // 单选题、判断题
        const selectedOption = optionMap.get(answer)
        if (selectedOption) {
          selectedKey = this.numberToLetter(selectedOption.sort)
          selectedValue = selectedOption.content
        }
      }
      
      return { selectedKey, selectedValue }
    },

    /**
     * 更新题目checkout状态（用于答题卡）
     */
    updateQuestionCheckoutStatus(questionId, isAnswered) {
      const status = isAnswered ? 1 : 0
      
      const updateList = (list) => {
        if (list && list.length) {
          const question = list.find(q => q.questionId === questionId)
          if (question) question.checkout = status
        }
      }
      
      updateList(this.paperData.radioList)
      updateList(this.paperData.multiList)
      updateList(this.paperData.judgeList)
      updateList(this.paperData.saqList)
    },

    /**
     * 防抖保存答案到服务器
     */
    debouncedSaveToServer: debounce(function(questionId, answer, quType) {
      this.saveAnswerToServer(questionId, answer, quType)
    }, 800),

    /**
     * 保存答案到本地存储
     */
    saveAnswersToLocalStorage() {
      if (!this.examId) return
      
      const examData = {
        examId: this.examId,
        paperId: this.paperId,
        answerStore: this.answerStore,
        answers: this.answers,
        paperData: {
          leftSeconds: this.paperData.leftSeconds,
          radioList: (this.paperData.radioList || []).map(q => ({
            questionId: q.questionId,
            checkout: q.checkout,
            sort: q.sort
          })),
          multiList: (this.paperData.multiList || []).map(q => ({
            questionId: q.questionId,
            checkout: q.checkout,
            sort: q.sort
          })),
          judgeList: (this.paperData.judgeList || []).map(q => ({
            questionId: q.questionId,
            checkout: q.checkout,
            sort: q.sort
          })),
          saqList: (this.paperData.saqList || []).map(q => ({
            questionId: q.questionId,
            checkout: q.checkout,
            sort: q.sort
          }))
        },
        lastSaveTime: Date.now(),
        enteredAt: this.enteredAt,
        totalExamSeconds: this.totalExamSeconds // 保存预设的总考试时长
      }
      
      try {
        localStorage.setItem(`exam_${this.examId}_answers`, JSON.stringify(examData))
        console.log('[本地存储] 答案保存成功')
      } catch (error) {
        console.error('[本地存储] 答案保存失败:', error)
      }
    },

    /**
     * 从本地存储加载答案数据
     */
    loadAnswersFromLocalStorage() {
      if (!this.examId) return false
      
      try {
        const storedData = localStorage.getItem(`exam_${this.examId}_answers`)
        if (!storedData) return false
        
        const examData = JSON.parse(storedData)  
        // 验证数据有效性
        if (examData.examId !== this.examId) return false
        
        // 恢复答案数据
        if (examData.answerStore) {
          this.answerStore = examData.answerStore
        }
        
        if (examData.answers) {
          this.answers = examData.answers
        }
        // 恢复作答时间
        if (examData.enteredAt) {
          this.enteredAt = examData.enteredAt
        }
        
        // 恢复总考试时长
        if (examData.totalExamSeconds) {
          this.totalExamSeconds = examData.totalExamSeconds
          console.log('[本地存储] 恢复总考试时长:', this.totalExamSeconds)
        }
        // 恢复题目作答状态
        if (examData.paperData) {
          // 恢复剩余时间，确保不超过总考试时长
          if (examData.paperData.leftSeconds) {
            // 计算用户离开的时间
            const timeElapsed = Math.floor((Date.now() - (examData.lastSaveTime || Date.now())) / 1000)
            console.log('[本地存储] 用户离开时间:', timeElapsed, '秒')
            
            // 计算新的剩余时间
            let newLeftSeconds = examData.paperData.leftSeconds - timeElapsed
            // 确保剩余时间不会超过总考试时长，也不会小于0
            if (this.totalExamSeconds > 0) {
              newLeftSeconds = Math.min(newLeftSeconds, this.totalExamSeconds)
            }
            newLeftSeconds = Math.max(newLeftSeconds, 0)
            this.paperData.leftSeconds = newLeftSeconds
            console.log('[本地存储] 恢复剩余时间:', this.paperData.leftSeconds)
          }
          // 恢复各题型的作答状态
          const restoreCheckoutStatus = (list, storedList) => {
            if (list && storedList) {
              storedList.forEach(storedQ => {
                const question = list.find(q => q.questionId === storedQ.questionId)
                if (question) {
                  question.checkout = storedQ.checkout
                }
              })
            }
          }
          
          restoreCheckoutStatus(this.paperData.radioList, examData.paperData.radioList)
          restoreCheckoutStatus(this.paperData.multiList, examData.paperData.multiList)
          restoreCheckoutStatus(this.paperData.judgeList, examData.paperData.judgeList)
          restoreCheckoutStatus(this.paperData.saqList, examData.paperData.saqList)
        }
        
        console.log('[本地存储] 答案数据恢复成功')
        return true
      } catch (error) {
        console.error('[本地存储] 答案数据加载失败:', error)
        return false
      }
    },

    /**
     * 清除本地存储的答案数据
     */
    clearLocalStorageAnswers() {
      if (!this.examId) return
      
      try {
        localStorage.removeItem(`exam_${this.examId}_answers`)
        console.log('[本地存储] 答案数据清除成功')
      } catch (error) {
        console.error('[本地存储] 答案数据清除失败:', error)
      }
    },

    /**
     * 保存答案到服务器 - 返回Promise以便等待
     */
    async saveAnswerToServer(questionId, answer, quType) {
      if (!this.paperId) return
      
      let answerStr = ''
      if (quType === 2) {
        // 多选题排序后保存
        answerStr = [...answer].sort((a, b) => a - b).join(',')
      } else {
        answerStr = String(answer || '').trim()
      }
      
      // 空答案不保存
      if (!answerStr && quType !== 2) {
        return
      }
      
      try {
        const res = await fillAnswer({
          examId: this.paperId,
          quId: questionId,
          answer: answerStr
        })
        
        if (res.code) {
          // 更新同步状态
          if (this.answerStore[questionId]) {
            this.answerStore[questionId].syncedWithServer = true
          }
          // 记录已提交
          this.submittedAnswers[questionId] = answerStr
          sessionStorage.setItem('exam_' + questionId, '1')
          console.log(`[保存成功] 题目${questionId}: ${answerStr}`)
          return true
        } else {
          console.error(`[保存失败] 题目${questionId}:`, res.msg)
          return false
        }
      } catch (error) {
        console.error(`[保存异常] 题目${questionId}:`, error)
        throw error // 抛出异常，让调用方知道失败
      }
    },

    /**
     * 强制同步所有未同步的答案 - 改为异步等待
     */
    async syncAllUnsavedAnswers() {
      const unsavedQuestions = Object.entries(this.answerStore)
        .filter(([_, data]) => !data.syncedWithServer)
      
      if (unsavedQuestions.length === 0) return
      
      console.log(`强制同步 ${unsavedQuestions.length} 个未保存答案`)
      
      // 使用 Promise.all 并行保存，提高效率
      const savePromises = unsavedQuestions.map(([questionId, data]) => {
        return this.saveAnswerToServer(
          parseInt(questionId), 
          data.selectedRaw, 
          data.quType
        ).catch(error => {
          console.error(`同步题目 ${questionId} 失败:`, error)
          // 检查是否是登录过期错误
          if (error.message.includes('登陆信息已过期')) {
            // 清除自动保存定时器
            if (this.autoSaveTimer) {
              clearInterval(this.autoSaveTimer)
              this.autoSaveTimer = null
            }
            // 显示登录过期提示
            ElMessageBox.alert(
              '您的登录信息已过期，请重新登录',
              '登录过期',
              {
                confirmButtonText: '确定',
                callback: () => {
                  window.location.href = '/login'
                }
              }
            )
          }
          return null
        })
      })
      
      await Promise.all(savePromises)
      
      // 等待一小段时间，确保后端数据已更新
      await new Promise(resolve => setTimeout(resolve, 300))
    },

    /**
     * 格式化正确答案 - 修复数字转换问题
     */
    formatRightAnswer(rightOption, quType, options) {
      if (!rightOption || rightOption === 'null' || rightOption === 'undefined') {
        return ''
      }
      
      // 如果已经是字母格式，直接返回
      if (/^[A-F](,[A-F])*$/.test(rightOption)) {
        return rightOption
      }
      
      const optionMap = new Map(options.map(opt => [opt.id, opt]))
      
      try {
        if (quType === 2) {
          // 多选题
          const ids = String(rightOption).split(',').map(Number).filter(id => !isNaN(id))
          if (ids.length === 0) return rightOption
          
          return ids
            .map(id => optionMap.get(id))
            .filter(opt => opt)
            .sort((a, b) => a.sort - b.sort)
            .map(opt => this.numberToLetter(opt.sort))
            .join(',')
        } else {
          // 单选题、判断题
          const id = parseInt(String(rightOption))
          if (isNaN(id)) return rightOption
          
          const option = optionMap.get(id)
          return option ? this.numberToLetter(option.sort) : rightOption
        }
      } catch (e) {
        console.error('格式化正确答案失败:', e)
        return rightOption
      }
    },

    /**
     * 填充题目答案
     * @param {Object} questionInfo - 题目信息对象
     * @param {Object} answerData - 答案数据
     * @param {number} quType - 题目类型
     * @param {Map} optionMap - 选项映射
     */
    fillQuestionAnswer(questionInfo, answerData, quType, optionMap) {
      
      if (quType === 2) {
        // 多选题
        const selectedOptions = (answerData.selectedRaw || [])
          .map(id => optionMap.get(id))
          .filter(opt => opt)
          .sort((a, b) => a.sort - b.sort)
        
        if (selectedOptions.length > 0) {
          questionInfo.myOption = selectedOptions.map(opt => opt.id).join(',')
          questionInfo.myOptionDisplay = selectedOptions.map(opt => 
            `${this.numberToLetter(opt.sort)}.${opt.content}`
          ).join('；')
        }
      } else if (quType === 4) {
        // 简答题
        if (answerData.selectedRaw && answerData.selectedRaw.trim() !== '') {
          questionInfo.myOption = answerData.selectedRaw
          questionInfo.myOptionDisplay = answerData.selectedRaw
        }
      } else {
        // 单选、判断
        const selectedOption = optionMap.get(answerData.selectedRaw)
        if (selectedOption) {
          questionInfo.myOption = String(selectedOption.id)
          questionInfo.myOptionDisplay = `${this.numberToLetter(selectedOption.sort)}.${selectedOption.content}`
        }
      }
    },

    numberToLetter,

    /**
     * 交卷前预览 - 完全修复版本
     */
    async handHandExamPre() {
      console.log('=== 生成提交预览数据 ===')
      console.log('当前 answerStore:', this.answerStore)
      console.log('当前 answers:', this.answers)
      
      // 1. 强制保存所有未同步的答案
      try {
        await this.syncAllUnsavedAnswers()
      } catch (error) {
        console.error('同步答案失败:', error)
        // 检查是否是登录过期错误
        if (error.message.includes('登陆信息已过期')) {
          return
        }
      }
      
      // 2. 从后端获取完整的题目信息（确保数据完整）
      try {
        const res = await examCollect(this.examId)
        console.log('examCollect响应:', res)
        
        // 兼容处理：后端返回code=1表示成功
        if ((res.code === 1 || res.code === 0) && (res.data || res)) {
          // 兼容后端返回数据结构
          const backendData = res.data || res
          
          // 3. 完全基于本地最新答案构建预览数据
          this.recordData = backendData.map(item => {
            const answerData = this.answerStore[item.id]
            const options = item.option || []
            const optionMap = new Map(options.map(opt => [opt.id, opt]))
            
            // 构建题目信息
            const questionInfo = {
              id: item.id,
              questionId: item.id,
              title: item.title,
              content: item.content,
              quType: item.quType,
              option: options,
              analyse: item.analyse || '',
              rightOption: this.formatRightAnswer(
                item.rightOption, 
                item.quType, 
                options
              ),
              myOption: '',
              myOptionDisplay: '未作答',
              sort: item.sort, // 保存排序字段，用于预览页面显示正确的题目序号
              score: item.score || 0, // 题目分值
              image: item.image || '', // 题目图片
              isAnswered: answerData ? answerData.isAnswered : false // 是否已作答
            }
            
            // 4. 完全基于本地最新答案填充用户答案
            if (answerData) {
              this.fillQuestionAnswer(questionInfo, answerData, item.quType, optionMap)
            }
            
            console.log(`题目 ${item.id} 预览数据:`, questionInfo.myOptionDisplay)
            return questionInfo
          })
          
          // 完全重新构建recordData，确保与考试页面顺序完全一致
          // 按照allItem数组的顺序构建recordData，因为allItem是按照考试页面的显示顺序构建的
          const newRecordData = []
          const backendDataMap = new Map()
          
          // 首先将后端数据转换为Map，方便通过ID查找
          backendData.forEach(item => {
            backendDataMap.set(item.id, item)
            if (item.questionId) {
              backendDataMap.set(item.questionId, item)
            }
          })
          
          // 然后按照allItem的顺序构建recordData
          this.allItem.forEach((question, index) => {
            // 尝试通过questionId或id查找后端数据
            const backendItem = backendDataMap.get(question.questionId) || backendDataMap.get(question.id)
            
            if (backendItem) {
              const answerData = this.answerStore[question.questionId] || this.answerStore[question.id]
              const options = backendItem.option || []
              const optionMap = new Map(options.map(opt => [opt.id, opt]))
              
              // 构建题目信息
              const questionInfo = {
                id: backendItem.id,
                questionId: question.questionId,
                title: backendItem.title,
                content: backendItem.content,
                quType: backendItem.quType,
                option: options,
                analyse: backendItem.analyse || '',
                rightOption: this.formatRightAnswer(
                  backendItem.rightOption, 
                  backendItem.quType, 
                  options
                ),
                myOption: '',
                myOptionDisplay: '未作答',
                sort: backendItem.sort,
                score: backendItem.score || 0,
                image: backendItem.image || '',
                isAnswered: answerData ? answerData.isAnswered : false,
                displayOrder: index + 1
              }
              
              // 填充用户答案
              if (answerData) {
                this.fillQuestionAnswer(questionInfo, answerData, backendItem.quType, optionMap)
              }
              
              newRecordData.push(questionInfo)
            }
          })
          
          // 替换原来的recordData
          this.recordData = newRecordData
          
          // 验证排序结果，确保与考试页面顺序一致
          console.log('重新构建后的题目顺序:', this.recordData.map(item => ({
            id: item.id,
            questionId: item.questionId,
            title: item.title,
            displayOrder: item.displayOrder
          })))
          console.log('考试页面题目顺序:', this.allItem.map(item => ({
            questionId: item.questionId,
            title: item.content
          })))
          
          // 性能优化：限制日志输出，避免大量题目时的性能问题
          if (this.recordData.length <= 20) {
            console.log('预览数据生成成功:', this.recordData)
          } else {
            console.log(`预览数据生成成功: 共 ${this.recordData.length} 道题目`)
          }
          
          this.examPreVisible = true
        } else {
          console.error('获取题目汇总信息失败:', res.msg)
          ElMessage.error('获取预览数据失败: ' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('获取题目汇总信息异常:', error)
        // 检查是否是登录过期错误
        if (error.message.includes('登陆信息已过期')) {
          ElMessageBox.alert(
            '您的登录信息已过期，请重新登录',
            '登录过期',
            {
              confirmButtonText: '确定',
              callback: () => {
                window.location.href = '/login'
              }
            }
          )
        } else {
          ElMessage.error('网络错误，获取预览数据失败')
        }
      }
    },
    
    /**
     * 格式化后端返回的答案用于显示
     */
    formatBackendAnswerForDisplay(myOption, quType, options) {
      if (!myOption) return '未作答'
      
      const optionMap = new Map(options.map(opt => [opt.id, opt]))
      
      if (quType === 2) {
        // 多选题
        const ids = myOption.split(',').map(Number)
        const formattedAnswer = ids
          .map(id => optionMap.get(id))
          .filter(opt => opt)
          .sort((a, b) => a.sort - b.sort)
          .map(opt => `${this.numberToLetter(opt.sort)}.${opt.content}`)
          .join('；')
        return formattedAnswer || '未作答'
      } else if (quType === 4) {
        // 简答题
        return myOption || '未作答'
      } else {
        // 单选、判断
        const option = optionMap.get(parseInt(myOption))
        return option ? `${this.numberToLetter(option.sort)}.${option.content}` : '未作答'
      }
    },

    // 开始考试
    async startExam(examId) {
      console.log('=== 开始考试 ===')
      console.log('考试ID:', examId)
      
      try {
        const res = await examQuList(examId)
        console.log('=== 开始考试响应 ===')
        console.log('完整响应:', res)
        console.log('响应data:', res.data)
        console.log('响应code:', res.code)
        
        if (res.code) {
          this.paperData = res.data
          console.log('设置paperData后:', this.paperData)
          console.log('paperData.leftSeconds:', this.paperData.leftSeconds)
          console.log('paperData.radioList:', this.paperData.radioList)
          console.log('paperData.multiList:', this.paperData.multiList)
          console.log('paperData.judgeList:', this.paperData.judgeList)
          console.log('paperData.saqList:', this.paperData.saqList)
          
          // 保存预设的总考试时长
          this.totalExamSeconds = this.paperData.leftSeconds
          console.log('保存总考试时长:', this.totalExamSeconds)
          
          // 设置考试开始时间和时长（用于计时器）
          if (this.paperData.startTime) {
            this.examStartTime = this.paperData.startTime
            console.log('设置考试开始时间:', this.examStartTime)
          }
          if (this.paperData.examDuration) {
            this.examDuration = this.paperData.examDuration * 60 // 转换为秒
            console.log('设置考试时长:', this.examDuration, '秒')
          }
          
          // 同步服务器时间
          await this.syncServerTime()
        } else {
          console.error('获取题目列表失败:', res.msg)
          ElMessage.error('获取题目列表失败: ' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('开始考试请求失败:', error)
        ElMessage.error('网络错误，获取题目列表失败')
      }
    },
    
    /**
     * 同步服务器时间
     * 计算本地时间与服务器时间的偏移量
     */
    async syncServerTime() {
      try {
        const localTimeBefore = Date.now()
        const res = await getServerTime()
        const localTimeAfter = Date.now()
        
        if (res.code) {
          const serverTime = res.data
          const localTime = (localTimeBefore + localTimeAfter) / 2
          
          // 计算偏移量：服务器时间 - 本地时间
          this.serverTimeOffset = serverTime - localTime
          this.isServerTimeSynced = true
          
          console.log('[时间同步] 服务器时间:', serverTime)
          console.log('[时间同步] 本地时间:', localTime)
          console.log('[时间同步] 偏移量:', this.serverTimeOffset, 'ms')
          console.log('[时间同步] 网络延迟:', localTimeAfter - localTimeBefore, 'ms')
        } else {
          console.warn('[时间同步] 获取服务器时间失败:', res.msg)
        }
      } catch (error) {
        console.error('[时间同步] 同步服务器时间异常:', error)
      }
    },
    /**
     * 统计有多少题没答的
     * @returns {number}
     */
    countNotAnswered() {
      let notAnswered = 0
      const checkList = (list) => {
        if (list) {
          list.forEach(item => {
            if (!item.checkout) {
              notAnswered += 1
            }
          })
        }
      }

      checkList(this.paperData.radioList)
      checkList(this.paperData.multiList)
      checkList(this.paperData.judgeList)
      checkList(this.paperData.saqList)

      return notAnswered
    },


    // 清空Session
    // 使用函数清除以 "exam_" 开头的所有键值对
    clearSessionStorageByPrefix(prefix) {
      Object.keys(sessionStorage)
        .filter(key => key.startsWith(prefix))
        .forEach(key => sessionStorage.removeItem(key))
    },

    // 交卷
    doHandler(isAutomatic = false) {
      const performSubmit = () => {
        this.handleText = isAutomatic ? '时间到，正在自动交卷...' : '正在交卷，请等待...'
        this.loading = true
        // 删除当前标签页
        import('@/stores/tagsView').then(({ useTagsViewStore }) => {
          const tagsViewStore = useTagsViewStore()
          tagsViewStore.removeTag({
            title: this.$route.meta.title || '考试页面',
            path: this.$route.path,
            name: this.$route.name
          })
        })
        handExam(this.examId).then(() => {
          this.loading = false
          this.handleText = '交卷'
          ElMessage({
            message: isAutomatic ? '考试时间到，试卷已自动提交！' : '试卷提交成功！',
            type: 'success'
          })
          this.clearSessionStorageByPrefix('exam_')
          this.clearLocalStorageAnswers()
          this.$router.push({ name: 'text-center', params: { id: this.paperId }})
        }).catch((error) => {
          this.loading = false
          this.handleText = '交卷'
          // 检查是否是登录过期错误
          if (error.message.includes('登陆信息已过期')) {
            ElMessageBox.alert(
              '您的登录信息已过期，请重新登录',
              '登录过期',
              {
                confirmButtonText: '确定',
                callback: () => {
                  window.location.href = '/login'
                }
              }
            )
          } else {
            ElMessage({
              type: 'error',
              message: (isAutomatic ? '自动' : '') + '交卷失败，请联系管理员！'
            })
            console.error((isAutomatic ? '自动' : '') + '交卷失败:', error);
          }
        })
      }

      if (isAutomatic) {
        // 如果是自动触发（时间到），直接执行提交
        performSubmit()
      } else {
        // 如果是手动触发（点击按钮或确认预览），显示确认框
        const notAnswered = this.countNotAnswered()
        const msg = notAnswered > 0
          ? `您还有 ${notAnswered} 题未作答，确认要交卷吗?`
          : '确认要交卷吗？'

        ElMessageBox.confirm(msg, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            // 用户在确认框中点击“确定”后执行提交
            performSubmit()
          })
          .catch(() => {
            // 用户点击“取消”
            ElMessage({
              type: 'info',
              message: '交卷已取消，您可以继续作答！'
            })
          })
      }
    },

    /**
     * 答题卡点击跳转题目
     */
    handleCardClick(item) {
      if (!item || !item.questionId) return
      
      // 滚动到题目
      this.scrollToQuestion(item.questionId)
      
      // 高亮当前题目（答题卡中）
      this.$forceUpdate()
    },

    // 滚动到指定题目
    scrollToQuestion(questionId) {
      const element = document.getElementById(`question-${questionId}`)
      if (element) {
        // 计算滚动位置，考虑顶部导航栏的高度
        const headerHeight = 100 // 顶部导航栏的大致高度
        const offsetTop = element.offsetTop - headerHeight
        
        // 获取当前滚动位置
        const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop
        const scrollDistance = Math.abs(offsetTop - currentScrollTop)
        
        // 如果滚动距离很小，直接定位，否则使用平滑滚动
        if (scrollDistance < 50) {
          window.scrollTo({
            top: offsetTop,
            behavior: 'auto'
          })
        } else {
          window.scrollTo({
            top: offsetTop,
            behavior: 'smooth'
          })
        }
        
        // 添加高亮效果
        this.highlightQuestion(element)
        
        console.log(`滚动到题目 ${questionId}，位置: ${offsetTop}px`)
      } else {
        console.warn(`未找到题目元素: question-${questionId}`)
      }
    },
    
    // 高亮题目
    highlightQuestion(element) {
      // 移除之前的高亮
      const previousHighlight = document.querySelector('.question-highlight')
      if (previousHighlight) {
        previousHighlight.classList.remove('question-highlight')
      }
      
      // 添加新的高亮
      element.classList.add('question-highlight')
      
      // 2秒后移除高亮
      setTimeout(() => {
        element.classList.remove('question-highlight')
      }, 2000)
    },

    // 更新题目状态
    updateQuestionStatus(questionId, status) {
      // 在所有题型列表中查找并更新状态
      const updateListStatus = (list) => {
        if (list && list.length > 0) {
          const question = list.find(q => q.questionId === questionId)
          if (question) {
            question.checkout = status
          }
        }
      }

      updateListStatus(this.paperData.radioList)
      updateListStatus(this.paperData.multiList)
      updateListStatus(this.paperData.judgeList)
      updateListStatus(this.paperData.saqList)
    },

    // 试卷详情
    async fetchData(examId) {
      console.log('=== 获取试卷详情 ===')
      console.log('考试ID:', examId)
      
      try {
        const response = await examQuList(examId)
        console.log('=== 获取试卷详情响应 ===')
        console.log('完整响应:', response)
        console.log('响应data:', response.data)
        console.log('响应code:', response.code)
        
        if (response.code) {
          // 试卷内容
          this.paperData = response.data
          console.log('设置paperData后:', this.paperData)
          console.log('paperData.leftSeconds:', this.paperData.leftSeconds)
          console.log('paperData.radioList:', this.paperData.radioList)
          console.log('paperData.multiList:', this.paperData.multiList)
          console.log('paperData.judgeList:', this.paperData.judgeList)
          console.log('paperData.saqList:', this.paperData.saqList)
          
          // 保存预设的总考试时长
          this.totalExamSeconds = this.paperData.leftSeconds
          console.log('保存总考试时长:', this.totalExamSeconds)
          
          // 设置考试开始时间和时长（用于计时器）
          if (this.paperData.startTime) {
            this.examStartTime = this.paperData.startTime
            console.log('设置考试开始时间:', this.examStartTime)
          }
          if (this.paperData.examDuration) {
            this.examDuration = this.paperData.examDuration * 60 // 转换为秒
            console.log('设置考试时长:', this.examDuration, '秒')
          }
          
          this.allItem = []

          // 合并所有题目到allItem数组
          this.mergeAllQuestions()
          console.log('合并后的allItem:', this.allItem)
          
          // 初始化答案对象
          this.initAnswers()
          
          // 从本地存储恢复答案数据
          this.loadAnswersFromLocalStorage()
          
          // 同步服务器时间（如果尚未同步）
          if (!this.isServerTimeSynced) {
            await this.syncServerTime()
          }
        } else {
          console.error('获取试卷详情失败:', response.msg)
          ElMessage.error('获取试卷详情失败: ' + (response.msg || '未知错误'))
        }
      } catch (error) {
        console.error('获取试卷详情请求失败:', error)
        ElMessage.error('网络错误，获取试卷详情失败')
      }
    },
    
    // 初始化答案对象
    initAnswers() {
      console.log('=== 初始化答案对象 ===')
      this.answers = {}
      
      // 初始化单选题答案
      if (this.paperData.radioList) {
        this.paperData.radioList.forEach(question => {
          this.answers[question.questionId] = ''
        })
      }
      
      // 初始化多选题答案
      if (this.paperData.multiList) {
        this.paperData.multiList.forEach(question => {
          this.answers[question.questionId] = []
        })
      }
      
      // 初始化判断题答案
      if (this.paperData.judgeList) {
        this.paperData.judgeList.forEach(question => {
          this.answers[question.questionId] = ''
        })
      }
      
      // 初始化简答题答案
      if (this.paperData.saqList) {
        this.paperData.saqList.forEach(question => {
          this.answers[question.questionId] = ''
        })
      }
      
      console.log('初始化后的answers:', this.answers)
    },
    
    // 合并所有题目
    mergeAllQuestions() {
      const addQuestionsToAllItems = (questionList) => {
        if (questionList && questionList.length > 0) {
          questionList.forEach(item => this.allItem.push(item))
        }
      }

      addQuestionsToAllItems(this.paperData.radioList)
      addQuestionsToAllItems(this.paperData.multiList)
      addQuestionsToAllItems(this.paperData.judgeList)
      addQuestionsToAllItems(this.paperData.saqList)
    },

    // 处理滚动事件
    handleScroll() {
      // 实现滚动逻辑
    },

    // 获取左侧距离
    getLfetDistance() {
      const body = document.querySelector('body')
      this.flexLeft = (body.offsetWidth - 1200) / 2
    },

    // 获取选项信息
    getOptionInfo(question, answer, hasAnswer) {
      let selectedKey = ''
      let selectedValue = ''
      
      if (hasAnswer) {
        if (typeof answer === 'string') {
          // 简答题
          selectedKey = answer.trim()
          selectedValue = answer.trim()
        } else if (Array.isArray(answer)) {
          // 多选题
          const options = question.options || []
          const optionMap = new Map(options.map(opt => [opt.id, opt]))
          const selectedOptions = answer.map(optId => optionMap.get(optId)).filter(opt => opt !== undefined)
          selectedKey = selectedOptions.map(opt => this.numberToLetter(opt.sort)).join(',')
          selectedValue = selectedOptions.map(opt => opt.content).join(',')
        } else {
          // 单选题或判断题
          const options = question.options || []
          const optionMap = new Map(options.map(opt => [opt.id, opt]))
          const selectedOption = optionMap.get(answer)
          if (selectedOption) {
            selectedKey = this.numberToLetter(selectedOption.sort)
            selectedValue = selectedOption.content
          }
        }
      }
      
      return { selectedKey, selectedValue }
    },

    // 更新答案状态
    updateAnswerState(questionId, answer, hasAnswer, selectedKey, selectedValue) {
      // 写入统一存储：标记已作答+记录选项信息
      // Vue 3 的 Proxy 会自动检测对象属性的变化，不需要使用 $set
      this.answerStore[questionId] = {
        selectedKey: selectedKey,   // 选项序号（如A/B/C）
        selectedValue: selectedValue, // 选项内容（如"中国"）
        isAnswered: hasAnswer,     // 已作答标识
        selectedAnswer: answer,      // 原始答案（用于后端提交）
        lastUpdateTime: Date.now(),
        syncedWithServer: false
      }
      
      // 同步更新 answers 对象（用于 v-model 绑定）
      this.answers[questionId] = answer
      
      // 更新 answerStatus
      if (!this.answerStatus[questionId]) {
        this.answerStatus[questionId] = {
          isAnswered: hasAnswer,
          selectedAnswer: answer,
          lastUpdateTime: Date.now(),
          syncedWithServer: false
        }
      } else {
        this.answerStatus[questionId].isAnswered = hasAnswer
        this.answerStatus[questionId].selectedAnswer = answer
        this.answerStatus[questionId].lastUpdateTime = Date.now()
      }
      
      console.log(`题目 ${questionId} 状态更新为 ${hasAnswer ? '已作答' : '未作答'}`)
      console.log(`answerStatus[${questionId}]:`, this.answerStatus[questionId])
      console.log(`answers[${questionId}]:`, this.answers[questionId])
      console.log(`answerStore[${questionId}]:`, this.answerStore[questionId])
      console.log(`完整的 answerStatus:`, this.answerStatus)
      console.log(`完整的 answers:`, this.answers)
      console.log(`完整的 answerStore:`, this.answerStore)
      
      // 更新题目状态（用于答题卡显示）
      this.updateQuestionStatus(questionId, hasAnswer ? 1 : 0)
      
      // 使用 nextTick 确保 DOM 更新完成后再强制更新子组件
      this.$nextTick(() => {
        // 强制更新答题卡组件
        this.$forceUpdate()
      })
    },


    /**
     * 处理计时器tick事件
     * @param {number} leftSeconds - 剩余秒数
     */
    handleTimerTick(leftSeconds) {
      // 可以在这里添加定时保存等逻辑
      // 例如每30秒保存一次答案
      if (leftSeconds % 30 === 0) {
        this.saveAnswersToLocalStorage()
      }
    },
    
    // 处理用户选择选项的事件
    handleOptionChange(questionId, answer) {
      console.log(`用户选择了题目 ${questionId}，答案：`, answer)
      
      // 检查答案是否有效
      const hasAnswer = answer && (typeof answer === 'string' ? answer.trim() !== '' : answer.length > 0)
      
      // 获取题目信息
      const question = this.allItem.find(item => item.questionId === questionId)
      if (!question) {
        console.error(`题目 ${questionId} 不存在`)
        return
      }
      
      // 获取选项信息
      const { selectedKey, selectedValue } = this.getOptionInfo(question, answer, hasAnswer)
      
      // 更新答案状态
      this.updateAnswerState(questionId, answer, hasAnswer, selectedKey, selectedValue)
      
      // 保存答案到后端
      this.saveAnswerToServer(questionId, answer, hasAnswer)
    },
    
    // 记录用户已进入答题页面
    recordUserEnteredExam(examId) {
      // 从localStorage获取已进入的考试列表
      let enteredExams = localStorage.getItem('enteredExams')
      if (enteredExams) {
        enteredExams = JSON.parse(enteredExams)
      } else {
        enteredExams = []
      }
      
      // 如果该考试未在列表中，则添加
      if (!enteredExams.includes(examId)) {
        enteredExams.push(examId)
        localStorage.setItem('enteredExams', JSON.stringify(enteredExams))
      }
    }
  }
}
</script>

<style scoped>
page {
  background: #ebecee;
}

.qu-content div {
  line-height: 30px;
  width: 100%;
}

.el-checkbox-group label,
.el-radio-group label {
  width: 100%;
}

.content-h {
  position: sticky;
  top: 20px;
  height: calc(100vh - 110px);
  overflow-y: auto;
  z-index: 10;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

/* 优化卡片样式 */
el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

el-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
}

.card-title {
  background: #eee;
  line-height: 35px;
  text-align: center;
  font-size: 14px;
}
.card-line {
  padding-left: 10px;
  display: flex;
  flex-wrap: wrap;
}
.card-line span {
  cursor: pointer;
  margin: 2px;
}

/* 移除自定义单选按钮和复选框样式，使用Element Plus默认样式 */

/* 题目高亮效果 */
.question-highlight {
  animation: highlightPulse 0.6s ease-in-out;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
  border-color: #409eff !important;
}

@keyframes highlightPulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 0 0 8px rgba(64, 158, 255, 0.3);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
  }
}

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 答题卡优化 */
.content-h::-webkit-scrollbar {
  width: 6px;
}

.content-h::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.content-h::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.content-h::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .content-h {
    position: relative;
    top: 0;
    height: auto;
    max-height: 300px;
    margin-bottom: 20px;
  }
  
  .qu-content {
    padding: 10px;
  }
  
  .question-highlight {
    animation: none;
  }
}

@media screen and (max-width: 480px) {
  .content-h {
    max-height: 250px;
  }
}

.el-radio img,
.el-checkbox img {
  max-width: 200px;
  max-height: 200px;
  border: #dcdfe6 1px dotted;
}

:deep(.el-input__textarea) {
  transition: all 0.3s ease;
}

:deep(.el-input__textarea:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}



:deep(.el-checkbox__label) {
  line-height: 30px;
}

:deep(.el-radio__label) {
  line-height: 30px;
}

/* 考试记录 */
.content {
  width: 97%;
  height: 60px;
  border: 1px solid #0a84ff;
  margin-top: 8px;
  margin-left: 10px;
  padding: 10px;
  font-weight: 200;
}
.sj {
  margin-top: 10px;
  margin-left: 10px;
  line-height: 22px;
}
.fk {
  width: 200px;
  height: 100%;
  box-shadow: 0 0 15px rgb(197, 197, 197);
  margin: auto;
  margin-top: 20px;
  margin-left: 15px;
}
.el-header {
  background-color: #b3c0d1;
  color: #333;
  line-height: 60px;
}

.left {
  width: 250px;
  height: 100%;
}
.right {
  width: 70%;
  height: 100%;
}
.el-divider--horizontal {
  display: block;
  height: 1px;
  width: 95%;
  margin: 24px 0;
}
.type_tag {
  margin-right: 5px;
  margin-top: 10px;
}

/* // 试题内容样式 */
.qu_list {
  height: 100%;
  width: 100%;
  overflow: auto;
  page-break-after: always;

  .qu_num {
    display: inline-block;
    /* // background: url('~@/assets/images/tkxl/btbj.png') no-repeat 100% 100%; */
    background-size: contain;
    height: 30px;
    width: 30px;
    line-height: 25px;
    color: #fff;
    font-size: 14px;
    text-align: center;
    margin-right: 15px;
    flex-shrink: 0;
  }

  .qu_content {
    padding-left: 10px;
  }

  /* // 选项组 */
  .qu_choose_group {
    width: 100%;

    /* 单个选项 */
    .qu_choose {
      display: block;
      margin: 10px;
      .el-radio__label {
        line-height: 20px;
      }
      /* // 去除前面的radio */
      :deep(.el-radio__input .el-radio__inner) {
        display: none;
      }

      /* // 单个选项内容样式 */
      .qu_choose_tag {
        display: inline-flex;
        width: 90%;
        /* // 选项标签 */
        .qu_choose_tag_type {
          font-weight: bold;
          /* // color: #0a84ff; */
          width: 10px;
        }
        /* // 选项内容 */
        .qu_choose_tag_content {
          padding: 0 10px 10px 10px;
        }

        .qu_choose_tag_el_image {
          clear: both;
          padding-top: 10px;
        }
      }
      /* // 选项答案 */
      .qu_choose_answer {
        float: right;
      }
    }
  }

  /* // 试题解析 */
  .qu_analysis {
    padding: 10px;

    .qu_analysis_content {
      padding-top: 10px;
    }
  }

  /* // 试题赋分 */
  .qu_assign_score {
    background: #f5f5f5;
    height: 100px;
    padding-top: 35px;

    .qu_assign_score_content {
      width: 80px;
    }
  }
}
.current {
  background: #f5f5f5;
}
.imgC{
  height:150px
}
.qu_choose_tag_img {
          height: auto;
          display: block;
          margin: 10px;
        }
</style>
