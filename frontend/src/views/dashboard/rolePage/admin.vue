<template>
  <div class="app-container">
    <el-row :gutter="20">
     <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>服务器信息</span>
            </div>
          </template>
          <div class="card-content">
            <div v-loading="loading" element-loading-text="加载中..." style="min-height: 120px;">
              <div v-if="!loading">
                <p>服务器名称: {{ serverInfo.name }}</p>
                <p>操作系统: {{ serverInfo.os }}</p>
                <p>IP地址: {{ serverInfo.ip }}</p>
                <p>系统架构: {{ serverInfo.arch }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>数据信息</span>
            </div>
          </template>
          <div class="card-content">
            <div v-loading="loading" element-loading-text="加载中..." style="min-height: 120px;">
              <div v-if="!loading">
                <p>课程总数: {{ dataInfo.courseCount }}</p>
                <p>用户总数: {{ dataInfo.userCount }}</p>
                <p>今日创建课程: {{ dataInfo.todayCourseCount }}</p>
                <p>今日新增用户: {{ dataInfo.todayUserCount }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 错误信息显示 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      style="margin-top: 20px"
      :closable="true"
      @close="error = null"
    />

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>在线人数统计</span>
            </div>
          </template>
          <div v-loading="loading" element-loading-text="加载中..." style="height: 300px;">
            <div id="onlineChart" style="height: 100%;"></div>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="clearfix">
              <span>操作统计</span>
            </div>
          </template>
          <div v-loading="loading" element-loading-text="加载中..." style="height: 300px;">
            <div id="opChart" style="height: 100%;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>数据信息统计</span>
            </div>
          </template>
          <div v-loading="loading" element-loading-text="加载中..." style="height: 300px;">
            <div id="dataChart" style="height: 100%;"></div>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="clearfix">
              <span>耗时分布</span>
            </div>
          </template>
          <div v-loading="loading" element-loading-text="加载中..." style="height: 300px;">
            <div id="durationChart" style="height: 100%;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-top: 20px; text-align: center;">
      <el-button type="primary" icon="Refresh" @click="refreshData" :loading="loading">手动刷新</el-button>
      <span style="margin-left: 10px; font-size: 12px; color: #909399;">自动刷新: {{ refreshInterval / 1000 }}秒</span>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboardData } from '@/api/stat'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      serverInfo: { name: '未知', os: '未知', ip: '未知', arch: '未知' },
      dataInfo: { courseCount: 0, userCount: 0, todayCourseCount: 0, todayUserCount: 0 },
      onlineData: [],
      dailyData: [],
      opData: [50, 20, 10, 20],
      durationData: [
        {value: 335, name: '<100ms'},
        {value: 310, name: '100-500ms'},
        {value: 234, name: '>500ms'}
      ],
      refreshInterval: 300000, // 5分钟
      timer: null,
      onlineChart: null,
      dataChart: null,
      opChart: null,
      durationChart: null,
      loading: false,
      error: null
    }
  },
  mounted() {
    this.initCharts()
    this.getData()
    this.startAutoRefresh()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeUnmount() {
    clearInterval(this.timer)
    window.removeEventListener('resize', this.resizeCharts)
  },
  methods: {
    initCharts() {
      return new Promise((resolve) => {
        this.$nextTick(() => {
          // 确保DOM元素已存在
          const onlineChartEl = document.getElementById('onlineChart')
          const dataChartEl = document.getElementById('dataChart')
          const opChartEl = document.getElementById('opChart')
          const durationChartEl = document.getElementById('durationChart')
          
          // 重新初始化图表（销毁旧实例）
          if (this.onlineChart) {
            this.onlineChart.dispose()
          }
          this.onlineChart = onlineChartEl ? echarts.init(onlineChartEl) : null
          
          if (this.dataChart) {
            this.dataChart.dispose()
          }
          this.dataChart = dataChartEl ? echarts.init(dataChartEl) : null
          
          if (this.opChart) {
            this.opChart.dispose()
          }
          this.opChart = opChartEl ? echarts.init(opChartEl) : null
          
          if (this.durationChart) {
            this.durationChart.dispose()
          }
          this.durationChart = durationChartEl ? echarts.init(durationChartEl) : null
          
          this.updateCharts() // 初始化后立即更新数据
          resolve()
        })
      })
    },
    resizeCharts() {
      this.onlineChart && this.onlineChart.resize()
      this.dataChart && this.dataChart.resize()
      this.opChart && this.opChart.resize()
      this.durationChart && this.durationChart.resize()
    },
    updateCharts() {
      try {
        // 确保DOM元素存在，如果图表实例不存在则重新初始化
        if (!this.onlineChart || !this.dataChart || !this.opChart || !this.durationChart) {
          console.log('图表实例不存在，重新初始化...')
          this.initCharts()
          return
        }
        
        // 在线人数统计数据 - 确保数据格式正确
        const onlineTimes = Array.isArray(this.onlineData) ? this.onlineData.map(item => item?.time || '').filter(Boolean) : []
        const onlineCounts = Array.isArray(this.onlineData) ? this.onlineData.map(item => Number(item?.count || 0)) : []

        // 每日注册人数和新增课程数据 - 确保数据格式正确
        const dailyDates = Array.isArray(this.dailyData) ? this.dailyData.map(item => item?.date || '').filter(Boolean) : []
        const registerCounts = Array.isArray(this.dailyData) ? this.dailyData.map(item => Number(item?.registerCount || 0)) : []
        const courseCounts = Array.isArray(this.dailyData) ? this.dailyData.map(item => Number(item?.courseCount || 0)) : []

        // 在线人数统计图表
        const onlineOption = {
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: onlineTimes, axisLabel: { rotate: 45 } },
          yAxis: {
            type: 'value',
            name: '在线人数',
            min: 0,
            axisLabel: {
              formatter: '{value}',
              precision: 0
            },
            splitNumber: 5,
            minInterval: 1
          },
          series: [{ 
            data: onlineCounts, 
            type: 'line', 
            smooth: true, 
            name: '在线人数', 
            lineStyle: { width: 2 },
            areaStyle: { opacity: 0.1 }
          }],
          grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true }
        }
        this.onlineChart.setOption(onlineOption)

        // 数据信息统计图表
        const dataOption = {
          tooltip: { trigger: 'axis' },
          legend: { data: ['每日注册人数', '每日新增课程'], top: 0 },
          xAxis: { type: 'category', data: dailyDates, axisLabel: { rotate: 45 } },
          yAxis: {
            type: 'value',
            name: '数量',
            min: 0,
            axisLabel: {
              formatter: '{value}',
              precision: 0
            },
            splitNumber: 5,
            minInterval: 1
          },
          series: [
            { data: registerCounts, type: 'bar', name: '每日注册人数', itemStyle: { color: '#409EFF' } },
            { data: courseCounts, type: 'bar', name: '每日新增课程', itemStyle: { color: '#67C23A' } }
          ],
          grid: { left: '3%', right: '4%', bottom: '15%', top: '20%', containLabel: true }
        }
        this.dataChart.setOption(dataOption)

        // 操作统计图表
        const opOption = {
          tooltip: {},
          xAxis: { data: ["Select", "Update", "Delete", "Insert"], axisLabel: { interval: 0, rotate: 30 } },
          yAxis: {},
          grid: { bottom: 30, top: 10, left: 30, right: 10 },
          series: [{ type: 'bar', data: this.opData }]
        }
        this.opChart.setOption(opOption)

        // 耗时分布图表
        const durationOption = {
          tooltip: {},
          series: [{
            type: 'pie',
            radius: '70%',
            center: ['50%', '50%'],
            data: this.durationData
          }]
        }
        this.durationChart.setOption(durationOption)
        
        console.log('图表更新成功:', {
          onlineData: { times: onlineTimes.length, counts: onlineCounts.length },
          dailyData: { dates: dailyDates.length, registers: registerCounts.length, courses: courseCounts.length },
          opData: this.opData.length,
          durationData: this.durationData.length
        })
      } catch (error) {
        console.error('图表更新失败:', error)
        // 即使出错，也要确保图表有默认配置
        if (this.onlineChart) {
          try {
            this.onlineChart.setOption({ 
              tooltip: { trigger: 'axis' },
              xAxis: { type: 'category', data: [] },
              yAxis: { type: 'value', name: '在线人数' },
              series: [{ data: [], type: 'line', name: '在线人数' }]
            })
          } catch (e) {
            console.error('设置在线人数图表默认配置失败:', e)
          }
        }
        if (this.dataChart) {
          try {
            this.dataChart.setOption({ 
              tooltip: { trigger: 'axis' },
              legend: { data: ['每日注册人数', '每日新增课程'] },
              xAxis: { type: 'category', data: [] },
              yAxis: { type: 'value', name: '数量' },
              series: [
                { data: [], type: 'bar', name: '每日注册人数' },
                { data: [], type: 'bar', name: '每日新增课程' }
              ]
            })
          } catch (e) {
            console.error('设置数据信息图表默认配置失败:', e)
          }
        }
        if (this.opChart) {
          try {
            this.opChart.setOption({ 
              tooltip: {},
              xAxis: { data: ["Select", "Update", "Delete", "Insert"] },
              yAxis: {},
              series: [{ type: 'bar', data: [] }]
            })
          } catch (e) {
            console.error('设置操作统计图表默认配置失败:', e)
          }
        }
        if (this.durationChart) {
          try {
            this.durationChart.setOption({ 
              tooltip: {},
              series: [{
                type: 'pie',
                data: []
              }]
            })
          } catch (e) {
            console.error('设置耗时分布图表默认配置失败:', e)
          }
        }
      }
    },
    async getData() {
      this.loading = true
      this.error = null
      try {
        console.log('开始获取仪表板数据...')
        const res = await getDashboardData()
        console.log('获取数据响应:', res)
        
        if (res && (res.code === 200 || res.code === 1 || res.code === 0)) { // 明确判断成功状态码
          // 确保数据结构正确
          const data = res.data || {}
          // 深拷贝数据，避免引用类型导致的旧数据残留
          this.serverInfo = { ...data.serverInfo }
          this.dataInfo = { ...data.dataInfo }
          this.onlineData = Array.isArray(data.onlineData) ? [...data.onlineData] : []
          this.dailyData = Array.isArray(data.dailyData) ? [...data.dailyData] : []
          this.opData = Array.isArray(data.opData) ? [...data.opData] : [50, 20, 10, 20]
          this.durationData = Array.isArray(data.durationData) ? [...data.durationData] : [
            {value: 335, name: '<100ms'},
            {value: 310, name: '100-500ms'},
            {value: 234, name: '>500ms'}
          ]
          
          console.log('数据处理完成:', {
            serverInfo: Object.keys(this.serverInfo || {}).length,
            dataInfo: Object.keys(this.dataInfo || {}).length,
            onlineData: this.onlineData.length,
            dailyData: this.dailyData.length,
            opData: this.opData.length,
            durationData: this.durationData.length
          })
          
          // 强制触发图表更新（无论实例是否存在）
          this.$nextTick(async () => {
            await this.initCharts() // 初始化后自动更新
          })
        } else {
          this.error = res?.msg || '获取数据失败'
          console.error('获取数据失败:', res?.msg)
          // 显示默认图表
          this.$nextTick(() => {
            this.updateCharts()
          })
        }
      } catch (error) {
        this.error = '网络错误，请检查网络连接'
        console.error('网络错误:', error)
        // 即使出错，也要显示默认图表
        this.$nextTick(() => {
          this.updateCharts()
        })
      } finally {
        this.loading = false
        console.log('数据获取流程完成')
      }
    },
    refreshData() {
      this.getData()
    },
    startAutoRefresh() {
      this.timer = setInterval(() => {
        this.getData()
      }, this.refreshInterval)
    }
  }
}
</script>

<style scoped>
.card-content p {
  font-size: 14px;
  color: #606266;
  margin: 10px 0;
}
.progress-item {
  display: flex;
  align-items: center;
  margin-top: 10px;
}
.progress-item span {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}
.el-progress {
  flex: 1;
}
</style>
