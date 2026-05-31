<template>
  <el-dialog
    title="安全验证"
    v-model="visible"
    width="360px"
    :close-on-click-modal="false"
    @close="handleClose"
    append-to-body
    custom-class="slide-captcha-dialog"
  >
    <div class="slide-captcha">
      <div class="captcha-box" v-loading="loading">
        <div class="captcha-img">
          <img v-if="bgImage" :src="bgImage" class="bg-img" alt="背景图" />
          <img
            v-if="sliderImage"
            :src="sliderImage"
            class="slider-img"
            :style="{ top: (yGap - 5) + 'px', left: sliderLeft + 'px' }"
            alt="滑块图"
          />
          <div class="refresh-btn" @click="refresh" title="刷新验证码">
            <el-icon>
              <component :is="'Refresh'" />
            </el-icon>
          </div>
        </div>
        <div class="captcha-slider">
          <div class="track" :class="{ success: isSuccess }">
            <div class="track-text" :style="{ opacity: isMoving ? 0 : 1 }">{{ tipText }}</div>
          </div>
          <div
            class="slider-btn"
            :class="{ active: isMoving, success: isSuccess, fail: isFail }"
            :style="{ left: sliderLeft + 'px' }"
            @mousedown="startDrag"
            @touchstart="startDrag"
          >
            <el-icon>
              <component :is="btnIcon" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { createSlideCaptcha, verifySlideCaptcha } from '@/api/user'

export default {
  name: 'SlideCaptcha',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: false,
      loading: false,
      bgImage: '',
      sliderImage: '',
      yGap: 0,
      token: '',
      sliderLeft: 0,
      isMoving: false,
      startX: 0,
      tipText: '向右滑动填充拼图',
      isSuccess: false,
      isFail: false,
      isRequesting: false
    }
  },
  computed: {
    btnIcon() {
      if (this.isSuccess) return 'Check'
      if (this.isFail) return 'Close'
      return 'DArrowRight'
    }
  },
  watch: {
    show: {
      handler(val) {
        this.visible = val
        if (val) {
          this.reset()
          this.getCaptcha()
        }
      },
      immediate: true
    },
    visible(val) {
      this.$emit('update:show', val)
      if (!val) {
        this.$emit('close')
      }
    }
  },
  methods: {
    // 增加防抖和请求状态锁，防止重复请求
    getCaptcha() {
      if (this.isRequesting) return
      console.log('[SlideCaptcha] 开始获取验证码')
      this.isRequesting = true
      this.loading = true
      this.isSuccess = false
      this.isFail = false
      createSlideCaptcha().then(res => {
        console.log('[SlideCaptcha] 获取验证码响应:', res)
        if ((res.code === 200 || res.code === 1) && res.data) {
          this.bgImage = res.data.backgroundImageBase64
          this.sliderImage = res.data.sliderImageBase64
          // 兼容后端可能返回的字段名差异（Jackson序列化可能导致首字母大写等问题）
          // 同时添加 Number() 转换，防止字符串导致的计算错误
          this.yGap = Number(res.data.yGap || res.data.YGap || res.data.ygap || 0)
          this.token = res.data.token
          this.tipText = '向右滑动填充拼图'
          console.log('[SlideCaptcha] 设置 yGap:', this.yGap)
        } else {
          // 如果未开启验证码或出错
          console.error('[SlideCaptcha] 获取验证码失败:', res.msg)
          this.$message.error(res.msg || '获取验证码失败')
        }
        this.loading = false
        this.isRequesting = false
      }).catch(error => {
        console.error('[SlideCaptcha] 获取验证码请求异常:', error)
        this.loading = false
        this.isRequesting = false
        this.tipText = '加载失败，请刷新'
      })
    },
    startDrag(e) {
      if (this.loading || this.isSuccess) return
      this.isMoving = true
      this.isFail = false
      this.startX = e.clientX || e.touches[0].clientX
      
      document.addEventListener('mousemove', this.onMove)
      document.addEventListener('mouseup', this.onEnd)
      document.addEventListener('touchmove', this.onMove)
      document.addEventListener('touchend', this.onEnd)
    },
    onMove(e) {
      if (!this.isMoving) return
      const clientX = e.clientX || e.touches[0].clientX
      let moveX = clientX - this.startX
      
      if (moveX < 0) moveX = 0
      // 限制最大滑动距离：容器宽度(300) - 滑块宽度(45)
      // 注意：滑块按钮的CSS宽度是46px (包含边框)，但逻辑宽度通常按45计算
      // 这里为了精确，我们统一使用45作为逻辑宽度
      if (moveX > 300 - 45) moveX = 300 - 45 
      
      // 使用 requestAnimationFrame 优化动画流畅度，避免高频重绘
      window.requestAnimationFrame(() => {
        this.sliderLeft = moveX
      })
    },
    onEnd() {
      if (!this.isMoving) return
      this.isMoving = false
      document.removeEventListener('mousemove', this.onMove)
      document.removeEventListener('mouseup', this.onEnd)
      document.removeEventListener('touchmove', this.onMove)
      document.removeEventListener('touchend', this.onEnd)
      
      this.verify()
    },
    verify() {
      console.log('[SlideCaptcha] 开始校验验证码, token:', this.token, 'xPos:', Math.round(this.sliderLeft))
      // 确保 xPos 是有效的数字
      const xPosValue = Math.round(this.sliderLeft)
      const data = {
        token: this.token,
        xPos: xPosValue // 确保字段名与后端匹配
      }
      verifySlideCaptcha(data).then(res => {
        console.log('[SlideCaptcha] 校验响应:', res)
        if (res.code === 200 || res.code === 1) {
          this.isSuccess = true
          this.tipText = '验证通过'
          // 保存验证token，用于后续登录请求
          const verifyToken = res.data
          setTimeout(() => {
            this.visible = false
            this.$emit('success', verifyToken)
          }, 800)
        } else {
          console.warn('[SlideCaptcha] 校验失败:', res.msg)
          this.isFail = true
          this.onFail()
        }
      }).catch(error => {
        console.error('[SlideCaptcha] 校验请求异常:', error)
        this.isFail = true
        this.onFail()
      })
    },
    onFail() {
      this.tipText = '验证失败，请重试'
      setTimeout(() => {
        this.reset()
        this.getCaptcha()
      }, 800)
    },
    reset() {
      this.sliderLeft = 0
      this.isSuccess = false
      this.isFail = false
      this.tipText = '向右滑动填充拼图'
    },
    refresh() {
      this.reset()
      this.getCaptcha()
    },
    handleClose() {
      this.visible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.slide-captcha {
  position: relative;
  /* padding-bottom: 10px; */
  user-select: none;
}
.captcha-box {
  position: relative;
  width: 300px;
  margin: 0 auto;
}
.captcha-img {
  position: relative;
  width: 300px;
  height: 200px;
  background: #f7f9fa;
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.captcha-img .bg-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
.captcha-img .slider-img {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 2;
}
.captcha-img .refresh-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  cursor: pointer;
  font-size: 16px;
  color: #fff;
  z-index: 10;
  background: rgba(0,0,0,0.3);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}
.captcha-img .refresh-btn:hover {
  background: rgba(0,0,0,0.6);
  transform: rotate(180deg);
}
.captcha-slider {
  position: relative;
  width: 300px;
  height: 40px;
  margin-top: 15px;
  background: #f7f9fa;
  border: 1px solid #e4e7eb;
  border-radius: 20px;
  box-shadow: inset 0 0 5px #ccc;
}
.captcha-slider .track {
  width: 100%;
  height: 100%;
  line-height: 38px;
  text-align: center;
  font-size: 14px;
  color: #45494c;
  border-radius: 20px;
}
.captcha-slider .track.success {
  background: #D2F4EF;
  color: #52CCBA;
  border-color: #52CCBA;
}
.captcha-slider .slider-btn {
  position: absolute;
  top: -1px;
  left: 0;
  width: 46px;
  height: 40px;
  background: #fff;
  box-shadow: 0 0 3px rgba(0,0,0,0.3);
  cursor: pointer;
  transition: background .2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  border: 1px solid #ddd;
}
.captcha-slider .slider-btn:hover,
.captcha-slider .slider-btn.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}
.captcha-slider .slider-btn.success {
  background: #52CCBA;
  color: #fff;
  border-color: #52CCBA;
}
.captcha-slider .slider-btn.fail {
  background: #F56C6C;
  color: #fff;
  border-color: #F56C6C;
}
</style>
