<template>
  <div class="dashboard">
    <div class="page-header">
      <h2 class="page-title">📊 威胁分析仪表盘</h2>
      <div class="header-actions">
        <el-button size="small" type="primary" @click="refreshAll" :loading="loading">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <el-row :gutter="14" class="stats-row">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <div class="stat-card" :style="{ borderTopColor: card.color }">
          <div class="stat-icon" :style="{ color: card.color }">{{ card.icon }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== C2 实时监控 + 攻击链 ===== -->
    <el-row :gutter="14" style="margin-top: 14px;">
      <!-- C2 实时监控卡片 -->
      <el-col :span="8">
        <div class="panel">
          <div class="panel-header">
            <span>🛰️ C2 历史状态（离线分析模式）</span>
            <el-tag type="success" size="small" effect="dark">安全模式</el-tag>
          </div>
          <div class="panel-body c2-panel">
            <div v-for="target in c2Targets" :key="target.label" class="c2-target-row">
              <div class="c2-target-info">
                <span class="c2-dot c2-down"></span>
                <div>
                  <div class="c2-label">{{ target.label }}</div>
                  <div class="c2-url">{{ target.host }}:{{ target.port }}</div>
                </div>
              </div>
              <div class="c2-metrics">
                <span class="text-muted">已断联</span>
                <div class="c2-time" style="color:#00e676;">不会发起外部连接</div>
              </div>
            </div>
            <!-- 感染历史统计 -->
            <div class="c2-history" v-if="c2History.firstHeartbeat">
              <el-divider style="margin:8px 0;" />
              <div class="history-grid">
                <div class="history-item">
                  <div class="history-num">{{ c2History.totalHeartbeats }}</div>
                  <div class="history-label">心跳总数</div>
                </div>
                <div class="history-item">
                  <div class="history-num">{{ c2History.durationHours }}h</div>
                  <div class="history-label">感染时长</div>
                </div>
              </div>
              <div class="history-time">⏱ 首次: {{ c2History.firstHeartbeat?.substring(0,19) || '-' }}</div>
              <div class="history-time">⏱ 最后: {{ c2History.lastHeartbeat?.substring(0,19) || '-' }}</div>
            </div>
            <!-- 心跳折线图 -->
            <div ref="c2Chart" style="height:120px; margin-top:8px;"></div>
            <el-button size="small" text @click="refreshC2" style="margin-top:6px;width:100%;">
              <el-icon><Refresh /></el-icon> 从日志刷新
            </el-button>
          </div>
        </div>
      </el-col>

      <!-- 攻击链 -->
      <el-col :span="16">
        <div class="panel">
          <div class="panel-header"><span>⚔️ 攻击链路全景 (Phase 0 → 3)</span></div>
          <div class="panel-body">
            <AttackChain />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== IOC 分布 + 最近事件 ===== -->
    <el-row :gutter="14" style="margin-top: 14px;">
      <el-col :span="8">
        <div class="panel">
          <div class="panel-header"><span>🎯 IOC 风险分布</span></div>
          <div class="panel-body"><div ref="riskChart" style="height:240px;"></div></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel">
          <div class="panel-header"><span>📦 阶段事件分布</span></div>
          <div class="panel-body"><div ref="phaseChart" style="height:240px;"></div></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel">
          <div class="panel-header"><span>⏱ 最近威胁事件</span></div>
          <div class="panel-body recent-events">
            <div v-for="(ev, i) in recentEvents" :key="i" class="recent-item">
              <el-tag :type="phaseTag(ev.phase)" size="small" effect="dark">{{ ev.phase }}</el-tag>
              <span class="recent-action">{{ ev.action }}</span>
              <span class="recent-time">{{ ev.timestamp ? ev.timestamp.substring(11,19) : '' }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { getDashboard, getC2Status, refreshC2FromLogs } from '../api'
import AttackChain from '../components/AttackChain.vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const dashboard = ref({})
const c2Targets = ref([])
const c2Events = ref([])
const c2History = ref({})
const riskChart = ref(null)
const phaseChart = ref(null)
const c2Chart = ref(null)

const statCards = computed(() => {
  const d = dashboard.value
  return [
    { icon: '⚠️', label: 'IOC 威胁指标', value: d.totalIocs || 0, color: '#ff5e5e' },
    { icon: '🔴', label: '高危 IOC', value: d.highRiskCount || 0, color: '#ee5a24' },
    { icon: '📋', label: '攻击事件', value: d.totalTimelineEvents || 0, color: '#f9a825' },
    { icon: '🧩', label: '病毒组件', value: d.totalComponents || 0, color: '#00e676' },
    { icon: '🛡️', label: 'YARA 规则', value: 10, color: '#448aff' },
    { icon: '🔗', label: 'C2 监控', value: c2Targets.value.length + '个', color: '#e040fb' }
  ]
})

const c2AnyOnline = computed(() => c2Targets.value.some(t => t.online))
const recentEvents = computed(() => dashboard.value.recentEvents || [])
const phaseTag = (p) => ({ DROPPER: 'danger', PERSIST: 'warning', INJECT: '', PHISH: 'danger', HEARTBEAT: 'info' }[p] || '')

const loadData = async () => {
  loading.value = true
  try {
    const [dashRes, c2Res] = await Promise.all([getDashboard(), getC2Status()])
    dashboard.value = dashRes.data?.dashboard || {}
    c2Targets.value = dashRes.data?.c2Targets || c2Res.data?.targets || []
    c2Events.value = dashRes.data?.c2Events || c2Res.data?.recentEvents || []
    c2History.value = c2Res.data?.history || dashRes.data?.c2History || {}
  } catch (e) { console.error(e) }
  loading.value = false
  await nextTick()
  renderCharts()
}

const refreshAll = () => { loadData() }
const refreshC2 = async () => {
  const res = await refreshC2FromLogs()
  c2Targets.value = res.data?.targets || []
  c2Events.value = res.data?.recentEvents || []
  c2History.value = res.data?.history || {}
  await nextTick(); renderC2Chart()
}

const renderCharts = () => {
  renderRiskChart()
  renderPhaseChart()
  renderC2Chart()
}

const renderRiskChart = () => {
  if (!riskChart.value) return
  const chart = echarts.init(riskChart.value)
  const stats = dashboard.value.riskLevelStats || {}
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['50%', '78%'], center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: { show: true, position: 'outside', color: '#888', formatter: '{b}\n{c}个' },
      emphasis: { label: { fontSize: 16, fontWeight: 'bold' } },
      data: [
        { value: stats.HIGH || 0, name: '高危', itemStyle: { color: '#ff5e5e' } },
        { value: stats.MEDIUM || 0, name: '中危', itemStyle: { color: '#ffa502' } },
        { value: stats.LOW || 0, name: '低危', itemStyle: { color: '#00e676' } }
      ]
    }]
  })
}

const renderPhaseChart = () => {
  if (!phaseChart.value) return
  const chart = echarts.init(phaseChart.value)
  const stats = dashboard.value.phaseStats || {}
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: Object.keys(stats), axisLabel: { color: '#888', fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#1a1a2e' } }, axisLabel: { color: '#666' } },
    series: [{
      type: 'bar', data: Object.values(stats),
      itemStyle: { borderRadius: [4,4,0,0], color: new echarts.graphic.LinearGradient(0,0,0,1,[
        {offset:0, color:'#448aff'}, {offset:1, color:'#0066ff'}
      ]) }
    }]
  })
}

const renderC2Chart = () => {
  if (!c2Chart.value) return
  const chart = echarts.init(c2Chart.value)
  const events = c2Events.value.slice().reverse()
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '3%', top: '5%', bottom: '3%' },
    xAxis: { show: false, data: events.map(e => e.time?.substring(11,19) || '') },
    yAxis: { show: false, min: 0, max: 1 },
    series: [{
      type: 'line', step: 'end', data: events.map(e => e.online ? 1 : 0),
      lineStyle: { color: '#ff5e5e', width: 1.5 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[
        {offset:0, color:'rgba(255,94,94,0.3)'}, {offset:1, color:'rgba(255,94,94,0)'}
      ]) },
      symbol: 'none'
    }]
  })
}

onMounted(loadData)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.page-title { color: #e8e8f8; margin: 0; font-size: 20px; }
.stat-card {
  background: linear-gradient(135deg, #12122a 0%, #181830 100%);
  border: 1px solid rgba(255,255,255,0.05);
  border-top: 3px solid #00d4ff;
  border-radius: 10px; padding: 18px 14px;
  text-align: center; transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(0,0,0,0.3); }
.stat-icon { font-size: 22px; margin-bottom: 6px; }
.stat-value { font-size: 26px; font-weight: 800; color: #e8e8f8; margin-bottom: 2px; }
.stat-label { color: #666; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; }

.panel {
  background: linear-gradient(135deg, #12122a 0%, #181830 100%);
  border: 1px solid rgba(255,255,255,0.05); border-radius: 10px;
  margin-bottom: 14px; overflow: hidden;
}
.panel-header {
  padding: 14px 18px; border-bottom: 1px solid rgba(255,255,255,0.05);
  color: #ccc; font-size: 14px; font-weight: 600;
  display: flex; justify-content: space-between; align-items: center;
}
.panel-body { padding: 16px; }

.c2-panel { padding: 12px 16px; }
.c2-target-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 12px; margin-bottom: 6px;
  background: rgba(255,255,255,0.02); border-radius: 6px;
}
.c2-target-info { display: flex; align-items: center; gap: 10px; }
.c2-dot { width: 10px; height: 10px; border-radius: 50%; }
.c2-dot.c2-up { background: #ff5e5e; box-shadow: 0 0 10px #ff5e5e88; animation: pulse 1.5s infinite; }
.c2-dot.c2-down { background: #00ff88; }
.c2-label { color: #ccc; font-size: 13px; font-weight: 600; }
.c2-url { color: #555; font-size: 11px; }
.c2-metrics { text-align: right; }
.c2-time { color: #555; font-size: 10px; margin-top: 2px; }

@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
.text-danger { color: #ff5e5e; }
.text-muted { color: #555; }

.recent-events { max-height: 240px; overflow-y: auto; padding: 6px 12px; }
.recent-item {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 0; border-bottom: 1px solid rgba(255,255,255,0.03);
  font-size: 12px;
}
.recent-action { flex: 1; color: #aaa; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.recent-time { color: #555; font-size: 11px; white-space: nowrap; }

.c2-history { padding: 4px 0; }
.history-grid { display: flex; gap: 12px; margin: 6px 0; }
.history-item {
  flex: 1; text-align: center; padding: 8px;
  background: rgba(255,255,255,0.02); border-radius: 6px;
}
.history-num { font-size: 18px; font-weight: 800; color: #e8e8f8; }
.history-label { font-size: 10px; color: #666; margin-top: 2px; }
.history-time { color: #555; font-size: 10px; padding: 2px 0; }
</style>
