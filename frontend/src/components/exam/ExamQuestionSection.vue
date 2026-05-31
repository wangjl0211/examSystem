<template>
  <div v-if="hasQuestions(questions)" class="exam-question-section">
    <h3 class="exam-question-section__title">{{ title }}</h3>
    <div
      v-for="question in questions"
      :key="question.questionId"
      :id="'question-' + question.questionId"
      class="exam-question-section__item"
    >
      <div class="exam-question-section__header">
        <p class="exam-question-section__content">{{ question.sort + 1 }}. {{ question.content }}</p>
        <span class="exam-question-section__score">分值: {{ question.score || 0 }}</span>
      </div>
      <div v-if="question.image" class="exam-question-section__image">
        <el-image :src="question.image" :preview-src="[question.image]" style="max-width: 200px" />
      </div>

      <!-- 单选题 -->
      <el-radio-group
        v-if="quType === 1"
        :model-value="answers[question.questionId]"
        @update:model-value="emitAnswer(question, $event)"
      >
        <el-radio v-for="(option, index) in question.options" :key="option.id" :label="option.id">
          {{ numberToLetter(index) }}. {{ option.content }}
          <div v-if="option.image" class="exam-question-section__option-image">
            <el-image :src="option.image" :preview-src="[option.image]" style="max-width: 200px" />
          </div>
        </el-radio>
      </el-radio-group>

      <!-- 多选题 -->
      <el-checkbox-group
        v-else-if="quType === 2"
        :model-value="answers[question.questionId] || []"
        @update:model-value="emitAnswer(question, $event)"
      >
        <el-checkbox v-for="(option, index) in question.options" :key="option.id" :label="option.id">
          {{ numberToLetter(index) }}. {{ option.content }}
          <div v-if="option.image" class="exam-question-section__option-image">
            <el-image :src="option.image" :preview-src="[option.image]" style="max-width: 200px" />
          </div>
        </el-checkbox>
      </el-checkbox-group>

      <!-- 判断题 -->
      <el-radio-group
        v-else-if="quType === 3"
        :model-value="answers[question.questionId]"
        @update:model-value="emitAnswer(question, $event)"
      >
        <el-radio v-for="(option, index) in question.options" :key="option.id" :label="option.id">
          {{ numberToLetter(index) }}. {{ index === 0 ? '正确' : '错误' }}
          <div v-if="option.image" class="exam-question-section__option-image">
            <el-image :src="option.image" :preview-src="[option.image]" style="max-width: 200px" />
          </div>
        </el-radio>
      </el-radio-group>

      <!-- 简答题 -->
      <div v-else-if="quType === 4" class="exam-question-section__saq">
        <el-input
          :model-value="answers[question.questionId]"
          @update:model-value="emitAnswer(question, $event)"
          @input="(val) => $emit('input-change', val, question.questionId)"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 6 }"
          placeholder="请输入答案"
          style="width: 100%"
          :maxlength="500"
          show-word-limit
        />
      </div>
    </div>
  </div>
</template>

<script>
import { hasQuestions, numberToLetter } from '@/utils/questionFormat'

/**
 * 考试答题页 - 按题型渲染题目区块
 */
export default {
  name: 'ExamQuestionSection',
  props: {
    /** 区块标题，如「单选题」 */
    title: {
      type: String,
      required: true
    },
    /** 题目列表 */
    questions: {
      type: Array,
      default: () => []
    },
    /** 题型：1 单选 2 多选 3 判断 4 简答 */
    quType: {
      type: Number,
      required: true
    },
    /** 答案映射 questionId -> answer */
    answers: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['answer-change', 'input-change'],
  methods: {
    hasQuestions,
    numberToLetter,
    emitAnswer(question, answer) {
      this.$emit('answer-change', {
        questionId: question.questionId,
        answer,
        quType: this.quType,
        saveToServer: true
      })
    }
  }
}
</script>

<style scoped>
.exam-question-section {
  margin-bottom: 30px;
}
.exam-question-section__title {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e6e6e6;
}
.exam-question-section__item {
  margin-bottom: 25px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}
.exam-question-section__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}
.exam-question-section__content {
  margin: 0;
}
.exam-question-section__score {
  font-size: 14px;
  color: #666;
  background-color: #e6f7ff;
  padding: 2px 8px;
  border-radius: 10px;
}
.exam-question-section__image {
  margin-bottom: 15px;
}
.exam-question-section__option-image {
  clear: both;
  margin-top: 10px;
}
.exam-question-section__saq {
  margin-bottom: 8px;
}
</style>
