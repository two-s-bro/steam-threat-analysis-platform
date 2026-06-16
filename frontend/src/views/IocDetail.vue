<template>
  <div class="ioc-detail" v-if="ioc">
    <el-page-header @back="$router.push('/iocs')" title="返回 IOC 库">
      <template #content>
        <span style="color: #e0e0f0;">IOC 详情 #{{ ioc.id }}</span>
      </template>
    </el-page-header>

    <el-card class="detail-card" style="margin-top: 16px;">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="IOC 类型">
          <el-tag>{{ ioc.iocType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <span :class="'risk-' + ioc.riskLevel" style="font-weight: 700;">{{ ioc.riskLevel }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="IOC 值" :span="2">
          <code style="color: #ff6b6b; background: #1a1a2e; padding: 4px 8px; border-radius: 4px;">
            {{ ioc.iocValue }}
          </code>
        </el-descriptions-item>
        <el-descriptions-item label="来源文件">{{ ioc.sourceFile }}</el-descriptions-item>
        <el-descriptions-item label="攻击阶段">Phase {{ ioc.attackPhase }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ ioc.description }}</el-descriptions-item>
        <el-descriptions-item label="录入时间">{{ ioc.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getIocById } from '../api'

const route = useRoute()
const ioc = ref(null)

onMounted(async () => {
  const res = await getIocById(route.params.id)
  if (res.code === 200) ioc.value = res.data
})
</script>

<style scoped>
.page-title { color: #e0e0f0; }
.detail-card { background: #1a1a2e; border: 1px solid #2a2a3e; }
.risk-HIGH { color: #ff6b6b; }
.risk-MEDIUM { color: #ffa502; }
.risk-LOW { color: #7bed9f; }
</style>
