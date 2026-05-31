<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <el-input v-model="search" placeholder="搜索接口..." style="width: 300px; margin-right: 20px;">
          <template #prefix>
            <el-icon class="el-input__icon"><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="filterMethod" placeholder="HTTP方法" clearable style="width: 120px; margin-right: 20px;">
          <el-option v-for="m in ['GET','POST','PUT','DELETE']" :key="m" :label="m" :value="m">
          </el-option>
       </el-select>
       <el-select v-model="filterCategory" placeholder="接口分类" clearable style="width: 150px;">
         <el-option v-for="c in categories" :key="c" :label="c" :value="c">
         </el-option>
       </el-select>
       <el-button type="primary" style="float: right; margin-right: 10px;" @click="refreshApiInfo" :loading="loading">
         <el-icon class="el-icon--left"><Refresh /></el-icon> 刷新
       </el-button>
       <el-button type="primary" style="float: right; margin-right: 10px;" @click="exportDoc">导出文档</el-button>
       <el-select v-model="sortBy" placeholder="排序方式" style="width: 120px; float: right; margin-right: 20px;">
         <el-option value="path" label="按路径排序"></el-option>
         <el-option value="method" label="按方法排序"></el-option>
         <el-option value="category" label="按分类排序"></el-option>
       </el-select>
      </template>
     
     <!-- 接口数量统计 -->
     <div style="margin-bottom: 15px; font-size: 14px; color: #606266;">
       <span>共 {{ filteredApis.length }} 个接口</span>
       <span v-if="filterCategory" style="margin-left: 10px;">| 分类: {{ filterCategory }}</span>
       <span v-if="filterMethod" style="margin-left: 10px;">| 方法: {{ filterMethod }}</span>
       <span v-if="search" style="margin-left: 10px;">| 搜索: {{ search }}</span>
     </div>
      
      <!-- 按分类分组显示接口 -->
      <div v-for="(apisInCategory, category) in groupedApis" :key="category" style="margin-bottom: 20px;">
        <el-card :header="category" shadow="hover">
          <el-collapse accordion>
            <el-collapse-item v-for="(api, index) in apisInCategory" :key="index">
              <template #title>
                <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                  <div style="display: flex; align-items: center;">
                    <el-tag size="small" :type="getMethodColor(api.method)" style="margin-right: 10px; width: 60px; text-align: center;">{{ api.method }}</el-tag>
                    <span style="font-weight: bold; margin-right: 20px;">{{ api.path }}</span>
                    <span style="color: #909399;">{{ api.desc }}</span>
                  </div>
                  <el-tag size="mini" :type="api.status === '已启用' ? 'success' : 'info'">
                    {{ api.status }}
                  </el-tag>
                </div>
              </template>
              
              <div class="api-detail">
                <!-- 请求参数区域 -->
                <div class="section-header">
                  <h4>请求参数</h4>
                </div>
                <el-table :data="api.params" border size="small" class="param-table">
                  <el-table-column prop="name" label="参数名" min-width="150">
                    <template #default="scope">
                      <span class="param-name">{{ scope.row.name }}</span>
                      <el-tag v-if="scope.row.required" size="mini" type="danger" style="margin-left: 5px;">必填</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="pos" label="位置" width="100">
                    <template #default="scope">
                      <el-tag size="mini" :type="getPositionType(scope.row.pos)">
                        {{ scope.row.pos }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="类型" width="120">
                    <template #default="scope">
                      <span class="param-type">{{ scope.row.type }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="desc" label="参数说明" min-width="200">
                    <template #default="scope">
                      <div class="param-desc">
                        <div>{{ scope.row.desc || '暂无说明' }}</div>
                        <div v-if="scope.row.example" class="param-example">
                          <span class="example-label">示例: </span>
                          <span class="example-value">{{ scope.row.example }}</span>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="example" label="示例值" min-width="150">
                    <template #default="scope">
                      <code v-if="scope.row.example" class="example-code">{{ scope.row.example }}</code>
                      <span v-else style="color: #909399;">-</span>
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 示例区域 -->
                <div class="section-header" style="margin-top: 25px;">
                  <h4>请求体示例</h4>
                  <div class="header-actions">
                    <el-button type="primary" size="small" @click="copyExample(api.reqExample, '请求体')">
                      <el-icon><DocumentCopy /></el-icon> 复制
                    </el-button>
                    <el-button size="small" @click="formatJsonField(api, 'reqExample')">
                      <el-icon><Operation /></el-icon> 美化
                    </el-button>
                  </div>
                </div>
                <div class="example-container">
                  <div class="example-header">
                    <span class="example-title">📋 请求体</span>
                    <span class="example-hint" v-if="hasPlaceholders(api.reqExample)">
                      ⚠️ 包含占位符，请替换为实际值
                    </span>
                  </div>
                  <pre class="code-block code-block-request">{{ getFormattedJson(api.reqExample) }}</pre>
                </div>

                <div class="section-header" style="margin-top: 25px;">
                  <h4>响应示例</h4>

                </div>
                <div class="example-container">
                  <div class="example-header">
                    <span class="example-title">📤 响应</span>
                  </div>
                  <pre class="code-block code-block-response">{{ getFormattedJson(api.resExample) }}</pre>
                </div>
                
                <!-- 操作按钮区域 
                <div class="action-area" style="margin-top: 25px;">
                  <el-button type="primary" size="small" @click="testApi(api)">
                    <el-icon><Right /></el-icon> 测试接口
                  </el-button>
                  <el-button size="small" @click="copyCurl(api)">
                    <el-icon><Document /></el-icon> 复制 cURL
                  </el-button>
                </div> -->
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </div>
    </el-card>

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

    <!-- 加载状态显示 -->
    <div v-if="loading" style="display: flex; justify-content: center; align-items: center; padding: 40px;">
      <el-icon class="is-loading" style="font-size: 24px;"><Loading /></el-icon>
      <span style="margin-left: 10px; font-size: 16px;">正在加载接口信息...</span>
    </div>
    
    <!-- 空状态提示 -->
    <div v-else-if="Object.keys(groupedApis).length === 0" style="text-align: center; padding: 60px;">
      <el-empty description="未找到匹配的接口" />
    </div>

    <el-dialog title="接口测试" v-model="dialogVisible" width="80%">
      <p>接口: {{ currentApi.path }}</p>
      <p>方法: {{ currentApi.method }}</p>
      <el-input type="textarea" :rows="6" v-model="testBody" placeholder="请求体内容"></el-input>
      <div v-if="testResponse" style="margin-top: 20px;">
        <h4>响应结果:</h4>
        <pre class="code-block">{{ JSON.stringify(testResponse, null, 2) }}</pre>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendTest" :loading="sendingTest">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getAllApiInfo } from '@/api/apiInfo'
import request from '@/utils/request'
import { Search, Refresh, DocumentCopy, Operation, Right, Document } from '@element-plus/icons-vue'


export default {
  name: 'ApiDocument',
  components: {
    Search,
    Refresh,
    DocumentCopy,
    Operation,
    Right,
    Document
  },
  data() {
    return {
      search: '',
      filterMethod: '',
      filterCategory: '',
      sortBy: 'path',
      dialogVisible: false,
      currentApi: {},
      testBody: '',
      testResponse: null,
      sendingTest: false,
      apis: [],
      loading: false,
      error: null,
      refreshInterval: 60000, // 1分钟自动刷新
      refreshTimer: null,
      // 缓存格式化后的JSON
      formattedJsonCache: {}
    }
  },
  computed: {
    filteredApis() {
      let filtered = this.apis.filter(api => {
        if (this.filterMethod && api.method !== this.filterMethod) return false
        if (this.filterCategory && api.category !== this.filterCategory) return false
        if (this.search && !api.path.includes(this.search) && !api.desc.includes(this.search)) return false
        return true
      })
      
      // 根据sortBy进行排序
      filtered.sort((a, b) => {
        if (this.sortBy === 'path') {
          return a.path.localeCompare(b.path)
        } else if (this.sortBy === 'method') {
          return a.method.localeCompare(b.method)
        } else if (this.sortBy === 'category') {
          return (a.category || '').localeCompare(b.category || '')
        }
        return 0
      })
      
      return filtered
    },
    categories() {
      // 从apis中提取所有唯一的分类
      const categorySet = new Set()
      this.apis.forEach(api => {
        if (api.category) {
          categorySet.add(api.category)
        }
      })
      return Array.from(categorySet)
    },
    // 按分类分组的接口
    groupedApis() {
      const groups = {}
      this.filteredApis.forEach(api => {
        const category = api.category || '未分类'
        if (!groups[category]) {
          groups[category] = []
        }
        groups[category].push(api)
      })
      return groups
    }
  },
  mounted() {
    this.getApiInfo()
    this.startAutoRefresh()
    
    // 捕获ResizeObserver循环错误
    if (window.ResizeObserver) {
      const originalResizeObserver = window.ResizeObserver
      window.ResizeObserver = class ResizeObserver extends originalResizeObserver {
        constructor(callback) {
          super((entries, observer) => {
            try {
              callback(entries, observer)
            } catch (error) {
              console.warn('ResizeObserver循环错误已捕获:', error)
            }
          })
        }
      }
    }
  },
  beforeUnmount() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    getMethodColor(method) {
      const map = { GET: '', POST: 'success', PUT: 'warning', DELETE: 'danger' }
      return map[method] || 'info'
    },
    /**
     * 获取参数位置的标签类型
     */
    getPositionType(pos) {
      const map = {
        'path': 'success',
        'query': 'warning',
        'body': 'primary',
        'header': 'info'
      }
      return map[pos] || 'info'
    },
    /**
     * 获取格式化后的JSON字符串
     */
    getFormattedJson(obj) {
      if (!obj) return '{}'
      try {
        // 如果是字符串，先解析再格式化
        if (typeof obj === 'string') {
          try {
            return JSON.stringify(JSON.parse(obj), null, 2)
          } catch {
            return obj
          }
        }
        return JSON.stringify(obj, null, 2)
      } catch {
        return String(obj)
      }
    },
    /**
     * 格式化JSON字段
     */
    formatJsonField(api, field) {
      try {
        if (typeof api[field] === 'string') {
          api[field] = JSON.parse(api[field])
        }
        // 重新格式化
        const formatted = this.getFormattedJson(api[field])
        api[field] = JSON.parse(formatted)
        this.$message.success('JSON格式化成功')
      } catch (error) {
        this.$message.error('JSON格式错误，无法格式化')
      }
    },
    /**
     * 复制示例内容
     */
    copyExample(obj, name) {
      const text = this.getFormattedJson(obj)
      this.copyText(text, `${name}已复制到剪贴板`)
    },
    /**
     * 复制文本到剪贴板
     */
    copyText(text, successMessage) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text)
          .then(() => {
            this.$message.success(successMessage)
          })
          .catch(() => {
            this.fallbackCopy(text, successMessage)
          })
      } else {
        this.fallbackCopy(text, successMessage)
      }
    },
    /**
     * 备用复制方法
     */
    fallbackCopy(text, successMessage) {
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        document.execCommand('copy')
        this.$message.success(successMessage)
      } catch {
        this.$message.error('复制失败，请手动复制')
      }
      document.body.removeChild(textArea)
    },
    /**
     * 复制cURL命令
     */
    copyCurl(api) {
      let curl = `curl -X ${api.method} "${api.path}"`
      
      // 添加Content-Type头
      if (api.method === 'POST' || api.method === 'PUT') {
        curl += ' \\\n  -H "Content-Type: application/json"'
      }
      
      // 添加请求体
      if (api.reqExample) {
        const body = this.getFormattedJson(api.reqExample)
        // 处理换行和引号
        const escapedBody = body.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\n/g, '\\n')
        curl += ` \\\n  -d "${escapedBody}"`
      }
      
      this.copyText(curl, 'cURL命令已复制到剪贴板')
    },
    /**
     * 检查是否包含占位符
     */
    hasPlaceholders(obj) {
      const jsonStr = this.getFormattedJson(obj)
      const placeholderPatterns = [
        /占位符/i,
        /示例/i,
        /example/i,
        /{{.*}}/,
        /<.*>/
      ]
      return placeholderPatterns.some(pattern => pattern.test(jsonStr))
    },
    async getApiInfo() {
      this.loading = true
      this.error = null
      try {
        const res = await getAllApiInfo()
        if (res.code) {
          this.apis = res.data
        } else {
          this.error = res.msg || '获取接口信息失败'
          this.$message.error(this.error)
        }
      } catch {
        this.error = '网络错误，请检查网络连接'
        this.$message.error(this.error)
      } finally {
        this.loading = false
      }
    },
    testApi(api) {
      this.currentApi = api
      this.testBody = JSON.stringify(api.reqExample, null, 2)
      this.testResponse = null
      this.dialogVisible = true
    },
    async sendTest() {
      this.sendingTest = true
      this.testResponse = null
      try {
        const url = this.currentApi.path
        const method = this.currentApi.method.toLowerCase()
        let data = null
        
        // 尝试解析请求体
        if (this.testBody) {
          try {
            data = JSON.parse(this.testBody)
          } catch {
            this.$message.error('请求体格式错误，请检查JSON格式')
            return
          }
        }
        
        // 发送请求
        const response = await request({
          url: url,
          method: method,
          data: data
        })
        
        this.testResponse = response
        this.$message.success('测试请求成功')
      } catch (error) {
        console.error('测试请求失败:', error)
        this.testResponse = {
          error: error.message || '请求失败',
          status: error.response?.status || '未知状态',
          data: error.response?.data || null
        }
        this.$message.error('测试请求失败')
      } finally {
        this.sendingTest = false
      }
    },
    exportDoc() {
      // 提供导出选项
      this.$confirm('请选择导出格式', '导出文档', {
        confirmButtonText: 'JSON',
        cancelButtonText: 'Markdown',
        distinguishCancelAndClose: true
      }).then(() => {
        // 导出为JSON格式
        this.exportAsJson()
      }).catch(action => {
        if (action === 'cancel') {
          // 导出为Markdown格式
          this.exportAsMarkdown()
        }
      })
    },
    exportAsJson() {
      const exportData = {
        version: '1.0',
        exportTime: new Date().toISOString(),
        apis: this.apis
      }
      
      const jsonString = JSON.stringify(exportData, null, 2)
      this.downloadFile('api-document.json', jsonString, 'application/json')
    },
    exportAsMarkdown() {
      let markdown = '# 接口文档\n\n'
      markdown += `导出时间: ${new Date().toLocaleString()}\n\n`
      
      // 按分类组织文档
      const groups = this.groupedApis
      for (const category in groups) {
        markdown += `## ${category}\n\n`
        
        groups[category].forEach(api => {
          markdown += `### ${api.desc}\n`
          markdown += `- **路径**: ${api.path}\n`
          markdown += `- **方法**: ${api.method}\n`
          markdown += `- **状态**: ${api.status}\n\n`
          
          // 请求参数
          if (api.params && api.params.length > 0) {
            markdown += `#### 请求参数\n`
            markdown += `| 参数名 | 位置 | 类型 | 必填 | 描述 |\n`
            markdown += `| ------ | ---- | ---- | ---- | ---- |\n`
            
            api.params.forEach(param => {
              markdown += `| ${param.name} | ${param.pos} | ${param.type} | ${param.required ? '是' : '否'} | ${param.desc} |\n`
            })
            markdown += `\n`
          }
          
          // 示例
          markdown += `#### 示例\n`
          markdown += `##### 请求体\n`
          markdown += `\`\`\`json\n${JSON.stringify(api.reqExample, null, 2)}\n\`\`\`\n\n`
          markdown += `##### 响应\n`
          markdown += `\`\`\`json\n${JSON.stringify(api.resExample, null, 2)}\n\`\`\`\n\n`
        })
      }
      
      this.downloadFile('api-document.md', markdown, 'text/markdown')
    },
    downloadFile(filename, content, mimeType) {
      const blob = new Blob([content], { type: mimeType })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      link.click()
      URL.revokeObjectURL(url)
      this.$message.success('文档导出成功')
    },
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.getApiInfo()
      }, this.refreshInterval)
    },
    refreshApiInfo() {
      this.getApiInfo()
    }
  }
}
</script>

<style scoped>
.code-block {
  background: #f4f4f5;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  overflow-x: auto;
  overflow-y: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  border: 1px solid #e4e7ed;
  max-height: 300px;
  min-height: 100px;
  transition: all 0.3s ease;
  margin: 0;
}

.code-block-request {
  background: #f0f9ff;
  border-color: #b3d8ff;
}

.code-block-response {
  background: #f0fff4;
  border-color: #b7f0c3;
}

.code-block:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.api-detail {
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
  margin-top: 10px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-left: 3px solid #409eff;
  padding-left: 10px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.param-table {
  margin-bottom: 10px;
}

.param-name {
  font-weight: 600;
  color: #303133;
}

.param-type {
  color: #606266;
  font-family: monospace;
  background: #f4f4f5;
  padding: 2px 8px;
  border-radius: 3px;
}

.param-desc {
  line-height: 1.6;
}

.param-example {
  margin-top: 5px;
  font-size: 12px;
}

.example-label {
  color: #909399;
}

.example-value {
  color: #409eff;
  font-family: monospace;
  background: #ecf5ff;
  padding: 2px 6px;
  border-radius: 3px;
}

.example-code {
  background: #f4f4f5;
  padding: 3px 8px;
  border-radius: 3px;
  font-family: monospace;
  color: #409eff;
  font-size: 12px;
}

.example-container {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.example-header {
  background: #f5f7fa;
  padding: 12px 15px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.example-title {
  font-weight: 600;
  color: #303133;
}

.example-hint {
  font-size: 12px;
  color: #e6a23c;
}

.action-area {
  display: flex;
  gap: 10px;
}

.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.el-collapse-item__content {
  padding-bottom: 15px;
}

/* 接口分类卡片样式 */
.el-card__header {
  font-weight: bold;
  font-size: 16px;
  background: #f0f2f5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  /* 头部搜索区域堆叠显示 */
  :deep(.el-card__header) {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 15px;
  }

  :deep(.el-card__header) .el-input,
  :deep(.el-card__header) .el-select {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 10px;
  }

  :deep(.el-card__header) .el-button {
    width: 100%;
    margin-left: 0 !important;
    margin-right: 0 !important;
    float: none !important;
    margin-bottom: 10px;
  }

  /* 表格横向滚动 */
  .api-detail .el-table {
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  /* 代码示例区域 */
  .el-row {
    flex-direction: column;
  }

  .el-col-12 {
    width: 100%;
    max-width: 100%;
    flex: 0 0 100%;
    margin-bottom: 15px;
  }

  .code-block {
    font-size: 12px;
    padding: 10px;
  }

  /* 接口标题区域 */
  :deep(.el-collapse-item__header) {
    flex-wrap: wrap;
    height: auto;
    padding: 10px 0;
  }

  :deep(.el-collapse-item__header) .el-tag {
    margin-bottom: 5px;
  }

  /* 小屏幕下的参数表格 */
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .header-actions .el-button {
    flex: 1;
  }

  .action-area {
    flex-direction: column;
  }

  .action-area .el-button {
    width: 100%;
  }
}
</style>
