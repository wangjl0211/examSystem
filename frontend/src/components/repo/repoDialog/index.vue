<template>
  <el-dialog :title="title" v-model="visible" width="30%">
    <el-row :gutter="20">
      <el-col>
        <el-form :model="repoForm">
          <el-form-item label="题库名称:">
            <el-input
              v-model="repoForm.repoTitle"
              autocomplete="off"
              style="width: 80%"
            />
          </el-form-item>
          <el-form-item label="是否开启刷题:">
            <el-switch
              v-model="repoForm.isExercise"
              active-color="#13ce66"
              inactive-color="#cccccc"
            />
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
</template>
<script>
export default {
  components: {},
  props: {
    title: {
      type: String,
      default: '提示'
    },
    modelValue: {
      type: Boolean,
      default: false
    },
    updateData: {
      type: Object,
      default() {
        return {
          title: '',
          isExercise: 0
        }
      }
    },
    onConfirm: {
      type: Function,
      default: () => {}
    }
  },
  data() {
    return {
      repoForm: {
        repoTitle: '',
        isExercise: false
      },
      visible: this.modelValue
    }
  },
  watch: {
    modelValue(val) {
      this.visible = val
    },
    updateData(val) {
      this.repoForm.repoTitle = val.title
      this.repoForm.isExercise = val.isExercise === 1;
    },
    visible(val) {
      this.$emit('update:modelValue', val)
    }
  },

  methods: {
    handleConfirm() {
      // 调用父组件传入的确认回调方法，并传递 repoForm 数据
      this.onConfirm(this.repoForm)
      // 关闭对话框
      this.visible = false
      this.repoForm = {
        repoTitle: '',
        isExercise: false
      }
    }
  }
}
</script>
<style></style>
