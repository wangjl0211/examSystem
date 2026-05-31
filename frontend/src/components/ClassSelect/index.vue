<template>
  <el-select
    v-model="selectedClassIds"
    :multiple="isMultiple"
    :collapse-tags="isMultiple"
    :collapse-tags-tooltip="isMultiple"
    :remote-method="fetchClasses"
    filterable
    remote
    reserve-keyword
    clearable
    :disabled="localIsEdit"
    automatic-dropdown
    placeholder="选择或搜索课程"
    no-data-text="暂无课程数据"
    class="class-selector"
    @change="handleClassChange"
  >
    <template #header v-if="isMultiple && classList.length > 0">
      <div class="select-header">
        <el-checkbox
          v-model="checkAll"
          :indeterminate="isIndeterminate"
          @change="handleCheckAllChange"
        >
          全选
        </el-checkbox>
      </div>
    </template>
    
    <el-option
      v-for="cls in classList"
      :key="cls.id"
      :label="cls.subjectName"
      :value="cls.id"
    >
      <div class="option-content">
        <span>{{ cls.subjectName }}</span>
        <el-checkbox 
          v-if="isMultiple"
          :model-value="selectedClassIds.includes(cls.id)"
          class="option-checkbox"
        />
      </div>
    </el-option>
  </el-select>
</template>

<script>
import { fetchClasses } from '@/api/class_'

export default {
  name: 'ClassSelect',
  props: {
    isMultiple: {
      type: Boolean,
      default: false
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    value: [String, Array],
    excludes: Array
  },
  data() {
    return {
      classList: [],
      selectedClassIds: this.isMultiple ? [] : '',
      localIsEdit: this.isEdit
    }
  },
  computed: {
    checkAll: {
      get() {
        return this.classList.length > 0 && this.selectedClassIds.length === this.classList.length
      },
      set() {
        // 在handleCheckAllChange中处理
      }
    },
    isIndeterminate() {
      return this.selectedClassIds.length > 0 && this.selectedClassIds.length < this.classList.length
    }
  },
  watch: {
    isEdit(val) {
      this.localIsEdit = val
    },
    value: {
      handler(newValue) {
        if (this.isMultiple) {
          this.selectedClassIds = Array.isArray(newValue) ? newValue : []
        } else {
          this.selectedClassIds = newValue
        }
      },
      immediate: true
    }
  },
  created() {
    this.fetchClasses()
  },
  methods: {
    fetchClasses(query = '') {
      fetchClasses({ pageNum: 1, pageSize: 1000, name: query }).then((response) => {
        this.classList = response.data || []
      })
    },
    handleClassChange(val) {
      const idsArray = Array.isArray(val) ? val : [val]
      const selectedClasses = this.classList.filter((cls) => idsArray.includes(cls.id))
      this.$emit('change', selectedClasses)
      this.$emit('input', val)
    },
    handleCheckAllChange(val) {
      const allIds = this.classList.map(item => item.id)
      this.selectedClassIds = val ? allIds : []
      this.handleClassChange(this.selectedClassIds)
    }
  }
}
</script>

<style scoped>
.select-header {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}
.option-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.option-checkbox {
  pointer-events: none; /* 防止点击复选框时阻止el-option的选中事件 */
}
</style>
