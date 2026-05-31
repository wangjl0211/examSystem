<template>
  <div class="test-container">
    <h2>滑块验证码测试页</h2>
    <div class="test-block">
      <h3>功能测试</h3>
      <el-button type="primary" @click="showCaptcha = true">打开验证码</el-button>
      <el-button @click="testRequest">测试网络请求</el-button>
      <div class="visual-test-guide" style="margin-top: 10px; color: #666; font-size: 14px;">
        <p><strong>对齐测试指南：</strong></p>
        <ol>
          <li>点击“打开验证码”</li>
          <li><strong>垂直对齐测试：</strong>观察滑块图上边缘与缺口上边缘是否完全对齐（无像素偏差）。</li>
          <li><strong>水平对齐测试：</strong>拖动滑块至缺口处，检查拼合是否完美，无左右缝隙。</li>
          <li><strong>边界测试：</strong>快速拖动至最左侧和最右侧，检查是否会溢出容器。</li>
        </ol>
      </div>
    </div>
    
    <div class="test-block">
      <h3>日志输出</h3>
      <div class="log-box">
        <div v-for="(log, index) in logs" :key="index" :class="log.type">
          [{{ log.time }}] {{ log.msg }}
        </div>
      </div>
    </div>

    <SlideCaptcha v-model:show="showCaptcha" @success="onSuccess" />
  </div>
</template>

<script>
import SlideCaptcha from '@/components/SlideCaptcha'
import { createSlideCaptcha } from '@/api/user'

export default {
  name: 'TestCaptcha',
  components: {
    SlideCaptcha
  },
  data() {
    return {
      showCaptcha: false,
      logs: []
    }
  },
  methods: {
    addLog(msg, type = 'info') {
      const time = new Date().toLocaleTimeString()
      this.logs.unshift({ time, msg, type })
    },
    onSuccess() {
      this.addLog('验证成功回调触发', 'success')
      this.$message.success('验证通过！')
    },
    testRequest() {
      this.addLog('开始测试 createSlideCaptcha 接口...')
      createSlideCaptcha()
        .then(res => {
          this.addLog(`接口响应成功: code=${res.code}`, 'success')
          console.log(res)
        })
        .catch(err => {
          this.addLog(`接口请求失败: ${err.message}`, 'error')
          console.error(err)
        })
    }
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
}
.test-block {
  margin-bottom: 20px;
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 4px;
}
.log-box {
  height: 300px;
  overflow-y: auto;
  background: #f5f5f5;
  padding: 10px;
  border: 1px solid #ddd;
}
.info { color: #333; }
.success { color: #67c23a; }
.error { color: #f56c6c; }
</style>
