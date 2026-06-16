<template>
  <div class="component-page">
    <h2 class="page-title">🧩 病毒组件关系图</h2>

    <el-row :gutter="16">
      <!-- 组件列表 -->
      <el-col :span="8">
        <el-card shadow="hover" class="comp-list-card">
          <template #header><span>📦 组件清单</span></template>
          <div v-for="comp in components" :key="comp.id"
            class="comp-item"
            @click="selected = comp"
            :class="{ active: selected?.id === comp.id }"
          >
            <div class="comp-name">{{ comp.name }}</div>
            <el-tag size="small" :type="roleTag(comp.role)">{{ comp.role }}</el-tag>
            <div class="comp-tech">{{ comp.techStack }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 组件详情 + 关系 -->
      <el-col :span="16">
        <el-card shadow="hover" v-if="selected" class="detail-card">
          <template #header>
            <span>{{ selected.name }}</span>
          </template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="角色">
              <el-tag size="small" :type="roleTag(selected.role)">{{ selected.role }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="技术栈">{{ selected.techStack }}</el-descriptions-item>
            <el-descriptions-item label="文件大小">
              {{ selected.fileSize ? (selected.fileSize / 1024).toFixed(1) + ' KB' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="攻击阶段">Phase {{ selected.attackPhase }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ selected.description }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 关系图容器 -->
        <el-card shadow="hover" style="margin-top: 12px;">
          <template #header><span>🔗 组件调用关系</span></template>
          <div ref="graphChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { getComponents, getComponentTree } from '../api'
import * as echarts from 'echarts'

const components = ref([])
const selected = ref(null)
const graphChart = ref(null)

const roleTag = (r) => ({ dropper: 'danger', controller: 'warning', injector: '', payload: 'info', c2: 'success' }[r] || '')

const renderGraph = async () => {
  if (!graphChart.value) return
  const chart = echarts.init(graphChart.value)

  const res = await getComponentTree()
  const tree = res.data || []

  // 将树转为 ECharts 力导向图数据
  const nodes = []
  const links = []
  const flatten = (list, parentId) => {
    list.forEach(item => {
      const id = String(item.id)
      nodes.push({ id, name: item.name, category: item.role === 'dropper' ? 0 : item.role === 'controller' ? 1 : item.role === 'injector' ? 2 : item.role === 'c2' ? 3 : 4, symbolSize: 40 })
      if (parentId) links.push({ source: String(parentId), target: id })
      if (item.children) flatten(item.children, item.id)
    })
  }
  flatten(tree, null)

  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      categories: [
        { name: 'Dropper', itemStyle: { color: '#ff6b6b' } },
        { name: '控制器', itemStyle: { color: '#ffa502' } },
        { name: '注入器', itemStyle: { color: '#7bed9f' } },
        { name: 'C2', itemStyle: { color: '#70a1ff' } },
        { name: '载荷', itemStyle: { color: '#a29bfe' } }
      ],
      data: nodes,
      links: links,
      force: { repulsion: 200, edgeLength: [150, 300] },
      label: { show: true, color: '#bbb', fontSize: 11 }
    }]
  })
}

onMounted(async () => {
  const res = await getComponents()
  components.value = res.data || []
  await nextTick()
  renderGraph()
})

watch(selected, () => nextTick(renderGraph))
</script>

<style scoped>
.page-title { color: #e0e0f0; margin-bottom: 20px; }
.comp-list-card { background: #1a1a2e; border: 1px solid #2a2a3e; max-height: 600px; overflow-y: auto; }
.comp-item { padding: 10px 12px; cursor: pointer; border-radius: 6px; margin-bottom: 4px; border: 1px solid transparent; }
.comp-item:hover { background: #252540; }
.comp-item.active { border-color: #00d4ff; background: #1e2a3a; }
.comp-name { color: #e0e0f0; font-weight: 600; margin-bottom: 4px; }
.comp-tech { color: #888; font-size: 11px; margin-top: 4px; }
.detail-card { background: #1a1a2e; border: 1px solid #2a2a3e; }
</style>
