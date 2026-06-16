<template>
  <div class="timeline-page">
    <div class="page-header">
      <h2 class="page-title">⏱ 攻击时间线</h2>
      <div class="header-actions">
        <el-radio-group v-model="filterPhase" @change="load" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="DROPPER">Dropper</el-radio-button>
          <el-radio-button value="PERSIST">持久化</el-radio-button>
          <el-radio-button value="INJECT">注入</el-radio-button>
          <el-radio-button value="PHISH">钓鱼</el-radio-button>
          <el-radio-button value="HEARTBEAT">心跳</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 时间线统计 -->
    <el-row :gutter="14" style="margin-bottom:16px;">
      <el-col :span="4" v-for="s in phaseStats" :key="s.name">
        <div class="mini-stat">
          <div class="mini-num">{{ s.count }}</div>
          <div class="mini-label">{{ s.name }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="panel">
      <div class="panel-body" style="max-height:70vh;overflow-y:auto;">
        <el-timeline v-if="pagedEvents.length">
          <el-timeline-item
            v-for="event in pagedEvents" :key="event.id"
            :timestamp="event.timestamp"
            :type="phaseIcon(event.phase)"
            placement="top"
          >
            <div class="timeline-card">
              <div class="event-top">
                <el-tag :type="phaseTag(event.phase)" size="small" effect="dark">{{ event.phase }}</el-tag>
                <span class="event-action">{{ event.action }}</span>
              </div>
              <div v-if="event.detail && event.detail.length < 500" class="event-raw">
                {{ event.detail }}
              </div>
              <div v-else-if="event.detail" class="event-raw">
                {{ event.detail.substring(0, 500) }}...
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-if="!pagedEvents.length && !loading" description="暂无时间线数据，请先导入真实日志" />

        <div v-if="totalCount > pageSize" style="text-align:center;margin-top:16px;">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalCount"
            layout="prev, pager, next"
            small
            @current-change="pageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTimeline, getTimelineByPhase } from '../api'

const events = ref([])
const filterPhase = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(30)

const totalCount = computed(() => events.value.length)
const pagedEvents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return events.value.slice(start, start + pageSize.value)
})

const phaseStats = computed(() => {
  const phases = ['DROPPER', 'PERSIST', 'INJECT', 'PHISH', 'HEARTBEAT']
  return phases.map(p => ({
    name: p, count: events.value.filter(e => e.phase === p).length
  }))
})

const phaseTag = (p) => ({ DROPPER:'danger', PERSIST:'warning', INJECT:'', PHISH:'danger', HEARTBEAT:'info' }[p] || '')
const phaseIcon = (p) => ({ DROPPER:'danger', PERSIST:'warning', INJECT:'primary', PHISH:'danger', HEARTBEAT:'info' }[p] || 'primary')

const load = async () => {
  loading.value = true; currentPage.value = 1
  const res = filterPhase.value ? await getTimelineByPhase(filterPhase.value) : await getTimeline()
  events.value = res.data || []; loading.value = false
}
const pageChange = () => {}

onMounted(load)
</script>

<style scoped>
.page-title { color: #e8e8f8; margin: 0; font-size: 20px; }
.mini-stat {
  background: linear-gradient(135deg, #12122a 0%, #181830 100%);
  border: 1px solid rgba(255,255,255,0.05); border-radius: 8px;
  padding: 12px 16px; text-align: center;
}
.mini-num { font-size: 20px; font-weight: 800; color: #e8e8f8; }
.mini-label { color: #666; font-size: 11px; margin-top: 2px; }
.panel {
  background: linear-gradient(135deg, #12122a 0%, #181830 100%);
  border: 1px solid rgba(255,255,255,0.05); border-radius: 10px; overflow: hidden;
}
.panel-body { padding: 20px; }

.timeline-card {
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: 8px; padding: 12px 16px;
  margin-bottom: 6px;
  transition: all 0.15s;
}
.timeline-card:hover { background: rgba(255,255,255,0.04); border-color: rgba(255,255,255,0.08); }
.event-top { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.event-action { color: #ccc; font-size: 14px; }
.event-raw { margin-top: 8px; padding: 8px 12px; background: #060610; border-radius: 4px; color: #8890a0; font-size: 11px; font-family: 'Consolas',monospace; word-break: break-all; }
</style>
