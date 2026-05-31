<template>
  <div class="app-container">
    <!-- 数据卡片区域 -->
    <div class="stats-container">
      <div class="stats-row">
        <!-- 课程总数卡片 -->
        <div class="stat-card">
          <div class="icon-container">
            <el-image
              style="width: 50px; height: 50px; margin-top: 10px"
              :src="iconUrl.subjectImgUrl"
            />
          </div>
          <div class="stat-info">
            <div class="stat-title">课程总数</div>
            <div class="stat-value">{{ classCount }}</div>
          </div>
        </div>

        <!-- 试题总数卡片 -->
        <div class="stat-card">
          <div class="icon-container">
            <el-image
              style="width: 50px; height: 50px; margin-top: 10px"
              :src="iconUrl.questionImgUrl"
            />
          </div>
          <div class="stat-info">
            <div class="stat-title">试题总数</div>
            <div class="stat-value">{{ quCount }}</div>
          </div>
        </div>

        <!-- 试卷总数卡片 -->
        <div class="stat-card">
          <div class="icon-container">
            <el-image
              style="width: 50px; height: 50px; margin-top: 10px"
              :src="iconUrl.examImgUrl"
            />
          </div>
          <div class="stat-info">
            <div class="stat-title">试卷总数</div>
            <div class="stat-value">{{ examCount }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner" />
      <div class="loading-text">数据加载中...</div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ errorMessage }}
    </div>

    <!-- 图表区域 -->
    <div v-else class="charts-container">
      <div ref="classChart" class="chart-box" />
      <div ref="examChart" class="chart-box" />
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { classCount, classExamCount, classAllCounts } from '@/api/stat'
import questionImgUrl from '@/assets/img/questions.png'
import examImgUrl from '@/assets/img/paper.png'
import subjectPng from '@/assets/img/subject.png'
export default {
  name: 'TeacherDashboard',
  data() {
    return {
      iconUrl: {
        subjectImgUrl: subjectPng,
        questionImgUrl: questionImgUrl,
        examImgUrl: examImgUrl
      },
      // 图表数据
      chartData: [],
      chartDataTitle: [],
      chartData2: [],
      chartDataTitle2: [],

      // 统计数据
      classCount: 0,
      quCount: 0,
      examCount: 0,

      // 状态控制
      loading: true,
      error: false,
      errorMessage: '',

      // 图表实例
      classChartInstance: null,
      examChartInstance: null
    }
  },

  async created() {
    try {
      // 获取所有统计数据
      await this.fetchAllData()
    } catch (error) {
      this.handleError(error)
    }
  },

  mounted() {
    this.$nextTick(() => {
      // 初始化图表
      this.initCharts()

      // 添加窗口大小变化监听
      window.addEventListener('resize', this.handleResize)
    })
  },

  beforeUnmount() {
    // 销毁图表实例，避免内存泄漏
    if (this.classChartInstance) {
      this.classChartInstance.dispose()
    }
    if (this.examChartInstance) {
      this.examChartInstance.dispose()
    }

    // 移除窗口大小变化监听
    window.removeEventListener('resize', this.handleResize)
  },

  methods: {
    // 获取所有数据
    async fetchAllData() {
      this.loading = true
      try {
        // 获取总数统计
        try {
          const res0 = await classAllCounts()
          if (res0 && res0.data) {
            this.classCount = res0.data.classCount || 0
            this.quCount = res0.data.questionCount || 0
            this.examCount = res0.data.examCount || 0
          } else {
            this.classCount = 0
            this.quCount = 0
            this.examCount = 0
          }
        } catch (error) {
          console.error('获取总数统计失败:', error)
          this.classCount = 0
          this.quCount = 0
          this.examCount = 0
        }

        // 获取课程人数分布
        try {
          const res1 = await classCount()
          if (res1 && res1.data) {
            this.processChartData(res1.data)
          } else {
            this.chartData = [{ name: '暂无数据', value: 1 }]
            this.chartDataTitle = ['暂无数据']
          }
        } catch (error) {
          console.error('获取课程人数分布失败:', error)
          this.chartData = [{ name: '暂无数据', value: 1 }]
          this.chartDataTitle = ['暂无数据']
        }

        // 获取课程试卷分布
        try {
          const res2 = await classExamCount()
          this.processChartData2(res2 && res2.data ? res2.data : [])
        } catch (error) {
          console.error('获取课程试卷分布失败:', error)
          this.chartData2 = [{ name: '暂无数据', value: 1 }]
          this.chartDataTitle2 = ['暂无数据']
        }

        this.loading = false
        // 初始化图表
        this.initCharts()
      } catch (error) {
        this.handleError(error)
      }
    },

    // 处理错误
    handleError(error) {
      this.loading = false
      this.error = false
      // 不显示错误提示，而是显示默认的空数据状态
      console.error('数据加载失败:', error)
      // 设置默认数据
      this.classCount = 0
      this.quCount = 0
      this.examCount = 0
      this.chartData = [{ name: '暂无数据', value: 1 }]
      this.chartDataTitle = ['暂无数据']
      this.chartData2 = [{ name: '暂无数据', value: 1 }]
      this.chartDataTitle2 = ['暂无数据']
      // 初始化图表
      this.initCharts()
    },

    // 处理窗口大小变化
    handleResize() {
      if (this.classChartInstance) {
        this.classChartInstance.resize()
      }
      if (this.examChartInstance) {
        this.examChartInstance.resize()
      }
    },

    // 初始化图表
    initCharts() {
      try {
        this.$nextTick(() => {
          // 确保DOM已经渲染且引用存在
          if (this.$refs.classChart) {
            this.classChartInstance = echarts.init(this.$refs.classChart)
          }
          if (this.$refs.examChart) {
            this.examChartInstance = echarts.init(this.$refs.examChart)
          }

          // 设置图表配置
          this.updateClassChart()
          this.updateExamChart()
        })
      } catch (error) {
        console.error('初始化图表失败:', error)
        // 确保图表实例为 null
        this.classChartInstance = null
        this.examChartInstance = null
      }
    },

    // 处理课程人数分布数据
    processChartData(data) {
      if (!data || data.length === 0) {
        this.chartData = [{ name: '暂无数据', value: 1 }]
        this.chartDataTitle = ['暂无数据']
      } else {
        this.chartData = data.map((item) => ({
          name: item.subjectName,
          value: item.totalStudent
        }))
        this.chartDataTitle = this.chartData.map((item) => item.name)
      }

      // 如果图表已初始化，则更新图表
      if (this.classChartInstance) {
        this.updateClassChart()
      }
    },

    // 处理课程试卷分布数据
    processChartData2(data) {
      // 新增逻辑：检查数据是否为空
      if (data.length === 0) {
        // 设置默认数据
        this.chartData2 = [{ name: '暂无数据', value: 1 }]
        this.chartDataTitle2 = ['暂无数据']
      } else {
        this.chartData2 = data.map((item) => ({
          name: item.subjectName,
          value: item.total
        }))
        this.chartDataTitle2 = this.chartData2.map((item) => item.name)
      }

      // 如果图表已初始化，则更新图表
      if (this.examChartInstance) {
        this.updateExamChart()
      }
    },

    // 更新课程人数分布图表
    updateClassChart() {
      if (!this.classChartInstance) return

      try {
        const option = {
          // 标题
          title: {
            text: '课程人数分布',
            x: 'center' // 标题位置
          },
          // 鼠标划过时饼状图上显示的数据
          tooltip: {
            trigger: 'item',
            formatter: '{a}<br/>{b}:{c} ({d}%)'
          },
          // 图例
          legend: {
            bottom: 10, // 控制图例出现的距离
            left: 'center', // 控制图例的位置
            textStyle: {
              color: '#000',
              fontSize: 16
            },
            data: Array.isArray(this.chartDataTitle) ? this.chartDataTitle : []
          },
          // 饼图中各模块的颜色
          color: ['#32dadd', '#b6a2de', '#5ab1ef', '#454599'],
          series: {
            name: '课程人数',
            type: 'pie', // echarts图的类型   pie代表饼图
            radius: '60%', // 饼图中饼状部分的大小所占整个父元素的百分比
            center: ['50%', '50%'], // 整个饼图在整个父元素中的位置
            data: Array.isArray(this.chartData) ? this.chartData : [],
            itemStyle: {
              normal: {
                label: {
                  show: true // 饼图上是否出现标注文字
                },
                labelLine: {
                  show: true // 外部标注上的小细线的显示隐藏
                }
              }
            }
          }
        }

        this.classChartInstance.setOption(option)
      } catch (error) {
        console.error('更新课程人数分布图表失败:', error)
        // 设置默认配置
        try {
          this.classChartInstance.setOption({
            title: {
              text: '课程人数分布',
              x: 'center'
            },
            tooltip: {
              trigger: 'item',
              formatter: '{a}<br/>{b}:{c} ({d}%)'
            },
            legend: {
              bottom: 10,
              left: 'center',
              data: []
            },
            series: {
              name: '课程人数',
              type: 'pie',
              radius: '60%',
              center: ['50%', '50%'],
              data: [{ name: '暂无数据', value: 1 }]
            }
          })
        } catch (e) {
          console.error('设置课程人数分布图表默认配置失败:', e)
        }
      }
    },

    // 更新课程试卷分布图表
    updateExamChart() {
      if (!this.examChartInstance) return

      try {
        const option = {
          title: {
            text: '各类试题分布',
            x: 'center' // 标题位置
          },
          // 鼠标划过时饼状图上显示的数据
          tooltip: {
            trigger: 'item',
            formatter: '{a}<br/>{b}:{c} ({d}%)'
          },
          // 图例
          legend: {
            bottom: 10, // 控制图例出现的距离
            left: 'center', // 控制图例的位置
            textStyle: {
              color: '#000',
              fontSize: 14
            },
            data: Array.isArray(this.chartDataTitle2) ? this.chartDataTitle2 : [],
            formatter: (name) => {
              if (this.chartData2.length === 0 || (this.chartData2.length === 1 && this.chartData2[0].name === '暂无数据')) {
                return name
              }
              const item = this.chartData2.find(i => i.name === name)
              if (!item) return name
              const total = this.chartData2.reduce((acc, cur) => acc + cur.value, 0)
              const p = ((item.value / total) * 100).toFixed(2)
              return `${name}: ${p}%`
            }
          },
          // 饼图中各模块的颜色
          color: [
            'rgb(253, 133, 133)',
            'rgb(172, 10, 172)',
            'rgb(70, 35, 194)',
            'rgb(44, 199, 23)'
          ],
          // 饼图数据
          series: {
            name: '试题分布',
            type: 'pie',
            radius: '60%',
            center: ['50%', '50%'],
            data: Array.isArray(this.chartData2) ? this.chartData2 : [],
            itemStyle: {
              normal: {
                label: {
                  show: true, // 饼图上是否出现标注文字
                  formatter: '{b}: {c} ({d}%)' // 显示名称、数量和百分比
                },
                labelLine: {
                  show: true // 外部标注上的小细线的显示隐藏
                }
              }
            }
          }
        }

        this.examChartInstance.setOption(option)
      } catch (error) {
        console.error('更新各类试题分布图表失败:', error)
        // 设置默认配置
        try {
          this.examChartInstance.setOption({
            title: {
              text: '各类试题分布',
              x: 'center'
            },
            tooltip: {
              trigger: 'item',
              formatter: '{a}<br/>{b}:{c} ({d}%)'
            },
            legend: {
              bottom: 10,
              left: 'center',
              data: []
            },
            series: {
              name: '试题分布',
              type: 'pie',
              radius: '60%',
              center: ['50%', '50%'],
              data: [{ name: '暂无数据', value: 1 }]
            }
          })
        } catch (e) {
          console.error('设置各类试题分布图表默认配置失败:', e)
        }
      }
    }
  }
}
</script>

<style scoped>
/* 统计卡片容器 */
.stats-container {
  margin: auto;
  border-radius: 16px;
  width: 100%;
  padding: 20px;
  margin-top: 30px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 0 3px 0 rgba(0, 0, 0, 0.04);
  background-color: #fff;
}

.stats-row {
  width: 100%;
  display: flex;
  justify-content: space-evenly;
  flex-wrap: wrap;
}

/* 统计卡片 */
.stat-card {
  /* width: 30%;
  min-width: 250px;
  height: 80px; */
  display: flex;
  background-color: #fff;
  transition: all 0.3s ease;
}

.icon-container {
  display: flex;
  transform: translateY(-6px);
  align-items: center;
}

.stat-info {
  display: flex;
  margin-left: 10px;
  flex-direction: column;
  justify-content: center;
}

.stat-title {
  font-size: 22px;
  font-weight: 500;
  /* padding: 0 0 5px 10px; */
  color: #333;
}

.stat-value {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-top: 4px;
  /* padding: 0 0 0 10px; */
  color: #409EFF;
}

/* 图表容器 */
.charts-container {
  width: 100%;
  height: 60vh;
  display: flex;
  margin: auto;
  margin-top: 30px;
  justify-content: space-between;
  flex-wrap: wrap;
}

.chart-box {
  width: 48%;
  min-width: 300px;
  border-radius: 16px;
  height: 100%;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 0 3px 0 rgba(0, 0, 0, 0.04);
  background-color: #fff;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  width: 100%;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #409EFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 20px;
  font-size: 18px;
  color: #666;
}

/* 错误提示 */
.error-message {
  text-align: center;
  color: #F56C6C;
  font-size: 18px;
  margin-top: 30px;
  padding: 20px;
  background-color: #FEF0F0;
  border-radius: 4px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式布局 */
@media screen and (max-width: 991px) {
  .stats-row {
    flex-wrap: wrap;
    gap: 10px;
  }
  .stat-card {
    flex: 1 1 calc(50% - 10px);
    min-width: 140px;
  }
}

@media screen and (max-width: 768px) {
  .stats-row {
    flex-direction: column;
  }

  .stat-card {
    width: 100%;
  }

  .charts-container {
    flex-direction: column;
  }

  .chart-box {
    width: 100%;
    height: 400px;
  }
}
</style>
