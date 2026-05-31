<template>
  <div style="width: 100%; height: 100vh; background-color: #f8f9fa; overflow: hidden; padding: 20px 20px 20px 20px; box-sizing: border-box;">
    <!-- 整个容器使用flex布局，固定高度，禁止滚动 -->
    <div style="display: flex; flex-direction: column; height: 100%; overflow: hidden;">
      
      <!-- Header区域 - 固定顶部 -->
      <el-row :gutter="24" style="flex-shrink: 0;">
        <el-col :span="24">
          <el-card style="margin-bottom: 10px">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div>
                <span style="font-size: 18px; font-weight: bold">得分：</span>
                <span style="font-size: 24px; font-weight: bold; color: #4CAF50">{{ userScore }} / {{ totalScore }}</span>
              </div>
              <div>
                <span style="font-size: 14px; color: #666">考试用时：{{ formatUserTime(userTime) }}</span>
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
            <p class="card-title">答题卡</p>
            
            <!-- 状态标签 - 固定 -->
            <el-row :gutter="24" class="card-line" style="padding-left: 10px; flex-shrink: 0;">
              <el-tag type="success" style="margin: 2px">正确</el-tag>
              <el-tag type="danger" style="margin: 2px">错误</el-tag>
              <el-tag type="info" style="margin: 2px">未作答</el-tag>
            </el-row>

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
                      'tag-success': question.isRight === 1,
                      'tag-danger': question.isRight === 0 && (question.myOption !== undefined && question.myOption !== null && question.myOption !== ''),
                      'tag-info': (question.myOption === undefined || question.myOption === null || question.myOption === '')
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
                      'tag-success': question.isRight === 1,
                      'tag-danger': question.isRight === 0 && (question.myOption !== undefined && question.myOption !== null && question.myOption !== ''),
                      'tag-info': (question.myOption === undefined || question.myOption === null || question.myOption === '')
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
                      'tag-success': question.isRight === 1,
                      'tag-danger': question.isRight === 0 && (question.myOption !== undefined && question.myOption !== null && question.myOption !== ''),
                      'tag-info': (question.myOption === undefined || question.myOption === null || question.myOption === '')
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
                      'tag-success': question.isRight === 1,
                      'tag-danger': question.isRight === 0 && (question.myOption !== undefined && question.myOption !== null && question.myOption !== ''),
                      'tag-info': (question.myOption === undefined || question.myOption === null || question.myOption === '')
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
            <div class="questions-container" ref="questionsContainer">
              <!-- 单选题 -->
              <div v-if="hasQuestions(questionGroups.radioList)" class="question-section">
                <h3 class="section-title">单选题</h3>
                <div v-for="question in questionGroups.radioList" :key="question.id" 
                     :id="'question-' + question.id" 
                     class="question-item"
                     :class="{ 'question-active': clickedItem === question.id }">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <el-tag :type="getQuestionStatus(question)" 
                           size="small" 
                           class="score-tag">
                      {{ getQuestionStatusText(question) }}
                    </el-tag>
                    <span class="score-badge">得分: {{ question.userScore }}/{{ question.score }}</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-radio-group disabled class="options-group" :model-value="getSelectedOptionContent(question)">
                    <el-radio
                      v-for="(item, indexs) in question.option"
                      :key="indexs"
                      :label="item.content"
                      class="option-item"
                      :class="{
                        'is-correct-answer': item.isRight,
                        'is-user-selected': question.myOption === indexs,
                        'is-correct-selected': item.isRight && question.myOption === indexs
                      }"
                    >
                      <span class="option-label">{{ numberToLetter(indexs) }}.</span>
                      <span class="option-content">{{ item.content }}</span>
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
                        <span class="analysis-label">我的选项：</span>
                        <span class="analysis-value">{{ numberToLetter(question.myOption) || '未作答' }}</span>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">正确选项：</span>
                        <span class="analysis-value">{{ numberToLetter(question.rightOption) }}</span>
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
                     :class="{ 'question-active': clickedItem === question.id }">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <el-tag :type="getQuestionStatus(question)" 
                           size="small" 
                           class="score-tag">
                      {{ getQuestionStatusText(question) }}
                    </el-tag>
                    <span class="score-badge">得分: {{ question.userScore }}/{{ question.score }}</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-checkbox-group disabled class="options-group" :model-value="getSelectedMultiOptionContent(question)">
                    <el-checkbox
                      v-for="(item, indexs) in question.option"
                      :key="indexs"
                      :label="item.content"
                      class="option-item"
                      :class="{
                        'is-correct-answer': item.isRight,
                        'is-user-selected': question.myOption && question.myOption.split(',').map(Number).includes(indexs),
                        'is-correct-selected': item.isRight && question.myOption && question.myOption.split(',').map(Number).includes(indexs)
                      }"
                    >
                      <span class="option-label">{{ numberToLetter(indexs) }}.</span>
                      <span class="option-content">{{ item.content }}</span>
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
                        <span class="analysis-label">我的选项：</span>
                        <span class="analysis-value">{{ formatMultiOption(question.myOption) || '未作答' }}</span>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">正确选项：</span>
                        <span class="analysis-value">{{ formatMultiOption(question.rightOption) }}</span>
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
                     :class="{ 'question-active': clickedItem === question.id }">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <el-tag :type="getQuestionStatus(question)" 
                           size="small" 
                           class="score-tag">
                      {{ getQuestionStatusText(question) }}
                    </el-tag>
                    <span class="score-badge">得分: {{ question.userScore }}/{{ question.score }}</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-radio-group disabled class="options-group" :model-value="getSelectedOptionContent(question)">
                    <el-radio
                      v-for="(item, indexs) in question.option"
                      :key="indexs"
                      :label="item.content"
                      class="option-item"
                      :class="{
                        'is-correct-answer': item.isRight,
                        'is-user-selected': question.myOption === indexs,
                        'is-correct-selected': item.isRight && question.myOption === indexs
                      }"
                    >
                      <span class="option-label">{{ numberToLetter(indexs) }}.</span>
                      <span class="option-content">{{ item.content }}</span>
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
                        <span class="analysis-label">我的选项：</span>
                        <span class="analysis-value">{{ numberToLetter(question.myOption) || '未作答' }}</span>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">正确选项：</span>
                        <span class="analysis-value">{{ numberToLetter(question.rightOption) }}</span>
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
                     :class="{ 'question-active': clickedItem === question.id }">
                  <div class="question-header">
                    <div class="question-title">{{ question.sort + 1 }}. {{ question.title }}</div>
                    <el-tag :type="getQuestionStatus(question)" 
                           size="small" 
                           class="score-tag">
                      {{ getQuestionStatusText(question) }}
                    </el-tag>
                    <span class="score-badge">得分: {{ question.userScore }}/{{ question.score }}</span>
                  </div>
                  <div v-if="question.image" class="question-image">
                    <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
                  </div>
                  <el-input
                    v-model="question.myOption"
                    class="answer-textarea"
                    type="textarea"
                    :autosize="{ minRows: 3, maxRows: 6 }"
                    placeholder=""
                    :disabled="true"
                  />
                  <!-- 答案对比区域 -->
                  <div class="qu_analysis">
                    <el-card class="analysis-card">
                      <div class="analysis-item">
                        <span class="analysis-label">我的回答：</span>
                        <div class="analysis-content">{{ question.myOption || '未作答' }}</div>
                      </div>
                      <div class="analysis-item">
                        <span class="analysis-label">标准答案：</span>
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
import { recordExamDetail } from "@/api/record";
import { numberToLetter, hasQuestions, groupQuestionsByType } from '@/utils/questionFormat';

export default {
  name: 'ExamRecordReview',
  data() {
    return {
      input: "",
      quIndex: -1,
      examId: 0,
      data: null,
      userId: null,
      clickedItem: null,
      // 考试信息
      userScore: 0,
      totalScore: 0,
      userTime: 0,
      // 题目分组
      questionGroups: {
        radioList: [],
        multiList: [],
        judgeList: [],
        saqList: []
      }
    };
  },
  created() {
    // 从query参数中获取userId（教师查看学生记录时）
    if (this.$route.query?.userId) {
      this.userId = parseInt(this.$route.query.userId);
    } 
    // 从localStorage中获取userId（兼容旧版本）
    else if (localStorage.getItem('record_exam_userId')) {
      this.userId = parseInt(localStorage.getItem('record_exam_userId'));
    }
    // 从query参数中的data对象获取userId（兼容旧版本）
    else if (this.$route.query?.data?.type === 1) {
      this.userId = parseInt(this.$route.query.data.userId);
    }
    
    this.examId = parseInt(localStorage.getItem("record_exam_examId")) || 0;
    console.log('设置的 examId:', this.examId);
    console.log('设置的 userId:', this.userId);
    console.log('userId 类型:', typeof this.userId);
    this.ExamDetail();
  },
  methods: {
    // 获取考试详情
    async ExamDetail() {
      const params = { examId: this.examId, userId: this.userId };
      try {
        const res = await recordExamDetail(params);
        console.log('后端返回数据:', res);
        if (res.code && res.data) {
          // 保存后端返回的数据
          this.data = res;
          // 后端返回的是包含用时信息的对象
          const questions = res.data.questions || res.data;
          
          // 处理题目数据，添加必要字段
          const processedQuestions = this.processQuestions(questions);
          
          // 计算总分和得分
          this.calculateScores(processedQuestions);
          
          // 分组题目
          this.groupQuestions(processedQuestions);
          
          // 从后端数据获取考试用时（秒）
          if (res.data && res.data.userTime) {
            this.userTime = res.data.userTime;
          } else if (res.userTime) {
            this.userTime = res.userTime;
          }
        }
      } catch (error) {
        console.error('获取考试详情失败:', error);
      }
    },
    
    // 计算总分和得分
    calculateScores(questions) {
      // 正确计算：总分是所有题目的分数之和，得分是所有题目的实际得分之和
      this.totalScore = questions.reduce((sum, question) => sum + (question.score || 0), 0);
      this.userScore = questions.reduce((sum, question) => sum + (question.userScore || 0), 0);
    },
    
    // 格式化用户用时为 xx分xx秒
    formatUserTime(seconds) {
      if (!seconds || seconds < 0) return '0分0秒';
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = seconds % 60;
      return `${minutes}分${remainingSeconds}秒`;
    },
    
    // 处理题目数据，添加必要字段
    processQuestions(questions) {
      return questions.map((question, index) => {
        // 确保score字段有值（题目设置分数）
        const questionScore = question.score || question.totalScore || 1;
        // 确保userScore字段有值（实际得分）
        let actualScore = question.userScore || question.correctScore || 0;
        
        // 对于简答题，根据得分判断isRight
        if (question.quType === 4) {
          question.isRight = actualScore >= questionScore ? 1 : 0;
        }
        
        return {
          ...question,
          id: index + 1, // 添加id字段用于定位
          sort: index, // 添加sort字段用于显示序号
          score: questionScore, // 题目设置分数
          userScore: actualScore, // 实际得分
          // 确保myOption是数字类型（对于单选题和判断题）
          myOption: question.quType !== 2 && question.myOption !== undefined && question.myOption !== null 
            ? (typeof question.myOption === 'string' && !isNaN(parseInt(question.myOption)) 
                ? parseInt(question.myOption) 
                : question.myOption)
            : question.myOption
        };
      });
    },
    
    // 分组题目
    groupQuestions(questions) {
      this.questionGroups = groupQuestionsByType(questions);
    },

    hasQuestions,
    
    // 获取选中选项的内容（用于单选和判断）
    getSelectedOptionContent(question) {
      if (!question || !question.option || question.myOption === undefined || question.myOption === null) return '';
      
      const optionIndex = parseInt(question.myOption);
      if (isNaN(optionIndex) || optionIndex < 0 || optionIndex >= question.option.length) {
        return '';
      }
      
      return question.option[optionIndex].content;
    },
    
    // 获取选中的多选题选项内容数组
    getSelectedMultiOptionContent(question) {
      if (!question || !question.option || !question.myOption) return [];
      
      const selectedIndices = question.myOption.split(',').map(num => parseInt(num));
      const selectedContents = [];
      
      selectedIndices.forEach(index => {
        if (!isNaN(index) && index >= 0 && index < question.option.length) {
          selectedContents.push(question.option[index].content);
        }
      });
      
      return selectedContents;
    },
    
    // 获取题目状态
    getQuestionStatus(question) {
      // 对于未作答的题目
      if ((question.myOption === undefined || question.myOption === null || question.myOption === '') && question.quType !== 4) {
        return 'info'; // 未作答
      }
      
      // 对于简答题的特殊处理
      if (question.quType === 4) {
        // 有回答但未评分
        if ((question.myOption !== undefined && question.myOption !== null && question.myOption !== '') && (question.userScore === undefined || question.userScore === null)) {
          return 'info'; // 未评分
        }
        // 评分等于或超过题目分数
        if (question.userScore >= question.score) {
          return 'success'; // 正确
        }
        // 评分低于题目分数
        return 'danger'; // 错误
      }
      
      // 对于其他题型
      if (question.isRight === 1) {
        return 'success'; // 正确
      }
      return (question.myOption !== undefined && question.myOption !== null && question.myOption !== '') ? 'danger' : 'info'; // 错误或未作答
    },
    
    // 获取题目状态文本
    getQuestionStatusText(question) {
      // 对于未作答的题目
      if ((question.myOption === undefined || question.myOption === null || question.myOption === '') && question.quType !== 4) {
        return '○ 未答';
      }
      
      // 对于简答题的特殊处理
      if (question.quType === 4) {
        // 有回答但未评分
        if ((question.myOption !== undefined && question.myOption !== null && question.myOption !== '') && (question.userScore === undefined || question.userScore === null)) {
          return '○ 未评分';
        }
        // 评分等于或超过题目分数
        if (question.userScore >= question.score) {
          return '✓ 正确';
        }
        // 评分低于题目分数
        return '✗ 错误';
      }
      
      // 对于其他题型
      if (question.isRight === 1) {
        return '✓ 正确';
      }
      return (question.myOption !== undefined && question.myOption !== null && question.myOption !== '') ? '✗ 错误' : '○ 未答';
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
      }
    },
    
    numberToLetter,

    
    // 格式化多选题选项
    formatMultiOption(option) {
      if (!option) return '未作答';
      const options = option.split(',').map(num => this.numberToLetter(parseInt(num)));
      return options.join(', ');
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

.score-tag {
  margin-right: 10px;
}

.score-badge {
  font-size: 14px;
  color: #909399;
  background-color: #f4f4f5;
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
    display: flex;
    align-items: flex-start;
    
    .option-label {
      margin-right: 8px;
      font-weight: 500;
      color: #606266;
    }
    
    .option-content {
      flex: 1;
    }
  }
}

// 增强选中状态的视觉效果

// 用户选中的错误答案样式
:deep(.is-user-selected:not(.is-correct-selected)) {
  .el-radio__label,
  .el-checkbox__label {
    .option-label, .option-content {
      font-weight: bold !important;
      color: #FF4040 !important;
    }
  }
}

// 用户选中的正确答案样式（优先级最高）
:deep(.is-correct-selected) {
  .el-radio__label,
  .el-checkbox__label {
    .option-label, .option-content {
      font-weight: bold !important;
      color: #409EFF !important;
    }
  }
}

// 确保选中状态的视觉效果覆盖默认样式
:deep(.el-radio.is-checked),
:deep(.el-checkbox.is-checked) {
  .el-radio__label,
  .el-checkbox__label {
    transition: all 0.2s ease;
  }
}

.answer-textarea {
  margin-top: 10px;
  width: 100%;
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
</style>