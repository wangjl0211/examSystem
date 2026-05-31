<template>
  <div style="width: 100%; height: 100vh; background-color: #f8f9fa; overflow: hidden; padding: 20px 20px 20px 20px; box-sizing: border-box;">
    <!-- 整个容器使用flex布局，固定高度，禁止滚动 -->
    <div style="display: flex; flex-direction: column; height: 100%; overflow: hidden;">
      
      <!-- Header区域 - 固定顶部（可选，可以根据需要保留或移除） -->
      <el-row :gutter="24" style="flex-shrink: 0;" v-if="false">
        <el-col :span="24">
          <el-card style="margin-bottom: 10px">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div>
                <span style="font-size: 18px; font-weight: bold">试卷总分：</span>
                <span style="font-size: 24px; font-weight: bold; color: #4CAF50">{{ totalScore }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 主体内容区域 - flex布局，高度自适应 -->
      <el-row :gutter="24" style="flex: 1; overflow: hidden; margin-top: 0;">
        
        <!-- 左侧答题卡区域 - 固定宽度，独立滚动 -->
        <el-col :span="5" :xs="24" style="height: 100%; overflow: hidden;">
          <el-card class="answer-card" :body-style="{ height: '100%', padding: '15px', overflow: 'hidden', display: 'flex', flexDirection: 'column' }">
            <p class="card-title">题目列表</p>
            

            <!-- 答题卡内容区域 - 滚动 -->
            <div class="answer-card-content">
              <!-- 单选题答题卡 -->
              <div v-if="hasQuestions(questionGroups.radioList)">
                <p class="card-subtitle">单选题</p>
                <el-row :gutter="24" class="card-line" style="padding-left: 10px">
                  <el-tag
                    v-for="question in questionGroups.radioList"
                    :key="question.id"
                    :type="getQuestionStatus(question)"
                    style="flex: 0 0 calc(12.5% - 4px); max-width: calc(12.5% - 4px); text-align: center; margin: 2px; cursor: pointer; transition: all 0.2s ease; box-sizing: border-box;"
                    :class="{
                      'tag-clicked': clickedItem === question.id,
                      'tag-current': currentQuestionId === question.id
                    }"
                    @click="scrollToQuestion(question.id)"
                  >
                    {{ question.sort + 1 }}
                  </el-tag>
                </el-row>
              </div>

              <!-- 多选题答题卡 -->
              <div v-if="hasQuestions(questionGroups.multiList)">
                <p class="card-subtitle">多选题</p>
                <el-row :gutter="24" class="card-line" style="padding-left: 10px">
                  <el-tag
                    v-for="question in questionGroups.multiList"
                    :key="question.id"
                    :type="getQuestionStatus(question)"
                    style="flex: 0 0 calc(12.5% - 4px); max-width: calc(12.5% - 4px); text-align: center; margin: 2px; cursor: pointer; transition: all 0.2s ease; box-sizing: border-box;"
                    :class="{
                      'tag-clicked': clickedItem === question.id,
                      'tag-current': currentQuestionId === question.id
                    }"
                    @click="scrollToQuestion(question.id)"
                  >
                    {{ question.sort + 1 }}
                  </el-tag>
                </el-row>
              </div>

              <!-- 判断题答题卡 -->
              <div v-if="hasQuestions(questionGroups.judgeList)">
                <p class="card-subtitle">判断题</p>
                <el-row :gutter="24" class="card-line" style="padding-left: 10px">
                  <el-tag
                    v-for="question in questionGroups.judgeList"
                    :key="question.id"
                    :type="getQuestionStatus(question)"
                    style="flex: 0 0 calc(12.5% - 4px); max-width: calc(12.5% - 4px); text-align: center; margin: 2px; cursor: pointer; transition: all 0.2s ease; box-sizing: border-box;"
                    :class="{
                      'tag-clicked': clickedItem === question.id,
                      'tag-current': currentQuestionId === question.id
                    }"
                    @click="scrollToQuestion(question.id)"
                  >
                    {{ question.sort + 1 }}
                  </el-tag>
                </el-row>
              </div>

              <!-- 简答题答题卡 -->
              <div v-if="hasQuestions(questionGroups.saqList)">
                <p class="card-subtitle">简答题</p>
                <el-row :gutter="24" class="card-line" style="padding-left: 10px">
                  <el-tag
                    v-for="question in questionGroups.saqList"
                    :key="question.id"
                    :type="getQuestionStatus(question)"
                    style="flex: 0 0 calc(12.5% - 4px); max-width: calc(12.5% - 4px); text-align: center; margin: 2px; cursor: pointer; transition: all 0.2s ease; box-sizing: border-box;"
                    :class="{
                      'tag-clicked': clickedItem === question.id,
                      'tag-current': currentQuestionId === question.id
                    }"
                    @click="scrollToQuestion(question.id)"
                  >
                    {{ question.sort + 1 }}
                  </el-tag>
                </el-row>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧题目区域 - 独立滚动 -->
        <el-col :span="19" :xs="24" style="height: 100%; overflow: hidden;">
          <el-card class="questions-card" :body-style="{ height: '100%', padding: '20px', overflow: 'hidden' }">
            <div class="questions-container" ref="questionsContainer" @scroll="onQuestionsScroll">
              <!-- 单选题 -->
              <div v-if="hasQuestions(questionGroups.radioList)" class="question-section">
                <h3 class="section-title">单选题</h3>
                <div v-for="question in questionGroups.radioList" :key="question.id" 
                     :id="'question-' + question.id" 
                     class="question-item"
                     :class="{ 'question-active': currentQuestionId === question.id }"
                     :ref="'question-' + question.id">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <span class="score-badge">分值: {{ question.score || 1 }}分</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-radio-group disabled :model-value="getCorrectOptionContent(question, question.rightOption)">
                    <el-radio
                      v-for="(item, indexs) in question.option"
                      :key="indexs"
                      :label="item.content"
                      class="option-item"
                      :class="{ 'option-correct': item.isRight }"
                    >
                      {{ numberToLetter(indexs) }}. {{ item.content }}
                      <div v-if="item.image != null && item.image != ''" style="clear: both; margin-top: 10px">
                        <el-image
                          :src="item.image"
                          :preview-src="[item.image]"
                          style="max-width: 200px"
                        />
                      </div>
                    </el-radio>
                  </el-radio-group>
                  <!-- 答案对比区域 -->
                  <div class="qu_analysis">
                    <el-card class="analysis-card">
                      <div class="analysis-item">
                        <span class="analysis-label">正确选项：</span>
                        <span class="analysis-value">{{ numberToLetter(question.rightOption) }}. {{ getCorrectOptionContent(question, question.rightOption) }}</span>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">试题解析：</span>
                        <span class="analysis-value">{{ question.analyse || '无解析' }}</span>
                      </div>
                    </el-card>
                  </div>
                </div>
              </div>
              
              <!-- 多选题 -->
              <div v-if="hasQuestions(questionGroups.multiList)" class="question-section">
                <h3 class="section-title">多选题</h3>
                <div v-for="question in questionGroups.multiList" :key="question.id" 
                     :id="'question-' + question.id" 
                     class="question-item"
                     :class="{ 'question-active': currentQuestionId === question.id }"
                     :ref="'question-' + question.id">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <span class="score-badge">分值: {{ question.score || 1 }}分</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-checkbox-group disabled :model-value="getCorrectMultiOptionContent(question, question.rightOption)">
                    <el-checkbox
                      v-for="(item, indexs) in question.option"
                      :key="indexs"
                      :label="item.content"
                      class="option-item"
                      :class="{ 'option-correct': item.isRight }"
                    >
                      {{ numberToLetter(indexs) }}. {{ item.content }}
                      <div v-if="item.image != null && item.image != ''" style="clear: both; margin-top: 10px">
                        <el-image
                          :src="item.image"
                          :preview-src="[item.image]"
                          style="max-width: 200px"
                        />
                      </div>
                    </el-checkbox>
                  </el-checkbox-group>
                  <!-- 答案对比区域 -->
                  <div class="qu_analysis">
                    <el-card class="analysis-card">
                      <div class="analysis-item">
                        <span class="analysis-label">正确选项：</span>
                        <span class="analysis-value">{{ formatMultiOptionWithContent(question) }}</span>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">试题解析：</span>
                        <span class="analysis-value">{{ question.analyse || '无解析' }}</span>
                      </div>
                    </el-card>
                  </div>
                </div>
              </div>
              
              <!-- 判断题 -->
              <div v-if="hasQuestions(questionGroups.judgeList)" class="question-section">
                <h3 class="section-title">判断题</h3>
                <div v-for="question in questionGroups.judgeList" :key="question.id" 
                     :id="'question-' + question.id" 
                     class="question-item"
                     :class="{ 'question-active': currentQuestionId === question.id }"
                     :ref="'question-' + question.id">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <span class="score-badge">分值: {{ question.score || 1 }}分</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-radio-group disabled :model-value="getCorrectOptionContent(question, question.rightOption)">
                    <el-radio
                      v-for="(item, indexs) in question.option"
                      :key="indexs"
                      :label="item.content"
                      class="option-item"
                      :class="{ 'option-correct': item.isRight }"
                    >
                      {{ numberToLetter(indexs) }}. {{ item.content }}
                      <div v-if="item.image != null && item.image != ''" style="clear: both; margin-top: 10px">
                        <el-image
                          :src="item.image"
                          :preview-src="[item.image]"
                          style="max-width: 200px"
                        />
                      </div>
                    </el-radio>
                  </el-radio-group>
                  <!-- 答案对比区域 -->
                  <div class="qu_analysis">
                    <el-card class="analysis-card">
                      <div class="analysis-item">
                        <span class="analysis-label">正确选项：</span>
                        <span class="analysis-value">{{ numberToLetter(question.rightOption) }}. {{ getCorrectOptionContent(question, question.rightOption) }}</span>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">试题解析：</span>
                        <span class="analysis-value">{{ question.analyse || '无解析' }}</span>
                      </div>
                    </el-card>
                  </div>
                </div>
              </div>
              
              <!-- 简答题 -->
              <div v-if="hasQuestions(questionGroups.saqList)" class="question-section">
                <h3 class="section-title">简答题</h3>
                <div v-for="question in questionGroups.saqList" :key="question.id" 
                     :id="'question-' + question.id" 
                     class="question-item"
                     :class="{ 'question-active': currentQuestionId === question.id }"
                     :ref="'question-' + question.id">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <span class="score-badge">分值: {{ question.score || 1 }}分</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <!-- 答案对比区域 -->
                  <div class="qu_analysis">
                    <el-card class="analysis-card">
                      <div class="analysis-item">
                        <span class="analysis-label">正确答案：</span>
                        <div class="analysis-content">{{ question.rightOption || '无标准答案' }}</div>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">试题解析：</span>
                        <div class="analysis-content">{{ question.analyse || '无解析' }}</div>
                      </div>
                    </el-card>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { details } from '@/api/exam'
import { numberToLetter, hasQuestions, groupQuestionsByType } from '@/utils/questionFormat'

export default {
  name: 'ExamPaperPreview',
  data() {
    return {
      input: "",
      quIndex: -1,
      data: null,
      userId: null,
      examId:"",
      clickedItem: null,
      currentQuestionId: null, // 当前可见的题目ID
      scrollTimeout: null, // 滚动节流定时器
      // 题目分组
      questionGroups: {
        radioList: [],
        multiList: [],
        judgeList: [],
        saqList: []
      }
    };
  },
  computed: {
    // 计算总分
    totalScore() {
      let total = 0;
      Object.values(this.questionGroups).forEach(group => {
        group.forEach(question => {
          total += question.score || 1;
        });
      });
      return total;
    }
  },
  created() {
    console.log("this.$route.query", this.$route.query.examId);
    this.examId = localStorage.getItem("exam-details-examId")
    this.ExamDetail();
  },
  mounted() {
    // 添加滚动监听
    this.$nextTick(() => {
      const container = this.$refs.questionsContainer;
      if (container) {
        container.addEventListener('scroll', this.onQuestionsScroll);
      }
    });
  },
  beforeUnmount() {
    // 移除滚动监听
    const container = this.$refs.questionsContainer;
    if (container) {
      container.removeEventListener('scroll', this.onQuestionsScroll);
    }
    if (this.scrollTimeout) {
      clearTimeout(this.scrollTimeout);
    }
  },
  methods: {
    hasQuestions,
    // 分页查询
    async ExamDetail() {
      const res = await details(this.examId);
      this.data = res.data;
      
      // 处理题目数据，添加必要字段
      const processedQuestions = this.processQuestions(res.data);
      
      // 分组题目
      this.groupQuestions(processedQuestions);
      
      // 默认选中第一题
      this.$nextTick(() => {
        if (processedQuestions.length > 0) {
          this.currentQuestionId = processedQuestions[0].id;
        }
      });
    },
    // 处理题目数据，添加必要字段
    processQuestions(questions) {
      return questions.map((question, index) => {
        // 确保每个题目有score字段
        const score = question.score || 1;
        
        return {
          ...question,
          id: index + 1, // 添加id字段用于定位
          sort: index, // 添加sort字段用于显示序号
          score: score, // 题目分数
        };
      });
    },
    // 分组题目
    groupQuestions(questions) {
      this.questionGroups = groupQuestionsByType(questions)
    },
    numberToLetter,

    // 格式化多选题选项（仅字母）
    formatMultiOption(option) {
      if (!option) return '无正确选项';
      const options = option.split(',').map(num => this.numberToLetter(num));
      return options.join(', ');
    },
    // 格式化多选题选项（包含内容）
    formatMultiOptionWithContent(question) {
      if (!question || !question.rightOption || !question.option) return '无正确选项';
      
      const rightIndices = question.rightOption.split(',').map(num => parseInt(num));
      const result = [];
      
      rightIndices.forEach(index => {
        if (!isNaN(index) && index >= 0 && index < question.option.length) {
          const option = question.option[index];
          result.push(`${this.numberToLetter(index)}. ${option.content}`);
        }
      });
      
      return result.join('；') || '无正确选项';
    },
    // 获取单选题和判断题的正确选项内容
    getCorrectOptionContent(question, rightOption) {
      if (!question || !question.option || !rightOption) return '';
      
      // 查找正确选项的索引
      const rightIndex = parseInt(rightOption);
      if (isNaN(rightIndex) || rightIndex < 0 || rightIndex >= question.option.length) {
        return '';
      }
      
      // 返回正确选项的内容
      return question.option[rightIndex].content;
    },
    // 获取多选题的正确选项内容数组
    getCorrectMultiOptionContent(question, rightOption) {
      if (!question || !question.option || !rightOption) return [];
      
      // 解析正确选项索引数组
      const rightIndices = rightOption.split(',').map(num => parseInt(num));
      const correctContents = [];
      
      // 查找每个正确选项的内容
      rightIndices.forEach(index => {
        if (!isNaN(index) && index >= 0 && index < question.option.length) {
          correctContents.push(question.option[index].content);
        }
      });
      
      return correctContents;
    },
    // 获取题目状态（用于左侧答题卡）
    getQuestionStatus(question) {
      // 教师端只使用一种状态，不需要区分正确错误
      // 如果当前题目是当前可见的题目，返回特殊样式
      if (this.currentQuestionId === question.id) {
        return 'primary';
      }
      return 'info';
    },
    // 滚动到指定题目
    scrollToQuestion(questionId) {
      this.clickedItem = questionId;
      setTimeout(() => {
        this.clickedItem = null;
      }, 200);
      
      const element = document.getElementById('question-' + questionId);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        // 更新当前题目ID
        this.currentQuestionId = questionId;
      }
    },
    // 监听右侧滚动，更新当前可见的题目
    onQuestionsScroll() {
      if (this.scrollTimeout) {
        clearTimeout(this.scrollTimeout);
      }
      
      this.scrollTimeout = setTimeout(() => {
        const container = this.$refs.questionsContainer;
        if (!container) return;
        
        const containerHeight = container.clientHeight;
        
        // 获取所有题目元素
        const questionElements = document.querySelectorAll('[id^="question-"]');
        let currentVisibleId = null;
        let minDistance = Infinity;
        
        questionElements.forEach(element => {
          const rect = element.getBoundingClientRect();
          const containerRect = container.getBoundingClientRect();
          
          // 计算元素中心点到容器中心的距离
          const elementCenter = rect.top + rect.height / 2;
          const containerCenter = containerRect.top + containerHeight / 2;
          const distance = Math.abs(elementCenter - containerCenter);
          
          if (distance < minDistance) {
            minDistance = distance;
            const id = element.id.replace('question-', '');
            currentVisibleId = parseInt(id);
          }
        });
        
        if (currentVisibleId && currentVisibleId !== this.currentQuestionId) {
          this.currentQuestionId = currentVisibleId;
        }
      }, 100);
    },
  },
};
</script>

<style scoped lang="scss">
/* 重置全局滚动 */
html, body {
  overflow: hidden;
  height: 100%;
  margin: 0;
  padding: 0;
}

// 答题卡样式
.answer-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .answer-card-content {
    flex: 1;
    overflow-y: auto;
    padding-right: 5px;
    margin-top: 10px;
    
    // 滚动条样式
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

// 题目卡片样式
.questions-card {
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

// 题目容器 - 独立滚动区域
.questions-container {
  height: 100%;
  overflow-y: auto;
  padding-right: 10px;
  
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

// 标题样式
.card-title {
  background: #409EFF;
  color: white;
  line-height: 40px;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  margin: -15px -15px 10px -15px;
  border-radius: 8px 8px 0 0;
}

.card-subtitle {
  background: #f5f7fa;
  line-height: 32px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  margin: 5px 0 10px 0;
  border-radius: 4px;
  color: #606266;
}

.card-line {
  padding-left: 10px;
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
  overflow-x: hidden;
  width: 100%;
  box-sizing: border-box;
}

// 标签样式
.tag-clicked {
  transform: scale(0.95);
  opacity: 0.8;
  box-shadow: 0 0 0 2px #409EFF;
}

.tag-current {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: white !important;
  font-weight: bold;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

// 确保答题卡内容不出现横向滚动
.answer-card-content {
  overflow-x: hidden;
}

.tag-success {
  background-color: #f0f9eb !important;
  border-color: #e1f3d8 !important;
  color: #67c23a !important;
}

.tag-danger {
  background-color: #fef0f0 !important;
  border-color: #fde2e2 !important;
  color: #f56c6c !important;
}

.tag-info {
  background-color: #f4f4f5 !important;
  border-color: #e9e9eb !important;
  color: #909399 !important;
}

// 题目区域样式
.question-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  color: #303133;
}

.question-item {
  margin-bottom: 25px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  &.question-active {
    border-left: 4px solid #409EFF;
    background-color: #f0f9ff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  }
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.question-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.score-badge {
  font-size: 14px;
  color: #409EFF;
  background-color: #ecf5ff;
  padding: 2px 8px;
  border-radius: 10px;
}

.question-image {
  margin-bottom: 15px;
}

.options-group {
  margin-top: 10px;
  
  .option-item {
    padding: 8px 12px;
    margin: 5px 0;
    border-radius: 4px;
    transition: all 0.2s ease;
    width: 100%;
    
    &.option-correct {
      background-color: #f0f9eb;
      border-left: 3px solid #67c23a;
    }
  }
}

// 答案分析区域
.qu_analysis {
  margin-top: 20px;
  
  .analysis-card {
    border-left: 4px solid #409EFF;
    border-radius: 4px;
    
    .analysis-item {
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .analysis-label {
        font-weight: 500;
        color: #606266;
        display: inline-block;
        width: 80px;
      }
      
      .analysis-value {
        color: #303133;
      }
      
      .analysis-content {
        margin-top: 5px;
        padding: 10px;
        background-color: #f9f9f9;
        border-radius: 4px;
        white-space: pre-wrap;
        line-height: 1.6;
      }
    }
  }
}

// 响应式适配
@media screen and (max-width: 1200px) {
  .el-col {
    &[span="5"] {
      flex: 0 0 25%;
      max-width: 25%;
    }
    
    &[span="19"] {
      flex: 0 0 75%;
      max-width: 75%;
    }
  }
}

@media screen and (max-width: 992px) {
  .el-col {
    &[span="5"] {
      flex: 0 0 30%;
      max-width: 30%;
    }
    
    &[span="19"] {
      flex: 0 0 70%;
      max-width: 70%;
    }
  }
}

@media screen and (max-width: 768px) {
  .el-col {
    &[span="5"],
    &[span="19"] {
      flex: 0 0 100%;
      max-width: 100%;
    }
  }
  
  .answer-card {
    margin-bottom: 20px;
    height: 300px;
  }
  
  .questions-container {
    max-height: calc(100vh - 380px);
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .score-badge {
    align-self: flex-start;
  }
}

@media screen and (max-width: 480px) {
  .question-item {
    padding: 15px;
  }
  
  .analysis-label {
    display: block !important;
    width: 100% !important;
    margin-bottom: 5px;
  }
  
  .answer-card {
    height: 250px;
  }
}

// 增强选中状态的视觉效果
:deep(.el-radio.is-checked .el-radio__label) {
  font-weight: bold !important;
  color: #409EFF !important;
}

:deep(.el-checkbox.is-checked .el-checkbox__label) {
  font-weight: bold !important;
  color: #409EFF !important;
}
</style>