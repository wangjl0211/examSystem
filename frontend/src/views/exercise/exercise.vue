<template>
  <div style="width: 100%; height: 100%; background-color: #f0f2f5; padding: 20px 0 0">
    <!-- 开头 -->
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card style="margin-bottom: 10px">
          题库：{{ repoTitle }}
          <el-button
            :loading="loading"
            style="float: right; margin-top: -10px"
            type="primary"
            @click="exitFun()"
          >
            结束刷题
          </el-button>
        </el-card>
      </el-col>

      <!-- 答题卡 -->
      <el-col :span="5" :xs="24" style="margin-bottom: 10px">
        <el-card class="content-h">
          <div class="btn_switch">
            <button
              class="btn_anniu"
              :class="{ newStyle: 0 === number }"
              @click="change(0)"
            >
              按顺序
            </button>
            <button
              class="btn_anniu"
              :class="{ newStyle: 1 === number }"
              @click="change(1)"
            >
              按题型
            </button>
            <div style="height: 20px" />
            <el-row :gutter="24" class="card-line" style="padding-left: 10px">
              <el-tag type="success">回答正确</el-tag>
              <el-tag type="danger">回答错误</el-tag>
              <el-tag type="info">未作答</el-tag>
              <el-tag type="warning">当前试题</el-tag>
              <div style="margin-bottom: 15px" />
            </el-row>
          </div>
          <!-- <div> -->
          <div v-if="0 === number">
            <!-- <p>我是题型</p> -->
            <p class="card-title">答题卡</p>
            <el-row :gutter="24" class="card-line" style="padding-left: 10px">
              <el-tag
                v-for="(item, index) in quList"
                :key="index"
                style="width: calc(100% / 8); text-align: center"
                :type="cardItemClass(item.exercised, item.quId, item.isRight)"
                @click="selectQuNum(item, index)"
              >{{ index + 1 }}</el-tag>
            </el-row>
          </div>

          <div v-if="1 === number">
            <div v-if="paperData.radioList.length > 0">
              <p class="card-title">单选题</p>
              <el-row :gutter="24" class="card-line">
                <el-tag
                  v-for="(item, index) in paperData.radioList"
                  :key="index"
                  :type="cardItemClass(item.exercised, item.quId, item.isRight)"
                  @click="selectQuId(item, index)"
                >
                  {{ index + 1 }}</el-tag>
              </el-row>
            </div>

            <div
              v-if="paperData.multiList  != undefined && paperData.multiList.length > 0"
            >
              <p class="card-title">多选题</p>
              <el-row :gutter="24" class="card-line">
                <el-tag
                  v-for="(item, index) in paperData.multiList"
                  :key="index"
                  :type="cardItemClass(item.exercised, item.quId, item.isRight)"
                  @click="selectQuId(item, index)"
                >{{ index + 1 }}</el-tag>
              </el-row>
            </div>

            <div
              v-if="paperData.judgeList  != undefined && paperData.judgeList.length > 0"
            >
              <p class="card-title">判断题</p>
              <el-row :gutter="24" class="card-line">
                <el-tag
                  v-for="(item, index) in paperData.judgeList"
                  :key="index"
                  :type="cardItemClass(item.exercised, item.quId, item.isRight)"
                  @click="selectQuId(item, index)"
                >{{ index + 1 }}</el-tag>
              </el-row>
            </div>
            <div v-if="paperData.saqList  != undefined && paperData.saqList.length > 0">
              <p class="card-title">简答题</p>
              <el-row :gutter="24" class="card-line">
                <el-tag
                  v-for="(item, index) in paperData.saqList"
                  :key="index"
                  :type="cardItemClass(item.exercised, item.quId, item.isRight)"
                  @click="selectQuId(item, index)"
                >{{ index + 1 }}</el-tag>
              </el-row>
            </div>
          </div>

          <!-- </div> -->
        </el-card>
      </el-col>

      <el-col :span="19" :xs="24">
        <el-card class="qu-content content-h">
          <!-- 题目内容区域 -->
          <div v-loading="questionLoading" element-loading-text="加载题目中..." style="min-height: 300px;">
            <p v-if="quDetail.content">
              <span :class="['question-type', {
                'single-choice': quDetail.quType === 1,
                'multiple-choice': quDetail.quType === 2,
                'judgment': quDetail.quType === 3,
                'short-answer': quDetail.quType === 4
              }]">{{ shouQuType(quDetail.quType) }}</span>
              {{ number == 1 ? curTypeIndex + 1 : currentQuIndex + 1 }}.{{ quDetail.content }}
            </p>
            <p v-if="quDetail.image != null && quDetail.image != ''">
              <el-image 
              :src="quDetail.image" 
              style="max-width: 100px;max-height:100%" 
              :preview-src="[quDetail.image]" />
            </p>
            <div v-if="quDetail.quType == 1 || quDetail.quType == 3">
              <el-radio-group v-model="radioValue" :disabled="isAnswered">
                <el-radio
                  v-for="item in quDetail.options"
                  :key="item.id"
                  :label="item.id"
                  @click="handleRadioClick(item.id)"
                >
                  <!-- 给选项文本添加 getOptionClass 动态 class -->
                  <span :class="getOptionClass(item)">
                    {{ numberToLetter(item.sort + 1) }}.{{ item.content }}
                  </span>
                  <div v-if="item.image && item.image  != ''" style="clear: both">
                    <el-image :src="item.image" style="max-width: 100px" />
                  </div>
                </el-radio>
              </el-radio-group>

            </div>

            <div v-if="quDetail.quType == 2">
              <el-checkbox-group v-model="multiValue" :disabled="isAnswered">
                <el-checkbox
                  v-for="item in quDetail.options"
                  :key="item.id"
                  :label="item.id"
                >
                  <span :class="getOptionClass(item)">
                    {{ numberToLetter(item.sort + 1) }}.{{ item.content }}
                  </span>
                  <div v-if="item.image && item.image  != ''" style="clear: both">
                    <el-image :src="item.image" style="max-width: 100px" />
                  </div>
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <div v-if="quDetail.quType === 4">
              <el-input
                v-model="radioValue"
                :disabled="isAnswered"
                type="textarea"
                resize="none"
                :clearable="true"
                placeholder="请输入答案"
                @input="handleSaqInput"
              />
            </div>

            <div v-if="isAnswered" class="answer-feedback">
              <!-- 回答结果提示 -->
              <div class="answer-result">
                <span :class="{ 'result-correct': rightQuAnswer.msg === '回答正确', 'result-wrong': rightQuAnswer.msg === '回答错误' }">
                  {{ rightQuAnswer.msg }}
                </span>
              </div>

              <!-- 单选题、多选题、判断题：显示正确选项内容 -->
              <div v-if="quDetail.quType !== 4" class="correct-options-display">
                <div class="correct-options-title">
                  <el-icon><CircleCheck /></el-icon>
                  <span>正确答案：</span>
                </div>
                <div class="correct-options-list">
                  <div
                    v-for="option in getCorrectOptions()"
                    :key="option.id"
                    class="correct-option-item"
                  >
                    <span class="option-letter">{{ numberToLetter(option.sort + 1) }}</span>
                    <span class="option-text">{{ option.content }}</span>
                    <div v-if="option.image && option.image !== ''" class="option-image">
                      <el-image :src="option.image" style="max-width: 100px" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- 简答题：显示用户答案和正确答案 -->
              <div v-if="quDetail.quType === 4" class="saq-answer-section">
                <!-- 用户答案 -->
                <div class="my-answer-section">
                  <div class="section-title my-answer-title">
                    <el-icon><Edit /></el-icon>
                    <span>我的答案</span>
                  </div>
                  <div class="section-content my-answer-content">
                    {{ radioValue || '未作答' }}
                  </div>
                </div>

                <!-- 正确答案 -->
                <div class="correct-answer-section">
                  <div class="section-title correct-answer-title">
                    <el-icon><CircleCheck /></el-icon>
                    <span>正确答案</span>
                  </div>
                  <div class="section-content correct-answer-content">
                    {{ getCorrectAnswerText() }}
                  </div>
                </div>
              </div>

              <!-- 试题分析 -->
              <div v-if="rightQuAnswer.data && rightQuAnswer.data.analysis" class="answer-analysis">
                <div class="analysis-title">
                  <el-icon><Document /></el-icon>
                  <span>试题分析</span>
                </div>
                <div class="analysis-content">
                  {{ rightQuAnswer.data.analysis }}
                </div>
              </div>
            </div>

            <div style="margin-top: 20px">
              <!-- 上一题按钮 -->
              <el-button
                v-if="showPrevious"
                type="primary"
                icon="ArrowLeft"
                @click="handPrevious()"
                :loading="questionLoading"
              >
                {{ preText }}
              </el-button>

              <!-- 下一题按钮 -->
              <el-button
                v-if="showNext"
                type="primary"
                icon="ArrowRight"
                @click="handNext()"
                :loading="questionLoading"
              >
                {{ nextText }}
              </el-button>

              <!-- 提交答案按钮（仅多选题和简答题显示） -->
              <el-button
                v-if="(quDetail.quType === 2 || quDetail.quType === 4) && !isAnswered"
                type="success"
                @click="handSubmitAnswer()"
                :loading="questionLoading"
              >
                提交答案
              </el-button>

            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 答题统计弹框 -->
    <el-dialog title="答题统计" v-model="statisticsDialogVisible" width="400px">
      <div class="statistics-container">
        <!-- 上半部分：统计数字 -->
        <el-row :gutter="20" class="stat-row">
          <el-col :span="8" class="stat-item">
            <div class="stat-number">{{ totalQuestions }}</div>
            <div class="stat-label">总题数</div>
          </el-col>
          <el-col :span="8" class="stat-item">
            <div class="stat-number">{{ correctCount }}</div>
            <div class="stat-label">正确</div>
          </el-col>
          <el-col :span="8" class="stat-item">
            <div class="stat-number">{{ wrongCount }}</div>
            <div class="stat-label">错误</div>
          </el-col>
        </el-row>
        <el-row :gutter="20" class="stat-row" style="margin-top: 20px;">
          <el-col :span="12" class="stat-item">
            <div class="stat-number">{{ unansweredCount }}</div>
            <div class="stat-label">未答</div>
          </el-col>
          <el-col :span="12" class="stat-item">
            <div class="stat-number">{{ accuracyRate }}</div>
            <div class="stat-label">正确率</div>
          </el-col>
        </el-row>
        <!-- 下半部分：进度条直观显示正确率 -->
        <el-progress
          :percentage="parseInt(accuracyRate)"
          status="success"
          stroke-width="16"
          style="margin-top: 20px;"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="onDialogCancel">取消</el-button>
          <el-button type="primary" @click="finishExam">结束刷题</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import { getQuestion, getQuestionDetail, submitAnswer } from '@/api/exercise'

export default {
  name: 'ExercisePractice',

  data() {
    return {
      flag: false,
      showAnalysis: 0,
      repoId: '',
      repoTitle: '',
      quId: '',
      curQuId: '',
      quDetail: {},
      quList: [],
      preText: '上一题',
      nextText: '提交答案',
      rightQuAnswer: {},
      number: 0,
      receivedRow: null,
      curTypeIndex: 0,
      curListIndex: 1,
      isFullscreen: false,
      showPrevious: false,
      showNext: true,
      loading: false,
      handleText: '提交',
      pageLoading: false,
      currentQuIndex: 0,
      paperId: '',
      cardItem: {},
      allItem: [],
      quData: {
        answerList: []
      },
      paperData: {
        radioList: [],
        multiList: [],
        judgeList: [],
        saqList: []
      },
      radioValue: '',
      multiValue: [],
      answeredIds: [],
      debounceFlag: false,
      isAnswered: false,
      // 新增属性，控制统计弹框的显示
      statisticsDialogVisible: false,
      // 新增属性，控制题目区域的加载状态
      questionLoading: false,
      // 新增属性，存储用户当前输入的答案
      currentAnswer: {
        radio: '',
        multi: [],
        saq: ''
      },
      // 新增属性，存储已提交题目的答案和状态
      submittedAnswers: {},
      // 标志位：用于区分是用户主动选择还是系统恢复值
      isRestoringState: false,
      // 防重复提交锁
      isSubmitting: false
    }
  },
  created() {
    this.repoId = this.$route.query.repoId
    this.repoTitle = this.$route.query.repoTitle
    // 从本地存储加载已提交的答案状态
    this.loadSubmittedAnswers()
    this.test()
  },
  beforeUnmount() {
    // 保存已提交的答案状态到本地存储
    this.saveSubmittedAnswersToLocal()
  },
  computed: {
    // 根据不同模式统计总题数
    totalQuestions() {
      if (this.number === 0) {
        return this.quList.length
      } else {
        return this.paperData.radioList.length +
               this.paperData.multiList.length +
               this.paperData.judgeList.length +
               this.paperData.saqList.length
      }
    },
    // 统计回答正确的题数
    correctCount() {
      let list = []
      if (this.number === 0) {
        list = this.quList
      } else {
        list = this.paperData.radioList.concat(
          this.paperData.multiList,
          this.paperData.judgeList,
          this.paperData.saqList
        )
      }
      return list.filter(item => item.isRight).length
    },
    // 统计已作答但回答错误的题数
    wrongCount() {
      let list = []
      if (this.number === 0) {
        list = this.quList
      } else {
        list = this.paperData.radioList.concat(
          this.paperData.multiList,
          this.paperData.judgeList,
          this.paperData.saqList
        )
      }
      return list.filter(item => item.exercised && !item.isRight).length
    },
    // 未作答题数
    unansweredCount() {
      return this.totalQuestions - this.correctCount - this.wrongCount
    },
    // 正确率计算
    accuracyRate() {
      let list = []
      if (this.number === 0) {
        list = this.quList
      } else {
        list = this.paperData.radioList.concat(
          this.paperData.multiList,
          this.paperData.judgeList,
          this.paperData.saqList
        )
      }
      // 筛选出所有已作答的题目
      const answered = list.filter(item => item.exercised)
      if (answered.length === 0) return '0%'
      // 对于非简答题，根据 isRight 判断；简答题只要作答就认为正确
      const correct = answered.filter(item => {
        if (item.quType === 4) {
          return true
        } else {
          return item.isRight
        }
      }).length
      const rate = Math.round((correct / answered.length) * 100)
      return rate + '%'
    }

  },
  watch: {
    radioValue(newVal) {
      // 仅在用户主动选择时触发，系统恢复值时不触发
      if (newVal && (this.quDetail.quType === 1 || this.quDetail.quType === 3) && !this.isAnswered && !this.isRestoringState) {
        this.isAnswered = true
        this.$nextTick(async () => {
          await this.fillAnswer()
          this.showAnalysis = 1
          this.nextText = '下一题'
          // 双重保险：确保正确答案显示
          if (!this.rightQuAnswer.correctAnswerStr) {
            this.extractCorrectAnswerFromQuDetail()
          }
          // 存储已提交的答案状态
          this.saveSubmittedAnswer()
          // 更新答题卡状态
          this.updateCardItemStatus()
          this.showButton()
        })
      }
    }
  },
  methods: {
    // 根据答案返回class
    getOptionClass(option) {
      // 未提交答案或未作答不做样式处理
      if (!this.rightQuAnswer.data || !this.isAnswered) return ''

      // isRight 字段是 Integer 类型：1 表示正确，0 表示错误
      const isCorrectValue = (val) => val === 1 || val === true

      // 从返回的答案数据中查找对应选项的正确性
      const answerOption = this.rightQuAnswer.data.options
        ? this.rightQuAnswer.data.options.find(o => o.id === option.id)
        : null
      const isCorrect = answerOption ? isCorrectValue(answerOption.isRight) : isCorrectValue(option.isRight)

      // 判断用户是否选择了该选项
      let isChosen = false
      if (this.quDetail.quType === 1 || this.quDetail.quType === 3) {
        // 单选和判断题：用户选择存储在 radioValue 中
        isChosen = (this.radioValue === option.id)
      } else if (this.quDetail.quType === 2) {
        // 多选题：用户选择存储在 multiValue 数组中
        isChosen = this.multiValue.includes(option.id)
      }

      // 样式逻辑：
      // 1. 正确选项始终显示绿色背景（无论用户是否选择）
      // 2. 用户选择的错误选项显示红色背景
      // 3. 其他选项不显示特殊样式
      if (isCorrect) {
        return 'option-correct'
      }
      if (isChosen && !isCorrect) {
        return 'option-wrong'
      }
      return ''
    },

    // 获取正确选项列表
    getCorrectOptions() {
      // 确保 quDetail 和 options 存在
      if (!this.quDetail || !this.quDetail.options || this.quDetail.options.length === 0) {
        return []
      }

      // isRight 字段是 Integer 类型：1 表示正确，0 表示错误
      const isCorrect = (option) => option.isRight === 1 || option.isRight === true

      // 优先从 rightQuAnswer.data.options 获取正确答案标记
      if (this.rightQuAnswer && this.rightQuAnswer.data && this.rightQuAnswer.data.options && this.rightQuAnswer.data.options.length > 0) {
        const correctOptions = this.rightQuAnswer.data.options.filter(isCorrect)
        if (correctOptions.length > 0) {
          return correctOptions
        }
      }

      // 从 quDetail.options 获取（原始题目数据）
      return this.quDetail.options.filter(isCorrect)
    },

    // 获取简答题正确答案文本
    getCorrectAnswerText() {
      // 优先从 rightQuAnswer.correctAnswerStr 获取
      if (this.rightQuAnswer.correctAnswerStr) {
        return this.rightQuAnswer.correctAnswerStr
      }

      // 从 quDetail.options 获取
      if (this.quDetail.options && this.quDetail.options.length > 0) {
        return this.quDetail.options[0].content || '暂无正确答案'
      }

      return '暂无正确答案'
    },

    // 重置答题状态
    resetAnswerState() {
      // 保存当前答案
      if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
        this.currentAnswer.radio = this.radioValue
      } else if (this.quDetail.quType === 2) {
        this.currentAnswer.multi = this.multiValue
      }
      
      // 完全重置所有与答案相关的状态
      this.radioValue = ''
      this.multiValue = []
      this.rightQuAnswer = { data: null, msg: '' }
      this.showAnalysis = 0
      this.isAnswered = false
      this.nextText = '提交答案'
    },
    // 处理简答题输入
    handleSaqInput() {
      this.currentAnswer.saq = this.radioValue
    },
    // 处理题型切换逻辑
    handleQuestionTypeSwitch() {
      const currentList = {
        1: this.paperData.radioList,
        2: this.paperData.multiList,
        3: this.paperData.judgeList,
        4: this.paperData.saqList
      }[this.curListIndex]

      if (this.curTypeIndex < currentList.length - 1) {
        this.curTypeIndex++
        this.curQuId = currentList[this.curTypeIndex].quId
      } else {
        const nextTypeMap = {
          1: { index: 2, list: this.paperData.multiList },
          2: { index: 3, list: this.paperData.judgeList },
          3: { index: 4, list: this.paperData.saqList },
          4: { index: 1, list: this.paperData.radioList }
        }
        const nextType = nextTypeMap[this.curListIndex]
        this.curListIndex = nextType.index
        this.curTypeIndex = 0
        this.curQuId = nextType.list[0]?.quId || ''
      }
    },

    // 修改结束刷题逻辑：确认后显示答题统计弹框
    exitFun() {
      this.statisticsDialogVisible = true
    },

    // 点击弹框中“确定结束”按钮后的处理：关闭弹框并进行跳转或其他后续处理
    finishExam() {
      // 清除本地存储的答案状态
      this.clearSubmittedAnswers()
      
      // 删除当前标签页
      import('@/stores/tagsView').then(({ useTagsViewStore }) => {
        const tagsViewStore = useTagsViewStore()
        tagsViewStore.removeTag({
          title: this.$route.meta.title || '刷题中心', // 从路由元数据中获取标题，默认为刷题中心
          path: this.$route.path,
          name: this.$route.name // 添加路由名称
        })
      })
      this.statisticsDialogVisible = false
      this.$router.push({ name: 'exercise-center' })
    },
    // 取消弹框，不结束刷题
    onDialogCancel() {
      this.statisticsDialogVisible = false
    },
    async test() {
      if (!this.repoId) {
        // 如果没有repoId，直接跳转到刷题中心
        this.$router.push({ name: 'exercise-center' })
        return
      }
      try {
        const res = await getQuestion(this.repoId)
        if (res.code) {
          this.quList = res.data

          // 清空各题型数组
          this.paperData.radioList = []
          this.paperData.multiList = []
          this.paperData.judgeList = []
          this.paperData.saqList = []

          if (this.number === 1) {
            this.quList.forEach((item) => {
              if (item.quType === 1) {
                this.paperData.radioList.push(item)
              } else if (item.quType === 2) {
                this.paperData.multiList.push(item)
              } else if (item.quType === 3) {
                this.paperData.judgeList.push(item)
              } else if (item.quType === 4) {
                this.paperData.saqList.push(item)
              }
            })
            this.quList = []
            // 初始化试题Id
            this.initQuId()
          }
          this.getCurrentQuDetial()
        } else {
          this.$message.error(res.msg || '您无权访问该题库')
          this.$router.push({ name: 'exercise-center' })
        }
      } catch (error) {
        console.error('获取试题列表失败:', error)
        this.$message.error('获取试题列表失败')
        this.$router.push({ name: 'exercise-center' })
      }
    },
    // 获取试题Id列表
    async getQuestionList() {
      try {
        const res = await getQuestion(this.repoId)
        if (res.code) {
          this.quList = res.data

          // 按顺序
          // if (this.number == 0) {
          this.paperData.radioList = []
          this.paperData.multiList = []
          this.paperData.judgeList = []
          this.paperData.saqList = []
          // }
          // 按题型
          if (this.number === 1) {
            this.quList.forEach((item) => {
              if (item.quType === 1) {
                this.paperData.radioList.push(item)
              } else if (item.quType === 2) {
                this.paperData.multiList.push(item)
              } else if (item.quType === 3) {
                this.paperData.judgeList.push(item)
              } else if (item.quType === 4) {
                this.paperData.saqList.push(item)
              }
            })
            this.quList = []
            // 初始化试题Id
            this.initQuId()
          }
        } else {
          this.$message.error(res.msg || '您无权访问该题库')
          this.$router.push({ name: 'exercise-center' })
        }
      } catch (error) {
        console.error('获取试题列表失败:', error)
        this.$message.error('获取试题列表失败')
        this.$router.push({ name: 'exercise-center' })
      }
    },
    numberToLetter(sort) {
      switch (sort) {
        case 1:
          return 'A'
        case 2:
          return 'B'
        case 3:
          return 'C'
        case 4:
          return 'D'
        case 5:
          return 'E'
        case 6:
          return 'F'
        default:
          return '' // 默认值，或者可以处理其他情况
      }
    },
    change(index) {
      this.number = index
      this.preText = '上一题'
      this.nextText = '提交答案'
      this.showAnalysis = 0

      this.getQuestionList()

      setTimeout(() => this.getCurrentQuDetial(), 200)
    },

    // 修改 getRightAnswer 方法，确保返回正确答案字符串
    getRightAnswer() {
      // 如果已经有正确答案字符串，直接返回
      if (this.rightQuAnswer.correctAnswerStr) {
        return this.rightQuAnswer.correctAnswerStr
      }
      
      const arr = []
      if (this.rightQuAnswer.data && this.rightQuAnswer.data.options) {
        this.rightQuAnswer.data.options.forEach((option) => {
          if (option.isRight) {
            arr.push(this.numberToLetter(option.sort + 1))
          }
        })
      } else if (this.quDetail && this.quDetail.options) {
        // 如果 rightQuAnswer 中没有 options，从 quDetail 中获取
        this.quDetail.options.forEach((option) => {
          if (option.isRight) {
            arr.push(this.numberToLetter(option.sort + 1))
          }
        })
      }

      let res = arr.join(',')
      if (this.quDetail.quType === 4 && this.quDetail.options && this.quDetail.options.length > 0) {
        res = this.quDetail.options[0].content || ''
      }

      // 缓存结果
      if (res) {
        this.rightQuAnswer.correctAnswerStr = res
      }
      
      return res
    },
    // 按题型选择题号
    async selectQuId(item, index) {
      // 保存当前题目状态（未提交的题目）
      if (!this.isAnswered) {
        if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
          this.currentAnswer.radio = this.radioValue
        } else if (this.quDetail.quType === 2) {
          this.currentAnswer.multi = this.multiValue
        }
      }
      
      this.resetAnswerState()
      
      
      this.curTypeIndex = index
      this.curQuId = item.quId
      if (item.quType === 1) {
        this.curListIndex = 1
      } else if (item.quType === 2) {
        this.curListIndex = 2
      } else if (item.quType === 3) {
        this.curListIndex = 3
      } else if (item.quType === 4) {
        this.curListIndex = 4
      }
      await this.getCurrentQuDetial()
    },
    async getCurrentQuDetial() {
      this.questionLoading = true

      try {
        let quId = ''
        if (this.number === 0) {
          quId = this.quList[this.currentQuIndex].quId
        } else if (this.number === 1) {
          quId = this.curQuId
        }

        const res = await getQuestionDetail(quId)
        this.quDetail = res.data

        // 检查该题目是否已经提交过答案
        const submittedAnswer = this.submittedAnswers[this.quDetail.id]
        if (submittedAnswer) {
          // 设置标志位，防止恢复值时触发 watch
          this.isRestoringState = true

          // 恢复已提交的答案状态
          this.isAnswered = true
          this.rightQuAnswer = submittedAnswer.rightQuAnswer
          this.showAnalysis = 1

          // 恢复用户的答案
          if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
            this.radioValue = submittedAnswer.userAnswer
          } else if (this.quDetail.quType === 2) {
            this.multiValue = submittedAnswer.userAnswer
          }

          // 双重保险：确保正确答案显示
          if (!this.rightQuAnswer.correctAnswerStr) {
            this.extractCorrectAnswerFromQuDetail()
            // 更新保存的答案状态
            this.saveSubmittedAnswer()
          }

          // 延迟重置标志位，确保 watch 不会被触发
          this.$nextTick(() => {
            this.isRestoringState = false
          })
        } else {
          // 未提交过答案，重置状态
          this.isRestoringState = true
          this.isAnswered = false
          this.rightQuAnswer = { data: null, msg: '' }
          this.showAnalysis = 0

          // 重置输入框内容
          if (this.quDetail.quType === 1 || this.quDetail.quType === 3) {
            this.radioValue = this.currentAnswer.radio || ''
          } else if (this.quDetail.quType === 2) {
            this.multiValue = this.currentAnswer.multi || []
          } else if (this.quDetail.quType === 4) {
            // 简答题：直接清空输入框
            this.radioValue = ''
          }

          // 延迟重置标志位
          this.$nextTick(() => {
            this.isRestoringState = false
          })
        }
      } catch (error) {
        console.error('获取题目详情失败:', error)
        this.$message.error('获取题目详情失败，请重试')
      } finally {
        this.questionLoading = false
        // 调用showButton更新按钮状态
        this.showButton()
      }
    },

    // 答题卡样式
    cardItemClass(answered, id, isright) {
      if (id === this.quDetail.id) {
        return 'warning'
      } else if (isright) {
        return 'success'
      } else if (answered) {
        return 'danger'
      } else {
        return 'info'
      }
    },
    // 用户按顺序刷题，初始化试题Id
    initQuId() {
      this.curQuId = this.paperData.radioList[0].quId
    },

    // 用于按顺序刷题，初始化试题顺序
    initCurrentIndex() {
      var exercisedCount = 0

      setTimeout(() => {
        this.quList.forEach((element) => {
          if (element.exercised) {
            this.currentQuIndex++
            exercisedCount++
          }
          if (exercisedCount === this.quList.length) {
            this.currentQuIndex = 0
          }
        })
        this.showButton()
      }, 100)
    },
    // 选择题号
    async selectQuNum(item, index) {
      // 保存当前题目状态（未提交的题目）
      if (!this.isAnswered) {
        if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
          this.currentAnswer.radio = this.radioValue
        } else if (this.quDetail.quType === 2) {
          this.currentAnswer.multi = this.multiValue
        }
      }
      
      this.preText = '上一题'
      this.nextText = '提交答案'
      this.resetAnswerState()
      
      
      this.currentQuIndex = index
      this.showButton()
      await this.getCurrentQuDetial()
    },

    // 题干显示题型
    shouQuType(type) {
      if (type === 1) {
        return '单选题'
      } else if (type === 2) {
        return '多选题'
      } else if (type === 3) {
        return '判断题'
      } else if (type === 4) {
        return '简答题'
      }
    },
    async handNext() {
      try {
        // 保存当前题目状态（未提交的题目）
        if (!this.isAnswered) {
          if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
            this.currentAnswer.radio = this.radioValue
          } else if (this.quDetail.quType === 2) {
            this.currentAnswer.multi = this.multiValue
          }
        }
        
        // 按顺序模式
        if (this.number === 0) {
          if (this.currentQuIndex < this.quList.length - 1) {
            this.currentQuIndex++
          }
        }
        // 按题型模式
        else if (this.number === 1) {
          this.handleQuestionTypeSwitch()
        }
        
        // 重置输入框内容，为新题目做准备
        this.radioValue = ''
        this.multiValue = []
        
        await this.getCurrentQuDetial()
        this.showButton()
      } catch (error) {
        console.error('操作失败:', error)
        this.$message.error('操作失败，请重试')
      }
    },
    async fillAnswer() {
      // 锁已经开了，直接退出，不执行第二次
      if (this.isSubmitting) {
        return
      }

      // 上锁，后面所有重复调用都会被拦截
      this.isSubmitting = true

      try {
        // 准备答案参数
        let answer = '';
        if (this.quDetail.quType === 2) {
          answer = this.multiValue.length > 0 ? this.multiValue.join(',') : '';
        } else {
          answer = this.radioValue || '';
        }

        let params = {
          repoId: this.quDetail.repoId,
          quId: this.quDetail.id,
          answer: answer,
          quType: parseInt(this.quDetail.quType)
        }

        const res = await submitAnswer(params)
        console.log('后端响应:', res)

        // 正确处理结果
        if (res && res.code === 1) {
          const questionData = res.data || {}
          this.rightQuAnswer = {
            data: {
              options: questionData.options || [],
              analysis: questionData.analysis || this.quDetail.analysis || ''
            },
            msg: res.msg || '回答正确'
          }
          this.extractCorrectAnswerFromQuDetail()
        } else {
          this.rightQuAnswer = {
            data: {
              options: this.quDetail.options || [],
              analysis: this.quDetail.analysis || '请参考正确答案'
            },
            msg: '回答错误'
          }
          this.extractCorrectAnswerFromQuDetail()
        }
      } catch (error) {
        console.error('提交答案失败:', error)
        this.rightQuAnswer = {
          data: {
            options: this.quDetail.options || [],
            analysis: this.quDetail.analysis || '请参考正确答案'
          },
          msg: '回答错误'
        }
        this.extractCorrectAnswerFromQuDetail()
      } finally {
        // 👇 解锁
        this.isSubmitting = false
      }
    },
    
    // 从题目详情中提取正确答案
    extractCorrectAnswerFromQuDetail() {
      if (!this.quDetail || !this.quDetail.options || this.quDetail.options.length === 0) {
        return
      }

      // isRight 字段是 Integer 类型：1 表示正确，0 表示错误
      const isCorrect = (option) => option.isRight === 1 || option.isRight === true

      // 确保 rightQuAnswer.data 存在
      if (!this.rightQuAnswer.data) {
        this.rightQuAnswer.data = {
          options: [],
          analysis: this.quDetail.analysis || ''
        }
      }

      // 始终从 quDetail.options 更新 rightQuAnswer.data.options
      // 这样可以确保 options 包含正确的 isRight 标记
      this.rightQuAnswer.data.options = this.quDetail.options.map(option => ({
        ...option,
        isRight: option.isRight
      }))

      // 从题目详情中找出正确答案
      const correctOptions = this.quDetail.options.filter(isCorrect)

      // 设置正确答案字符串
      if (correctOptions.length > 0) {
        const correctLetters = correctOptions.map(opt => this.numberToLetter(opt.sort + 1))
        this.rightQuAnswer.correctAnswerStr = correctLetters.join(',')
      }

      // 简答题特殊处理
      if (this.quDetail.quType === 4) {
        if (this.quDetail.options.length > 0) {
          this.rightQuAnswer.correctAnswerStr = this.quDetail.options[0].content || '暂无正确答案'
        }
      }

      // 确保 msg 字段存在
      if (!this.rightQuAnswer.msg) {
        // 判断用户答案是否正确
        const isAnswerCorrect = this.checkAnswerIsCorrect()
        this.rightQuAnswer.msg = isAnswerCorrect ? '回答正确' : '回答错误'
      }
    },

    // 检查用户答案是否正确
    checkAnswerIsCorrect() {
      if (!this.quDetail || !this.quDetail.options) return false

      // isRight 字段是 Integer 类型：1 表示正确，0 表示错误
      const isCorrect = (option) => option.isRight === 1 || option.isRight === true

      if (this.quDetail.quType === 1 || this.quDetail.quType === 3) {
        // 单选题和判断题：检查用户选择的选项是否为正确选项
        const selectedOption = this.quDetail.options.find(opt => opt.id === this.radioValue)
        return selectedOption ? isCorrect(selectedOption) : false
      } else if (this.quDetail.quType === 2) {
        // 多选题：检查用户选择的所有选项是否都是正确选项
        if (this.multiValue.length === 0) return false
        const correctOptionIds = this.quDetail.options.filter(isCorrect).map(opt => opt.id)
        const userSelectedSorted = [...this.multiValue].sort()
        const correctSorted = [...correctOptionIds].sort()
        return JSON.stringify(userSelectedSorted) === JSON.stringify(correctSorted)
      }
      return false
    },
    async showButton() {
      // 按顺序模式
      if (this.number === 0) {
        // 第一道题：只显示"下一题"按钮
        if (this.currentQuIndex === 0) {
          this.showPrevious = false
          this.showNext = true
        } 
        // 最后一题：只显示"上一题"按钮
        else if (this.currentQuIndex === this.quList.length - 1) {
          this.showPrevious = true
          this.showNext = false
        } 
        // 中间题：显示"上一题"和"下一题"按钮
        else {
          this.showPrevious = true
          this.showNext = true
        }
      }
      // 按题型模式
      else if (this.number === 1) {
        const currentList = this.getCurrentTypeList()
        const isFirstInType = this.curTypeIndex === 0
        const isLastInType = this.curTypeIndex === currentList.length - 1
        const isFirstType = this.curListIndex === 1
        const isLastType = this.curListIndex === this.getLastTypeIndex()
        
        // 第一个题型的第一题：只显示"下一题"按钮
        if (isFirstType && isFirstInType) {
          this.showPrevious = false
          this.showNext = true
        } 
        // 最后一个题型的最后一题：只显示"上一题"按钮
        else if (isLastType && isLastInType) {
          this.showPrevious = true
          this.showNext = false
        } 
        // 中间题：显示"上一题"和"下一题"按钮
        else {
          this.showPrevious = true
          this.showNext = true
        }
      }
      
      // 设置按钮文本
      if (this.showNext) {
        this.nextText = '下一题'
      }
    },
    async handPrevious() {
      // 保存当前题目状态（未提交的题目）
      if (!this.isAnswered) {
        if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
          this.currentAnswer.radio = this.radioValue
        } else if (this.quDetail.quType === 2) {
          this.currentAnswer.multi = this.multiValue
        }
      }
      
      // 重置输入框内容，为新题目做准备
      this.radioValue = ''
      this.multiValue = []
      
      // 按顺序模式
      if (this.number === 0) {
        if (this.currentQuIndex > 0) {
          this.currentQuIndex--
          this.showButton()
          await this.getCurrentQuDetial()
        }
      }
      // 按题型模式
      else if (this.number === 1) {
        if (this.curTypeIndex > 0) {
          this.curTypeIndex--
        } else {
          // 切换到上一个题型的最后一题
          const prevTypeMap = {
            2: { index: 1, list: this.paperData.radioList },
            3: { index: 2, list: this.paperData.multiList },
            4: { index: 3, list: this.paperData.judgeList }
          }
          const prevType = prevTypeMap[this.curListIndex]
          if (prevType && prevType.list.length > 0) {
            this.curListIndex = prevType.index
            this.curTypeIndex = prevType.list.length - 1
          }
        }
        this.curQuId = this.getCurrentTypeList()[this.curTypeIndex].quId
        await this.getCurrentQuDetial()
      }
    },
    // 单选题和判断题点击选项后立即提交答案
    async handleRadioClick(answer) {
      // 如果已经作答，不允许重复选择
      if (this.isAnswered) return

      this.radioValue = answer

      // 单选题和判断题：点击后立即提交并判断
      if (this.quDetail.quType === 1 || this.quDetail.quType === 3) {
        try {
          await this.fillAnswer()
          this.isAnswered = true
          this.showAnalysis = 1

          // 确保正确答案显示
          if (!this.rightQuAnswer.correctAnswerStr) {
            this.extractCorrectAnswerFromQuDetail()
          }

          // 存储已提交的答案状态
          this.saveSubmittedAnswer()

          // 更新答题卡状态
          this.updateCardItemStatus()

          this.showButton()
        } catch (error) {
          console.error('提交答案失败:', error)
        }
      }
    },

    // 更新答题卡中当前题目的状态
    updateCardItemStatus() {
      const currentQuestionId = this.quDetail.id
      const isCorrect = this.rightQuAnswer.msg === '回答正确'

      // 更新 quList 中的状态
      this.quList.forEach(item => {
        if (item.quId === currentQuestionId) {
          item.exercised = true
          item.isRight = isCorrect
        }
      })

      // 更新 paperData 中的状态
      const updateList = (list) => {
        list.forEach(item => {
          if (item.quId === currentQuestionId) {
            item.exercised = true
            item.isRight = isCorrect
          }
        })
      }

      updateList(this.paperData.radioList)
      updateList(this.paperData.multiList)
      updateList(this.paperData.judgeList)
      updateList(this.paperData.saqList)
    },

    // 提交答案方法（仅用于多选题和简答题）
    async handSubmitAnswer() {
      // 确保仅处理多选题和简答题
      if (this.quDetail.quType !== 2 && this.quDetail.quType !== 4) {
        return
      }

      // 多选题需要至少选择一个选项
      if (this.quDetail.quType === 2 && this.multiValue.length === 0) {
        this.$message.warning('请至少选择一个选项')
        return
      }

      // 简答题需要输入答案
      if (this.quDetail.quType === 4 && (!this.radioValue || this.radioValue.trim() === '')) {
        this.$message.warning('请输入答案')
        return
      }

      try {
        await this.fillAnswer()
        this.isAnswered = true
        this.showAnalysis = 1

        // 双重保险：确保正确答案显示
        if (!this.rightQuAnswer.correctAnswerStr) {
          this.extractCorrectAnswerFromQuDetail()
        }

        // 存储已提交的答案状态
        this.saveSubmittedAnswer()

        // 更新答题卡状态
        this.updateCardItemStatus()

        this.showButton()
      } catch (error) {
        console.error('提交答案失败:', error)
        this.isAnswered = true
        this.showAnalysis = 1

        // 从题目详情中提取正确答案
        this.rightQuAnswer = {
          data: {
            options: this.quDetail.options || [],
            analysis: this.quDetail.analysis || '请参考正确答案'
          },
          msg: '回答错误'
        }
        this.extractCorrectAnswerFromQuDetail()

        // 存储已提交的答案状态
        this.saveSubmittedAnswer()

        // 更新答题卡状态
        this.updateCardItemStatus()

        this.showButton()
      }
    },
    
    // 保存已提交的答案状态
    saveSubmittedAnswer() {
      let userAnswer = ''
      if (this.quDetail.quType === 1 || this.quDetail.quType === 3 || this.quDetail.quType === 4) {
        userAnswer = this.radioValue
      } else if (this.quDetail.quType === 2) {
        userAnswer = this.multiValue
      }
      
      this.submittedAnswers[this.quDetail.id] = {
        userAnswer: userAnswer,
        rightQuAnswer: JSON.parse(JSON.stringify(this.rightQuAnswer))
      }
      
      // 保存到本地存储
      this.saveSubmittedAnswersToLocal()
    },
    
    // 从本地存储加载已提交的答案状态
    loadSubmittedAnswers() {
      try {
        const storedData = localStorage.getItem(`submittedAnswers_${this.repoId}`)
        if (storedData) {
          this.submittedAnswers = JSON.parse(storedData)
        }
      } catch (error) {
        console.error('加载本地存储失败:', error)
        this.submittedAnswers = {}
      }
    },
    
    // 保存已提交的答案状态到本地存储
    saveSubmittedAnswersToLocal() {
      try {
        localStorage.setItem(`submittedAnswers_${this.repoId}`, JSON.stringify(this.submittedAnswers))
      } catch (error) {
        console.error('保存到本地存储失败:', error)
      }
    },
    
    // 清除本地存储的答案状态
    clearSubmittedAnswers() {
      try {
        localStorage.removeItem(`submittedAnswers_${this.repoId}`)
        this.submittedAnswers = {}

        // 重置当前题目的状态
        this.isAnswered = false
        this.rightQuAnswer = { data: null, msg: '' }
        this.showAnalysis = 0
        this.radioValue = ''
        this.multiValue = []

        // 重置答题卡状态
        this.quList.forEach(item => {
          item.exercised = false
          item.isRight = false
        })

        // 重置 paperData 中的状态
        const resetList = (list) => {
          list.forEach(item => {
            item.exercised = false
            item.isRight = false
          })
        }
        resetList(this.paperData.radioList)
        resetList(this.paperData.multiList)
        resetList(this.paperData.judgeList)
        resetList(this.paperData.saqList)

        // 重置当前答案
        this.currentAnswer = {
          radio: '',
          multi: [],
          saq: ''
        }
      } catch (error) {
        console.error('清除本地存储失败:', error)
      }
    },
    isLastQuestion() {
      if (this.number === 1) {
        const currentList = this.getCurrentTypeList()
        return this.curTypeIndex === currentList.length - 1 &&
               this.curListIndex === this.getLastTypeIndex()
      }
      return false
    },
    getCurrentTypeList() {
      switch (this.curListIndex) {
        case 1: return this.paperData.radioList
        case 2: return this.paperData.multiList
        case 3: return this.paperData.judgeList
        case 4: return this.paperData.saqList
      }
    },
    getLastTypeIndex() {
      if (this.paperData.saqList.length > 0) return 4
      if (this.paperData.judgeList.length > 0) return 3
      if (this.paperData.multiList.length > 0) return 2
      return 1
    }
  }
}
</script>

<style scoped>
page {
  background: #ebecee;
}

.btn_anniu {
  width: 50%;
  padding: 10px 0;
  font-size: 19px;
  font-weight: bold;
  border: 0 solid #fff;
  color: #000;
  outline: none;
  background: #fff;
}

.newStyle {
  border-bottom: 2px solid #f0892e;
  color: #f0892e;
  font-size: 21px;
  font-weight: bold;
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
  height: calc(100vh - 110px);
  overflow-y: auto;
}

.card-title {
  background: #eee;
  line-height: 35px;
  text-align: center;
  font-size: 14px;
}

.card-line {
  padding-left: 10px;
}

.card-line span {
  cursor: pointer;
  margin: 2px;
}

/* 彻底移除当前选项显示格式，统一采用新的展示规范 */
:deep(.el-radio),
:deep(.el-checkbox) {
  padding: 5px 0;
  border: none;
  margin-bottom: 8px;
  width: auto;
}

:deep(.el-radio__label),
:deep(.el-checkbox__label) {
  line-height: 24px;
  margin-left: 8px;
}

:deep(.el-radio__input),
:deep(.el-checkbox__input) {
  display: inline-block;
}

:deep(.el-radio__inner),
:deep(.el-checkbox__inner) {
  display: inline-block;
}

.el-radio img,
.el-checkbox img {
  max-width: 200px;
  max-height: 200px;
  margin-top: 5px;
}

 .bg-green {
    background-color: #dff0d8; /* 绿色背景 */
    color: #3c763d;
    padding: 2px 4px;
    border-radius: 3px;
  }
  .bg-red {
    background-color: #f2dede; /* 红色背景 */
    color: #a94442;
    padding: 2px 4px;
    border-radius: 3px;
  }

  /* 选项正确样式 - 绿色高亮 */
  .option-correct {
    background-color: #67c23a;
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    display: inline-block;
    font-weight: bold;
  }

  /* 选项错误样式 - 红色高亮 */
  .option-wrong {
    background-color: #f56c6c;
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    display: inline-block;
    font-weight: bold;
  }

  /* 答案反馈区域样式 */
  .answer-feedback {
    margin-top: 20px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 8px;
    border-left: 4px solid #409eff;
  }

  .answer-result {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 15px;
  }

  .result-correct {
    background-color: #67c23a;
    color: #fff;
    padding: 8px 16px;
    border-radius: 6px;
    display: inline-block;
  }

  .result-wrong {
    background-color: #f56c6c;
    color: #fff;
    padding: 8px 16px;
    border-radius: 6px;
    display: inline-block;
  }

  /* 正确选项显示区域 */
  .correct-options-display {
    margin-top: 15px;
    padding: 15px;
    background-color: #fff;
    border-radius: 8px;
    border: 1px solid #e1f3d8;
  }

  .correct-options-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: bold;
    color: #67c23a;
    margin-bottom: 12px;
  }

  .correct-options-title .el-icon {
    font-size: 20px;
  }

  .correct-options-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .correct-option-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 10px 15px;
    background-color: #f0f9eb;
    border-radius: 6px;
    border: 1px solid #c2e7b0;
  }

  .option-letter {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    background-color: #67c23a;
    color: #fff;
    border-radius: 50%;
    font-weight: bold;
    font-size: 14px;
    flex-shrink: 0;
  }

  .option-text {
    flex: 1;
    color: #303133;
    line-height: 28px;
  }

  .option-image {
    margin-top: 8px;
    width: 100%;
  }

  /* 简答题答案区域 */
  .saq-answer-section {
    margin-top: 15px;
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .my-answer-section,
  .correct-answer-section {
    padding: 15px;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
  }

  .my-answer-section {
    background-color: #ecf5ff;
    border-color: #d9ecff;
  }

  .correct-answer-section {
    background-color: #f0f9eb;
    border-color: #c2e7b0;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: bold;
    margin-bottom: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e4e7ed;
  }

  .section-title .el-icon {
    font-size: 18px;
  }

  .my-answer-title {
    color: #409eff;
  }

  .correct-answer-title {
    color: #67c23a;
  }

  .section-content {
    color: #303133;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .my-answer-content {
    color: #606266;
  }

  .correct-answer-content {
    color: #67c23a;
    font-weight: 500;
  }

  /* 试题分析 */
  .answer-analysis {
    margin-top: 15px;
    padding: 15px;
    background-color: #fff;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
  }

  .analysis-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: bold;
    color: #909399;
    margin-bottom: 10px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e4e7ed;
  }

  .analysis-title .el-icon {
    font-size: 18px;
  }

  .analysis-content {
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .statistics-container {
  text-align: center;
  padding: 20px;
  background: #fff;
}
.stat-row {
  margin-bottom: 10px;
}
.stat-item {
  background-color: #f7f9fc;
  padding: 10px;
  border-radius: 8px;
}
.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.question-type {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-right: 8px;
}

.single-choice {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.multiple-choice {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.judgment {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.short-answer {
  background-color: #f9f0ff;
  color: #722ed1;
  border: 1px solid #d3adf7;
}

</style>
