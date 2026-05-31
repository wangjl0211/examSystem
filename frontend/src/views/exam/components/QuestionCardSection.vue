<template>
  <div v-if="questions && questions.length > 0">
    <p class="card-title">{{ title }}</p>
    <el-row :gutter="24" class="card-line">
      <el-tag
        v-for="item in questions"
        :key="item.questionId"
        :type="getCardItemClass(item)"
        style="width: calc(100% / 8); text-align: center; margin: 2px; cursor: pointer; transition: all 0.2s ease;"
        :class="{
          'tag-clicked': clickedItem === item.questionId
        }"
        @click="selectQuestion(item)"
      >
        {{ item.sort + 1 }}
      </el-tag>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'QuestionCardSection',
  props: {
    title: {
      type: String,
      required: true
    },
    questions: {
      type: Array,
      required: true
    },
    answerStore: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      clickedItem: null
    }
  },
  computed: {
    // 使用计算属性来缓存和追踪 answerStore 的变化
    computedAnswerStore() {
      // 创建一个新的对象引用，确保当 answerStore 内部属性变化时，计算属性会触发更新
      return { ...this.answerStore }
    }
  },
  methods: {
    selectQuestion(item) {
      // 添加点击反馈
      this.clickedItem = item.questionId
      setTimeout(() => {
        this.clickedItem = null
      }, 200)
      
      // 触发事件，传递题目信息
      this.$emit('select-question', item)
    },
    getCardItemClass(item) {
      // 已答题
      if (this.isQuestionAnswered(item.questionId)) {
        return 'success'
      }
      // 未答题
      return 'info'
    },
    isQuestionAnswered(questionId) {
      // 确保 answerStore 和对应的属性存在
      return this.answerStore && this.answerStore[questionId] && this.answerStore[questionId].isAnswered
    }
  }
}
</script>

<style scoped>
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

.tag-clicked {
  transform: scale(0.95);
  opacity: 0.8;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .card-line span {
    width: calc(100% / 5);
  }
}

@media screen and (max-width: 480px) {
  .card-line span {
    width: calc(100% / 4);
  }
}
</style>
