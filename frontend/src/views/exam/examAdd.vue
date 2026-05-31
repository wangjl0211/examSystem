<template>
  <div class="app-container">
    <h3>组卷信息</h3>
    <el-tabs type="border-card" @tab-click="handleClick" v-model="activeName">
      <el-tab-pane label="自己选题" name="first">
        <ChooseQuestion
          @selected-change="handleSelectedChange"
          ref="questionSelector"
        ></ChooseQuestion>
      </el-tab-pane>
      <el-tab-pane label="随机抽题" name="second">

        <div>
          <div style="margin-top: 15px">
            <repo-select
              v-model="repoList[0].repoId"
              :multi="false"
              :excludes="excludes"
              @change="repoChange($event, repoList[0])"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <h3>考试配置</h3>
    <el-card style="margin-top: 20px">
      <el-form
        ref="postForm"
        :model="postForm"
        :rules="rules"
        label-position="left"
        label-width="120px"
      >
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="postForm.title"/>
        </el-form-item>

        <!-- 自己选题模式下的题型分数设置 -->
        <template v-if="activeName === 'first'">
          <el-form-item v-if="repoList[0].radioCount > 0" label="单选题分数" required>
            <el-input-number v-model="repoList[0].radioScore" :min="0" style="width: 120px" />
          </el-form-item>
          <el-form-item v-if="repoList[0].multiCount > 0" label="多选题分数" required>
            <el-input-number v-model="repoList[0].multiScore" :min="0" style="width: 120px" />
          </el-form-item>
          <el-form-item v-if="repoList[0].judgeCount > 0" label="判断题分数" required>
            <el-input-number v-model="repoList[0].judgeScore" :min="0" style="width: 120px" />
          </el-form-item>
          <el-form-item v-if="repoList[0].saqCount > 0" label="简答题分数" required>
            <el-input-number v-model="repoList[0].saqScore" :min="0" style="width: 120px" />
          </el-form-item>
        </template>

        <!-- 随机抽题模式下的题型配置 -->
        <template v-else-if="activeName === 'second'">
          <el-form-item label="单选题配置">
            <div style="display: flex; gap: 20px">
              <div>
                <span>数量：</span>
                <el-input-number
                  v-model="repoList[0].radioCount"
                  :min="0"
                  :max="repoList[0].totalRadio"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="repoList[0].totalRadio != undefined">
                  / {{ repoList[0].totalRadio }}
                </template>
              </div>
              <div>
                <span>分数：</span>
                <el-input-number
                  v-model="repoList[0].radioScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </div>
            </div>
          </el-form-item>
          <el-form-item label="多选题配置">
            <div style="display: flex; gap: 20px">
              <div>
                <span>数量：</span>
                <el-input-number
                  v-model="repoList[0].multiCount"
                  :min="0"
                  :max="repoList[0].totalMulti"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="repoList[0].totalMulti != undefined">
                  / {{ repoList[0].totalMulti }}
                </template>
              </div>
              <div>
                <span>分数：</span>
                <el-input-number
                  v-model="repoList[0].multiScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </div>
            </div>
          </el-form-item>
          <el-form-item label="判断题配置">
            <div style="display: flex; gap: 20px">
              <div>
                <span>数量：</span>
                <el-input-number
                  v-model="repoList[0].judgeCount"
                  :min="0"
                  :max="repoList[0].totalJudge"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="repoList[0].totalJudge != undefined">
                  / {{ repoList[0].totalJudge }}
                </template>
              </div>
              <div>
                <span>分数：</span>
                <el-input-number
                  v-model="repoList[0].judgeScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </div>
            </div>
          </el-form-item>
          <el-form-item label="简答题配置">
            <div style="display: flex; gap: 20px">
              <div>
                <span>数量：</span>
                <el-input-number
                  v-model="repoList[0].saqCount"
                  :min="0"
                  :max="repoList[0].totalSaq"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="repoList[0].totalSaq != undefined">
                  / {{ repoList[0].totalSaq }}
                </template>
              </div>
              <div>
                <span>分数：</span>
                <el-input-number
                  v-model="repoList[0].saqScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </div>
            </div>
          </el-form-item>
        </template>

        <el-form-item label="总分数" prop="totalScore">
          <el-input-number v-model="postForm.totalScore" :disabled="true" style="width: 120px" />
        </el-form-item>

        <el-form-item label="及格分" prop="passedScore">
          <el-input-number
            v-model="postForm.passedScore"
            :max="postForm.totalScore"
          />
        </el-form-item>

        <el-form-item label="最多切屏次数" prop="maxCount">
          <el-input-number v-model="postForm.maxCount" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="examDuration">
          <el-input-number v-model="postForm.examDuration" />
        </el-form-item>
        <el-form-item label="考试时间范围" prop="start">
          <el-date-picker
            v-model="postForm.start"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>


      </el-form>
    </el-card>

    <h3>权限配置</h3>
    <el-card style="margin-top: 20px">
      <div style="display: flex">
        <div style="margin-left: 10px">
          <el-form
            ref="postForm"
            :model="postForm"
            :rules="rules"
            label-position="left"
            label-width="120px"
          >
            <el-form-item label="考试课程" prop="classIds">
              <div class="custom-select-container">
                <!-- 自定义选择框 -->
        <div class="custom-select-input" @click="handleSelectClick">
          <div v-if="postForm.classIds.length === 0" class="placeholder">请选择课程</div>
          <div v-else class="selected-courses">
            <div v-for="courseId in postForm.classIds" :key="courseId" class="course-item" @click.stop>
              {{ getCourseNameById(courseId) }}
            </div>
          </div>
          <i class="el-select__caret el-input__icon el-icon-arrow-up"></i>
        </div>
                <!-- 隐藏的实际选择框 -->
        <el-select
          ref="courseSelect"
          v-model="postForm.classIds"
          multiple
          filterable
          remote
          :remote-method="fetchTeacherCourses"
          reserve-keyword
          clearable
          placeholder="请选择课程"
          style="width: 100%; position: absolute; opacity: 0; pointer-events: none;"
          popper-class="course-select-dropdown"
          popper-append-to-body
          @change="handleCourseChange"
          @visible-change="handleMenuVisibleChange"
        >
                  <!-- 全选选项 -->
                  <el-option
                    v-if="teacherCourses.length > 0"
                    key="all"
                    :value="'all'"
                    :label="'全选'"
                    class="all-select-option"
                  >
                    <div style="display: flex; align-items: center; justify-content: space-between;" @click.stop>
                      <span>全选</span>
                      <el-checkbox v-model="isAllSelected" @change="handleAllSelectChange" style="margin-left: 10px;"></el-checkbox>
                    </div>
                  </el-option>
                  <!-- 课程选项 -->
                  <el-option
                    v-for="course in teacherCourses"
                    :key="course.id"
                    :value="course.id"
                    :label="course.subjectName"
                    class="course-option"
                  >
                    <div style="display: flex; align-items: center; justify-content: space-between;" @click.stop>
                      <span>{{ course.subjectName }}</span>
                      <el-checkbox v-model="courseSelection[course.id]" @change="(val) => handleCourseCheckboxChange(course.id, val)" style="margin-left: 10px;"></el-checkbox>
                    </div>
                  </el-option>
                </el-select>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <div style="margin-top: 20px">
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script>
import RepoSelect from "@/components/RepoSelect";
import { saveData } from "@/api/exam";
import { fetchClasses } from "@/api/class_";
import ChooseQuestion from "@/components/ExamComponents/ChooseQuestion";
export default {
  name: "ExamDetail",

  components: {
    RepoSelect,
    ChooseQuestion,
    // CertificateSelect,
  },
  data() {
    return {
      activeName: "first",
      input: "",
      treeData: [],
      defaultProps: {
        label: "deptName",
      },
      filterText: "",
      treeLoading: false,
      // dateValues: [],
      // 题库
      repoList: [
        {
          addQuType: "0",
          queIds: "",
          id: "",
          rowId: 0,
          radioCount: 0,
          radioScore: 0,
          multiCount: 0,
          multiScore: 0,
          judgeCount: 0,
          judgeScore: 0,
          saqCount: 0,
          saqScore: 0,
        },
      ],
      // 已选择的题库
      excludes: [],
      postForm: {
        start: [],
        // 总分数
        totalScore: 0,
        // 题库列表
        repoList: [],
        // 开放类型
        openType: 1,
        // 考试课程列表
        departIds: [],
        // 初始化课程列表
        classIds: [],
      },
      // 课程选择相关
      teacherCourses: [],
      isAllSelected: false,
      courseSelection: {},
      rules: {
        title: [{ required: true, message: "考试名称不能为空！" }],

        // content: [{ required: true, message: '考试描述不能为空！' }],

        open: [{ required: true, message: "考试权限不能为空！" }],

        totalScore: [{ required: true, message: "考试分数不能为空！" }],

        passedScore: [{ required: true, message: "及格分不能为空！" }],

        examDuration: [{ required: true, message: "考试时间不能为空！" }],

        start: [{ required: true, message: "考试时间范围不能为空！" }],

        maxCount: [{ required: false, message: "最多切屏次数" }],
        classIds: [
          {
            required: true,
            message: "请选择考试课程！",
            type: "array",
            min: 1,
          },
        ],
      },
    };
  },

  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },

    // dateValues: {
    //   handler() {
    //     this.postForm.startTime = this.dateValues[0];
    //     this.postForm.endTime = this.dateValues[1];
    //   },
    // },

    // 题库变换
    repoList: {
      handler(val) {
        this.calculateTotalScore();
        this.postForm.repoList = val;
        this.$forceUpdate();
      },
      deep: true,
    },
    // 标签切换时重新计算总分数
    activeName: {
      handler() {
        this.calculateTotalScore();
      }
    },
  },
  methods: {
    // 点击tab
    handleClick(tab, event) {
      this.$refs.questionSelector.clearSelection();
      // console.log(event)
      this.repoList[0].addQuType = tab.index;
      this.repoList[0].queIds = "";
      this.repoList[0].id = "";
      this.repoList[0].rowId = 0;
      this.repoList[0].radioCount = 0;
      this.repoList[0].radioScore = 0;
      this.repoList[0].multiCount = 0;
      this.repoList[0].judgeCount = 0;
      this.repoList[0].judgeScore = 0;
      this.repoList[0].saqCount = 0;
      this.repoList[0].saqScore = 0;
      console.log(tab, event);
    },
    // 子组件选择的ids
    handleSelectedChange(selectedIds) {
      const ids = [];
      selectedIds.selectedRows.forEach((item) => {
        ids.push(item.id);
      });

      this.repoList[0].queIds = ids.join(",");
      this.repoList[0].radioCount = selectedIds.questionList.radioCount;
      this.repoList[0].radioScore = selectedIds.questionList.radioScore;
      this.repoList[0].multiCount = selectedIds.questionList.multiCount;
      this.repoList[0].multiScore = selectedIds.questionList.multiScore;
      this.repoList[0].judgeCount = selectedIds.questionList.judgeCount;
      this.repoList[0].judgeScore = selectedIds.questionList.judgeScore;
      this.repoList[0].saqCount = selectedIds.questionList.saqCount;
      this.repoList[0].saqScore = selectedIds.questionList.saqScore;
      console.log("从子组件接收到的选中ID:", this.repoList);
      // 在这里你可以将选中的ID保存到父组件的数据中
      this.selectedQuestionIds = selectedIds;
      // 或者执行其他需要的操作
    },
    // 验证课程选择
    validateCourseSelection() {
      if (!this.postForm.classIds || this.postForm.classIds.length === 0) {
        this.$notify({
          title: "提示信息",
          message: "请选择考试课程！",
          type: "warning",
          duration: 2000,
        });
        return false;
      }
      return true;
    },

    // 验证题库配置
    validateRepoConfig(repo, index) {
      if (!repo.repoId) {
        this.$notify({
          title: "提示信息",
          message: "考试题库选择不正确！",
          type: "warning",
          duration: 2000,
        });
        return false;
      }

      if ((repo.radioCount > 0 && repo.radioScore === 0) ||
          (repo.radioCount === 0 && repo.radioScore > 0)) {
        this.$notify({
          title: "提示信息",
          message: `题库第：[${index + 1}]项存在无效的单选题配置！`,
          type: "warning",
          duration: 2000,
        });
        return false;
      }

      if ((repo.multiCount > 0 && repo.multiScore === 0) ||
          (repo.multiCount === 0 && repo.multiScore > 0)) {
        this.$notify({
          title: "提示信息",
          message: `题库第：[${index + 1}]项存在无效的多选题配置！`,
          type: "warning",
          duration: 2000,
        });
        return false;
      }

      if ((repo.judgeCount > 0 && repo.judgeScore === 0) ||
          (repo.judgeCount === 0 && repo.judgeScore > 0)) {
        this.$notify({
          title: "提示信息",
          message: `题库第：[${index + 1}]项存在无效的判断题配置！`,
          type: "warning",
          duration: 2000,
        });
        return false;
      }

      if ((repo.saqCount > 0 && repo.saqScore === 0) ||
          (repo.saqCount === 0 && repo.saqScore > 0)) {
        this.$notify({
          title: "提示信息",
          message: `题库第：[${index + 1}]项存在无效的简答题配置！`,
          type: "warning",
          duration: 2000,
        });
        return false;
      }

      return true;
    },

    // 显示确认对话框并提交
    confirmAndSubmit() {
      this.$confirm("确实要提交保存吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.submitForm();
      });
    },

    handleSave() {
      // 验证课程是否选择
      if (!this.validateCourseSelection()) {
        return;
      }
      
      if (this.repoList[0].addQuType === "1") {
        this.$refs.postForm.validate((valid) => {
          if (!valid) {
            return;
          }
          if (this.postForm.totalScore === 0) {
            this.$notify({
              title: "提示信息",
              message: "考试规则设置不正确，请确认！",
              type: "warning",
              duration: 2000,
            });
            return;
          }

          // 验证所有题库配置
          for (let i = 0; i < this.postForm.repoList.length; i++) {
            if (!this.validateRepoConfig(this.postForm.repoList[i], i)) {
              return;
            }
          }

          this.confirmAndSubmit();
        });
      }
      if (this.repoList[0].addQuType === "0") {
        this.confirmAndSubmit();
      }
    },

    handleCheckChange() {
      const that = this;
      // 置空
      this.postForm.departIds = [];
      const nodes = this.$refs.tree.getCheckedNodes();
      nodes.forEach(function (item) {
        that.postForm.departIds.push(item.id);
      });
    },

    // 添加子项
    handleAdd() {
      this.repoList.push();
    },

    removeItem(index) {
      this.repoList.splice(index, 1);
    },
    formatDateToISOString(date) {
      if (!(date instanceof Date)) {
        return null;
      }

      // 获取本地时间的各部分（不进行时区转换）
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");

      // 格式化为本地时间字符串（不含时区信息）
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
      // 输出示例："2026-05-14 09:00:00"（保留用户选择的本地时间）
    },

    submitForm() {
      console.log("postForm", this.postForm);
      // 校验和处理数据
      this.postForm.repoList = this.repoList;

      const params = {
        title: this.postForm.title,
        // content: this.postForm.content, // 添加考试描述字段
        examDuration: this.postForm.examDuration,
        maxCount: this.postForm.maxCount,
        passedScore: this.postForm.passedScore,
        startTime: this.formatDateToISOString(this.postForm.start[0]),
        endTime: this.formatDateToISOString(this.postForm.start[1]),
        subjectIds: this.postForm.classIds.join(","),
        repoId: this.postForm.repoList[0].repoId,
        addQuype: this.postForm.repoList[0].addQuType,
        quIds: this.postForm.repoList[0].queIds,
        radioCount: this.postForm.repoList[0].radioCount,
        radioScore: this.postForm.repoList[0].radioScore,
        multiCount: this.postForm.repoList[0].multiCount,
        multiScore: this.postForm.repoList[0].multiScore,
        judgeCount: this.postForm.repoList[0].judgeCount,
        judgeScore: this.postForm.repoList[0].judgeScore,
        saqCount: this.postForm.repoList[0].saqCount,
        saqScore: this.postForm.repoList[0].saqScore,
      };
      saveData(params).then((res) => {
        if (res.code) {
          this.$notify({
            title: "成功",
            message: "考试保存成功！",
            type: "success",
            duration: 2000,
          });

          this.$router.push({ name: "exam-management" });
        } else {
          this.$notify({
            title: "失败",
            message: res.msg,
            type: "error",
            duration: 2000,
          });
        }
      });
    },

    filterNode(value, data) {
      if (!value) return true;
      return data.deptName.indexOf(value) != -1;
    },

    onClassChange() {},
    // 处理选择框点击事件
    handleSelectClick() {
      // 直接切换菜单状态
      if (this.$refs.courseSelect) {
        this.$refs.courseSelect.toggleMenu()
      }
    },
    // 处理菜单显示/隐藏状态变化
    handleMenuVisibleChange() {
      // 可以在这里添加菜单状态变化的处理逻辑
      // 例如更新图标方向等
    },
    // 根据课程ID获取课程名称
    getCourseNameById(courseId) {
      const course = this.teacherCourses.find(course => course.id === courseId)
      return course ? course.subjectName : ''
    },
    // 获取教师课程列表
    fetchTeacherCourses(query = '') {
      fetchClasses({ pageNum: 1, pageSize: 1000, name: query }).then((response) => {
        this.teacherCourses = response.data || []
      })
    },
    // 处理课程选择变化
    handleCourseChange(val) {
      if (val.includes('all')) {
        // 移除全选标识
        const filteredVal = val.filter(item => item !== 'all')
        
        // 检查当前是否已经全选
        const allCourseIds = this.teacherCourses.map(course => course.id)
        const isAllSelected = allCourseIds.every(id => filteredVal.includes(id))
        
        if (isAllSelected) {
          // 如果已经全选，则取消全选
          this.postForm.classIds = []
          this.isAllSelected = false
          // 明确将每个课程的状态设置为false，确保UI正确更新
          this.courseSelection = {}
          this.teacherCourses.forEach(course => {
            this.courseSelection[course.id] = false
          })
        } else {
          // 如果没有全选，则选择所有课程
          this.postForm.classIds = allCourseIds
          this.isAllSelected = true
          // 更新courseSelection对象
          this.courseSelection = {}
          allCourseIds.forEach(id => {
            this.courseSelection[id] = true
          })
        }
      } else {
        // 更新全选状态
        this.isAllSelected = val.length === this.teacherCourses.length
        // 更新courseSelection对象
        this.courseSelection = {}
        val.forEach(id => {
          this.courseSelection[id] = true
        })
      }
      // 手动触发表单验证更新
      if (this.$refs.postForm) {
        this.$refs.postForm.validateField('classIds')
      }
    },
    // 处理全选变化
    handleAllSelectChange(val) {
      // 阻止事件冒泡，避免与el-select的默认行为冲突
      if (event) {
        event.stopPropagation()
      }
      
      if (val) {
        // 全选
        const allCourseIds = this.teacherCourses.map(course => course.id)
        this.postForm.classIds = allCourseIds
        this.isAllSelected = true
        // 更新courseSelection对象
        this.courseSelection = {}
        allCourseIds.forEach(id => {
          this.courseSelection[id] = true
        })
      } else {
        // 取消全选
        this.postForm.classIds = []
        this.isAllSelected = false
        // 明确将每个课程的状态设置为false，确保UI正确更新
        this.courseSelection = {}
        this.teacherCourses.forEach(course => {
          this.courseSelection[course.id] = false
        })
      }
      // 手动触发表单验证更新
      if (this.$refs.postForm) {
        this.$refs.postForm.validateField('classIds')
      }
    },
    // 处理课程勾选框变化
    handleCourseCheckboxChange(courseId, val) {
      try {
        // 阻止事件冒泡，避免与el-select的默认行为冲突
        if (event) {
          event.stopPropagation()
        }
        
        if (val) {
          // 选中课程
          if (!this.postForm.classIds.includes(courseId)) {
            this.postForm.classIds.push(courseId)
          }
          this.courseSelection[courseId] = true
        } else {
          // 取消选中课程
          const index = this.postForm.classIds.indexOf(courseId)
          if (index > -1) {
            this.postForm.classIds.splice(index, 1)
          }
          this.courseSelection[courseId] = false
        }
        // 更新全选状态
        this.isAllSelected = this.postForm.classIds.length === this.teacherCourses.length
        // 手动触发表单验证更新
        if (this.$refs.postForm) {
          this.$refs.postForm.validateField('classIds')
        }
      } catch (error) {
        console.error('处理课程选择失败:', error)
      }
    },
    // 计算总分数
    calculateTotalScore() {
      let totalScore = 0;
      this.excludes = [];
      for (const item of this.repoList) {
        if (item.radioCount > 0 && item.radioScore > 0) {
          totalScore += item.radioCount * item.radioScore;
        }

        if (item.multiCount > 0 && item.multiScore > 0) {
          totalScore += item.multiCount * item.multiScore;
        }

        if (item.judgeCount > 0 && item.judgeScore > 0) {
          totalScore += item.judgeCount * item.judgeScore;
        }
        if (item.saqCount > 0 && item.saqScore > 0) {
          totalScore += item.saqCount * item.saqScore;
        }
        if (item.id) {
          this.excludes.push(item.id);
        }
      }

      // 赋值
      this.postForm.totalScore = totalScore;
    },
    repoChange(e, row) {
      // 赋值ID
      row.id = e.id;
      if (e != null) {
        row.totalRadio = e.radioNum;
        row.totalMulti = e.multiNum;
        row.totalJudge = e.judgeNum;
        row.totalSaq = e.saqNum;
      } else {
        row.totalRadio = 0;
        row.totalMulti = 0;
        row.totalJudge = 0;
        row.totalSaq = 0;
      }
    },
  },
};
</script>

<style scoped>
/* 自定义选择框样式 */
.custom-select-container {
  position: relative;
  width: 100%;
}

.custom-select-input {
  position: relative;
  width: 100%;
  min-height: 36px;
  padding: 6px 30px 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.custom-select-input:hover {
  border-color: #c0c4cc;
}

.custom-select-input:focus-within {
  border-color: #409eff;
  outline: 0;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.placeholder {
  color: #909399;
  line-height: 24px;
}

.selected-courses {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.course-item {
  line-height: 24px;
  padding: 2px 8px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  color: #409eff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  box-sizing: border-box;
}

.custom-select-input i {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0c4cc;
  transition: transform 0.3s;
}

.custom-select-input:hover i {
  color: #909399;
}

/* 确保下拉菜单位置正确 */
.el-select-dropdown {
  z-index: 1001;
}
/* 课程选择下拉框样式 */
.course-select-dropdown {
  min-width: 300px;
}
/* 全选选项样式 */
.all-select-option {
  font-weight: bold;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 8px;
  margin-bottom: 8px;
}
/* 课程选项样式 */
.course-option {
  position: relative;
}
</style>
