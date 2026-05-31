<template>
  <el-dialog
    top="2vh"
    title="提交预览"
    v-model="dialogVisible"
    :width="isMobile ? '95%' : '80%'"
    :before-close="handleClose"
  >
    <el-container style="height: 70vh; border: 1px solid #eee">
      <el-container>
        <el-main class="right">
          <el-col>
            <el-card class="qu_list">
              <div>
                <!-- 所有题目部分 -->
                <template v-for="(item, index) in processedRecordData">
                  <!-- 客观题 -->
                  <div
                    v-if="item.quType === 1 || item.quType === 2 || item.quType === 3"
                    :key="'obj-' + index"
                    :class="'index' + index"
                  >
                    <el-row :gutter="24">
                      <el-col :span="20" style="text-align: left">
                        <!-- 题干区域 -->
                        <div>
                          <div class="qu_content">
                            <span class="qu_num">{{ item.displayOrder || index + 1 }}. </span>
                            <el-tag 
                              size="small" 
                              :type="getQuestionTypeTag(item.quType)"
                              style="margin-right: 10px"
                            >
                              {{ getQuestionTypeName(item.quType) }}
                            </el-tag>
                            {{ item.title }}
                          </div>
                          <div v-if="item.image != null && item.image != ''">
                            <el-image :src="item.image" style="max-width: 200px;" />
                          </div>
                        </div>

                        <!-- 选项区域 -->
                        <template v-if="item.quType === 1">
                          <!-- 单选题使用单选框 -->
                          <el-radio-group 
                            class="qu_choose_group"
                            :model-value="getSelectedOptionId(item.myOption, item.options)"
                          >
                            <el-radio
                              v-for="(option, optionIndex) in item.options"
                              :key="'option-' + optionIndex"
                              :label="option.id"
                              class="qu_choose"
                              :class="{
                                'imgC': option.image != null && option.image != '',
                              }"
                            >
                              {{ numberToLetter(optionIndex) }}. {{ option.content }}
                              <div v-if="option.image != null && option.image != ''">
                                <el-image :src="option.image" style="max-width: 200px" class="qu_choose_tag_img" />
                              </div>
                            </el-radio>
                          </el-radio-group>
                        </template>
                        <template v-else-if="item.quType === 3">
                          <!-- 判断题使用单选框 -->
                          <el-radio-group 
                            class="qu_choose_group"
                            :model-value="getSelectedOptionId(item.myOption, item.options)"
                          >
                            <el-radio
                              v-for="(option, optionIndex) in item.options"
                              :key="'option-' + optionIndex"
                              :label="option.id"
                              class="qu_choose"
                              :class="{
                                'imgC': option.image != null && option.image != '',
                              }"
                            >
                              {{ numberToLetter(optionIndex) }}. {{ optionIndex === 0 ? '正确' : '错误' }}
                              <div v-if="option.image != null && option.image != ''">
                                <el-image :src="option.image" style="max-width: 200px" class="qu_choose_tag_img" />
                              </div>
                            </el-radio>
                          </el-radio-group>
                        </template>
                        <template v-else-if="item.quType === 2">
                          <!-- 多选题使用复选框 -->
                          <el-checkbox-group 
                            class="qu_choose_group"
                            :model-value="getSelectedOptionIds(item.myOption)"
                          >
                            <el-checkbox
                              v-for="(option, optionIndex) in item.options"
                              :key="'option-' + optionIndex"
                              :label="option.id"
                              class="qu_choose"
                              :class="{
                                'imgC': option.image != null && option.image != '',
                              }"
                            >
                              {{ numberToLetter(optionIndex) }}. {{ option.content }}
                              <div v-if="option.image != null && option.image != ''">
                                <el-image :src="option.image" style="max-width: 200px" class="qu_choose_tag_img" />
                              </div>
                            </el-checkbox>
                          </el-checkbox-group>
                        </template>

                        <!-- 我的答案区域 -->
                        <div class="qu_analysis">
                          <el-card>
                            <div>
                              <span>我的答案：</span>
                              <span
                                :style="{
                                  color: getAnswerColor(item.isRight)
                                }"
                              >
                                {{ item.myOptionDisplay || item.myOption || '未作答' }}
                              </span>
                            </div>
                          </el-card>
                        </div>
                      </el-col>
                    </el-row>
                    <el-divider />
                  </div>
                  
                  <!-- 主观题 -->
                  <div
                    v-else-if="item.quType === 4"
                    :key="'subj-' + index"
                    :class="'index' + index"
                  >
                    <el-row :gutter="24">
                      <el-col :span="20" style="text-align: left">
                        <!-- 题干部分 -->
                        <div>
                          <div class="qu_content">
                            <span class="qu_num">{{ item.displayOrder || index + 1 }}. </span>
                            <el-tag 
                              size="small" 
                              :type="getQuestionTypeTag(item.quType)"
                              style="margin-right: 10px"
                            >
                              {{ getQuestionTypeName(item.quType) }}
                            </el-tag>
                            {{ item.title }}
                          </div>
                        </div>

                        <!-- 简答题内容区域 -->
                        <el-radio-group class="qu_choose_group">
                          <el-input
                            v-model="item.myOption"
                            style="margin-top: 10px"
                            type="textarea"
                            :autosize="{ minRows: 2, maxRows: 4 }"
                            placeholder="请输入内容"
                            readonly
                          />
                        </el-radio-group>
                      </el-col>
                    </el-row>
                    <el-divider />
                  </div>
                </template>
              </div>
              <el-divider />
            </el-card>
          </el-col>
        </el-main>
      </el-container>
    </el-container>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="onCancel">取 消</el-button>
        <el-button type="primary" @click="onConfirm">确 定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ElRadio, ElRadioGroup, ElCheckbox, ElCheckboxGroup } from 'element-plus'
import { numberToLetter as numberToLetterUtil } from '@/utils/questionFormat'

export default {
  name: 'ExamSummaryDialog',
  components: {
    ElRadio,
    ElRadioGroup,
    ElCheckbox,
    ElCheckboxGroup
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    recordData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      isMobile: window.innerWidth < 768
    }
  },
  mounted() {
    this._resizeHandler = () => {
      this.isMobile = window.innerWidth < 768
    }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this._resizeHandler)
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    // 统一处理选项数据，确保所有题目都有标准的options属性
    processedRecordData() {
      return this.recordData.map(item => {
        return {
          ...item,
          options: item.option || item.options || []
        }
      })
    }
  },
  methods: {
    // 检查选项是否被选中
    isCheck(myOption, sort) {
      if (!myOption) return false
      
      // 确保 sort 是数字类型
      const sortNum = typeof sort === 'string' ? parseInt(sort, 10) : sort
      
      // 将 sortNum 转换为字母
      const sortLetter = this.numberToLetter(sortNum)
      
      // 将 myOption 转换为数组（支持逗号分隔的字母，如 "A,B"）
      let arr = []
      if (typeof myOption === 'string') {
        arr = myOption.split(',').map(item => item.trim())
      } else if (Array.isArray(myOption)) {
        arr = myOption
      }
      
      return arr.includes(sortLetter)
    },

    // 处理对话框关闭
    handleClose(done) {
      this.$emit('close')
      done()
    },

    // 取消按钮
    onCancel() {
      this.dialogVisible = false
    },

    // 确认按钮
    onConfirm() {
      this.$emit('confirm')
      this.dialogVisible = false
    },

    // 获取答案颜色
    getAnswerColor(isRight) {
      if (isRight === 1) return 'green'
      if (isRight === 0) return 'red'
      return 'gray'
    },

    // 获取题型名称
    getQuestionTypeName(quType) {
      const typeMap = {
        1: '单选题',
        2: '多选题',
        3: '判断题',
        4: '简答题'
      }
      return typeMap[quType] || '未知题型'
    },

    // 获取题型标签类型
    getQuestionTypeTag(quType) {
      const typeMap = {
        1: 'primary',
        2: 'success',
        3: 'warning',
        4: 'info'
      }
      return typeMap[quType] || 'default'
    },

    numberToLetter(input) {
      return numberToLetterUtil(input)
    },

    // 获取单选题和判断题的选中选项ID
    getSelectedOptionId(myOption, options) {
      if (!myOption) return ''
      
      // 如果 myOption 是数字，直接返回
      if (!isNaN(myOption)) {
        return parseInt(myOption)
      }
      
      // 如果 myOption 是字母，根据选项文本查找对应的ID
      if (typeof myOption === 'string' && /^[A-F]$/.test(myOption)) {
        const optionIndex = 'ABCDEF'.indexOf(myOption)
        if (optionIndex >= 0 && options && options[optionIndex]) {
          return options[optionIndex].id
        }
      }
      
      return ''
    },

    // 获取多选题的选中选项ID数组
    getSelectedOptionIds(myOption) {
      if (!myOption) return []
      
      // 如果是数组，直接返回
      if (Array.isArray(myOption)) {
        return myOption.map(id => parseInt(id))
      }
      
      // 如果是逗号分隔的字符串，转换为数组
      if (typeof myOption === 'string') {
        // 检查是否为逗号分隔的数字
        if (/^\d+(,\d+)*$/.test(myOption)) {
          return myOption.split(',').map(id => parseInt(id))
        }
        
        // 检查是否为逗号分隔的字母
        if (/^[A-F](,[A-F])*$/.test(myOption)) {
          return [] // 字母格式需要根据选项映射，这里暂时返回空数组
        }
      }
      
      return []
    }
  }
}
</script>
<style scoped>
.right {
  width: 100%;
  height: 100%;
}

.el-divider--horizontal {
  display: block;
  height: 1px;
  width: 95%;
  margin: 24px 0;
}

/* 试题内容样式 */
.qu_list {
  height: 100%;
  width: 100%;
  overflow: auto;
  page-break-after: always;
}

.qu_content {
  padding-left: 10px;
}

/* 题号样式 */
.qu_num {
  font-weight: bold;
  margin-right: 5px;
}

/* 选项组 */
.qu_choose_group {
  width: 100%;
}

/* 单个选项 */
.qu_choose {
  display: flex;
  align-items: center;
  margin: 10px;
}

/* 移除自定义单选按钮和复选框样式，使用Element Plus默认样式 */

.imgC {
  height: 150px;
}

.qu_choose_tag_img {
  height: auto;
  display: block;
  margin: 10px;
}

/* 试题解析 */
.qu_analysis {
  padding: 10px;
}

.qu_analysis_content {
  padding-top: 10px;
}
</style>