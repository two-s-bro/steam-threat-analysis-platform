<template>
  <div class="log-viewer">
    <div class="page-header">
      <h2 class="page-title">📄 病毒日志查看器</h2>
      <div class="header-actions">
        <el-button size="small" type="success" @click="importRealLogs" :loading="importing">
          <el-icon><Upload /></el-icon> 导入真实日志文件
        </el-button>
      </div>
    </div>

    <!-- 导入结果 -->
    <div v-if="importResult" style="margin-bottom:14px;">
      <el-alert :title="importResult" type="success" closable @close="importResult = null" />
    </div>

    <!-- 日志统计 -->
    <el-row :gutter="14" style="margin-bottom:14px;">
      <el-col :span="6" v-for="stat in logStats" :key="stat.label">
        <div class="mini-stat">
          <div class="mini-num">{{ stat.value }}</div>
          <div class="mini-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" type="border-card" class="log-tabs">
      <!-- ===== 补丁日志 ===== -->
      <el-tab-pane label="补丁操作日志" name="patch">
        <template #label>
          <span>📝 补丁操作日志 <el-tag size="small" effect="dark" type="warning">{{ patchCount }}</el-tag></span>
        </template>
        <div class="log-toolbar">
          <el-input v-model="patchFilter" placeholder="搜索日志..." size="small" style="width:260px;" clearable />
          <span class="log-meta">文件: SteamPatchToast_patch.lo | 2517行 | 从真实病毒提取</span>
        </div>
        <div class="log-box">
          <div v-for="(line, i) in filteredPatchLines" :key="i" class="log-line" :class="logLineClass(line)">
            <span class="line-num">{{ patches.indexOf(line) + 1 }}</span>
            <span>{{ line.length > 300 ? line.substring(0, 300) + '...' : line }}</span>
          </div>
        </div>
      </el-tab-pane>

      <!-- ===== C2 通信日志 ===== -->
      <el-tab-pane label="C2 通信日志" name="download">
        <template #label>
          <span>📡 C2 通信日志 <el-tag size="small" effect="dark" type="info">{{ dlCount }}</el-tag></span>
        </template>
        <div class="log-toolbar">
          <span class="log-meta">文件: _downloadlog/76561198892973993.txt | 心跳间隔 ~3.5分钟</span>
        </div>
        <div class="log-box">
          <div v-for="(line, i) in downloadLines" :key="i" class="log-line c2-log">
            <span class="line-num">{{ i + 1 }}</span>
            <span>{{ line }}</span>
          </div>
        </div>
      </el-tab-pane>

      <!-- ===== 启动日志 ===== -->
      <el-tab-pane label="启动摘要" name="startup">
        <template #label>
          <span>🚀 启动摘要 <el-tag size="small" effect="dark" type="success">{{ startupCount }}</el-tag></span>
        </template>
        <div class="log-toolbar">
          <span class="log-meta">文件: SteamPatchToast_startup.lo | 19行</span>
        </div>
        <div class="log-box">
          <div v-for="(line, i) in startupLines" :key="i" class="log-line startup-log">
            <span class="line-num">{{ i + 1 }}</span>
            <span>{{ line }}</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTimeline, importLogs } from '../api'
import { Upload } from '@element-plus/icons-vue'

const activeTab = ref('patch')
const importing = ref(false)
const importResult = ref(null)

// 从后端已导入的时间线中提取日志行
const patchCount = ref(0)
const dlCount = ref(0)
const startupCount = ref(0)
const patches = ref([])
const downloadLines = ref([])
const startupLines = ref([])
const patchFilter = ref('')

const logStats = computed(() => [
  { label: '补丁日志行数', value: patchCount.value },
  { label: 'C2 通信记录', value: dlCount.value },
  { label: '启动记录', value: startupCount.value },
  { label: '总计', value: patchCount.value + dlCount.value + startupCount.value }
])

const filteredPatchLines = computed(() => {
  if (!patchFilter.value) return patches.value.slice(0, 300)
  const kw = patchFilter.value.toLowerCase()
  return patches.value.filter(l => l.toLowerCase().includes(kw))
})

const logLineClass = (line) => {
  if (line.includes('nexustechsolution') || line.includes('force kill')) return 'danger'
  if (line.includes('patch:') || line.includes('inject') || line.includes('cfg')) return 'warn'
  if (line.includes('watch:') || line.includes('heartbeat')) return 'info'
  return ''
}

const loadFromTimeline = async () => {
  const res = await getTimeline()
  const events = res.data || []
  // 从时间线事件中重建日志行
  const pLines = []
  const dLines = []
  const sLines = []
  events.forEach(e => {
    if (e.phase === 'HEARTBEAT' && e.detail) {
      dLines.push(e.detail)
    } else if (e.detail && e.detail.startsWith('[')) {
      sLines.push(e.detail)
    } else if (e.detail) {
      pLines.push(e.detail)
    }
  })
  patches.value = pLines
  downloadLines.value = dLines
  startupLines.value = sLines
  patchCount.value = pLines.length
  dlCount.value = dLines.length
  startupCount.value = sLines.length
}

const importRealLogs = async () => {
  importing.value = true
  try {
    const res = await importLogs()
    importResult.value = res.message || '导入完成! ' + JSON.stringify(res.data)
    // 延迟后重新加载
    setTimeout(async () => { await loadFromTimeline() }, 1500)
  } catch (e) {
    importResult.value = '导入失败: ' + e.message
  }
  importing.value = false
}

onMounted(loadFromTimeline)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.page-title { color: #e8e8f8; margin: 0; font-size: 20px; }
.mini-stat {
  background: linear-gradient(135deg, #12122a 0%, #181830 100%);
  border: 1px solid rgba(255,255,255,0.05); border-radius: 8px;
  padding: 14px 18px; text-align: center;
}
.mini-num { font-size: 22px; font-weight: 800; color: #e8e8f8; }
.mini-label { color: #666; font-size: 11px; margin-top: 2px; }
.log-tabs {
  background: transparent; border-radius: 10px; overflow: hidden;
  --el-border-color: rgba(255,255,255,0.05);
}
.log-toolbar {
  padding: 10px 16px; display: flex; align-items: center; gap: 16px;
  background: #0d0d20; border-bottom: 1px solid rgba(255,255,255,0.03);
}
.log-meta { color: #555; font-size: 11px; }
.log-box {
  background: #060610; max-height: 520px; overflow-y: auto; padding: 12px 16px;
  font-family: 'Consolas','Courier New',monospace; font-size: 12px; line-height: 1.7;
}
.log-line { color: #8890a0; padding: 1px 0; }
.log-line.danger { color: #ff5e5e; background: rgba(255,94,94,0.04); }
.log-line.warn { color: #ffa502; }
.log-line.info { color: #5ea3ff; }
.log-line.c2-log { color: #00e676; }
.log-line.startup-log { color: #c084fc; }
.line-num { color: #333; margin-right: 10px; user-select: none; min-width: 36px; display: inline-block; text-align: right; font-size: 10px; }
</style>
