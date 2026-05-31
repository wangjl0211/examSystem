// vite.config.js - 精简兼容版
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import path from 'path'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { visualizer } from 'rollup-plugin-visualizer'




// https://vitejs.dev/config/
export default defineConfig({
  // 基础路径
  base: './',
  
  // 解析配置
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
    extensions: ['.vue', '.js', '.ts', '.json']
  },

  // 插件配置 - 仅保留已安装的
  plugins: [
    // Vue 3 支持 (已安装 @vitejs/plugin-vue)
    vue(),
    // SVG 图标支持
    createSvgIconsPlugin({
      // 指定需要缓存的图标文件夹
      iconDirs: [path.resolve(process.cwd(), 'src/icons/svg')],
      // 指定symbolId格式
      symbolId: 'icon-[dir]-[name]'
    }),
    // 打包分析插件 - 仅在构建时启用
    visualizer({
      filename: 'dist/stats.html',
      open: false,
      gzipSize: true,
      brotliSize: true
    }),
    {
      name: 'login-info-log',
      configureServer(server) {
        server.httpServer?.once('listening', () => {
          const port = server.config.server.port || 9527
          const host = server.config.server.host || 'localhost'
          console.log(`  ➜  管理员登录：http://${host}:${port}/#/admin/login`)
          console.log(`  ➜  普通用户登录：http://${host}:${port}/#/login`)
        })
      }
    }
  ],

  // 服务器配置
  server: {
    host: 'localhost',
    port: 9527,
    open: true,
    cors: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },

  // 构建配置
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    rollupOptions: {
      output: {
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          if (/.+(png|jpe?g|gif|svg|webp|avif)(\?.*)?$/i.test(assetInfo.name)) {
            return 'assets/img/[name]-[hash][extname]'
          }
          if (/.+(woff2?|eot|ttf|otf)(\?.*)?$/i.test(assetInfo.name)) {
            return 'assets/fonts/[name]-[hash][extname]'
          }
          return 'assets/[ext]/[name]-[hash][extname]'
        },
        // 手动分块策略 - 优化大文件分块
        manualChunks: {
          // 第三方库分块
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          'vendor-element': ['element-plus', '@element-plus/icons-vue'],
          'vendor-charts': ['echarts'],
          'vendor-utils': ['axios', 'crypto-js', 'js-cookie', 'jwt-decode']
        }
      }
    },
    // 分块警告限制 - 10MB
    chunkSizeWarningLimit: 10000
  },

  // CSS 配置
  css: {
    preprocessorOptions: {
      scss: {
        charset: false
      }
    }
  },

  // 优化选项
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'element-plus',
      '@element-plus/icons-vue',
      'axios'
    ],
    exclude: []
  },

  // 环境变量
  envPrefix: ['VITE_', 'VUE_APP_']
})