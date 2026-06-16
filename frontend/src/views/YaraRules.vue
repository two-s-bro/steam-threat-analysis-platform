<template>
  <div class="yara-page">
    <div class="page-header">
      <h2 class="page-title">🎯 YARA 规则引擎</h2>
      <div class="header-actions">
        <el-button size="small" type="primary" @click="showCreate = true">
          <el-icon><Plus /></el-icon> 新建规则
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" style="margin-bottom:14px;">
      基于真实 Steam 劫持病毒特征编写的 YARA 检测规则。可在下方测试区输入文件内容进行模拟匹配。
    </el-alert>

    <!-- 规则列表 -->
    <div class="panel" style="margin-bottom:14px;">
      <div class="panel-header">
        <span>📋 规则列表 ({{ rules.length }} 条)</span>
        <el-input v-model="filterKeyword" placeholder="搜索规则..." size="small" style="width:200px;" clearable prefix-icon="Search" />
      </div>
      <el-table :data="filteredRules" stripe v-loading="loading" size="small"
        :header-cell-style="{background:'#12122a',color:'#888',borderColor:'rgba(255,255,255,0.04)'}"
        :cell-style="{borderColor:'rgba(255,255,255,0.03)'}">
        <el-table-column prop="ruleName" label="规则名称" min-width="220">
          <template #default="{ row }">
            <span style="color:#00d4ff;font-weight:600;font-size:13px;">{{ row.ruleName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="dark" :type="targetTag(row.targetType)">{{ row.targetType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险" width="70">
          <template #default="{ row }">
            <span :class="'risk-' + row.riskLevel" style="font-weight:700;">{{ row.riskLevel }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="matchCount" label="命中" width="60" align="center" />
        <el-table-column prop="enabled" label="状态" width="70">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" size="small" @change="toggleRule(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="viewRule(row)">查看</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 匹配测试区 -->
    <div class="panel">
      <div class="panel-header"><span>🧪 YARA 匹配测试</span></div>
      <div class="panel-body">
        <el-input v-model="testContent" type="textarea" :rows="4"
          placeholder="在此粘贴要检测的文本内容（如：日志片段、注册表导出、文件内容）..."
          style="margin-bottom:10px;" />
        <el-button type="primary" @click="runMatch" :loading="matching">
          <el-icon><Aim /></el-icon> 运行匹配
        </el-button>

        <!-- 匹配结果 -->
        <div v-if="matchResults !== null" style="margin-top:14px;">
          <el-divider />
          <div v-if="matchResults.length === 0" style="color:#00e676;padding:20px;text-align:center;">
            ✅ 未命中任何规则 — 样本安全
          </div>
          <div v-else>
            <el-alert type="error" :closable="false" style="margin-bottom:10px;">
              命中 {{ matchResults.length }} 条规则！
            </el-alert>
            <div v-for="(r, i) in matchResults" :key="i" class="match-item">
              <div class="match-header">
                <el-tag type="danger" size="small">{{ r.ruleName }}</el-tag>
                <span class="risk-badge" :class="'risk-' + r.riskLevel">{{ r.riskLevel }}</span>
              </div>
              <div class="match-patterns">
                命中模式: <code v-for="p in r.matchedPatterns" :key="p" class="match-code">{{ p }}</code>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 新建/编辑弹窗 ========== -->
    <el-dialog v-model="showCreate" title="新建 YARA 规则" width="640px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="规则名称">
          <el-input v-model="form.ruleName" placeholder="SteamHijack_XXX" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="风险等级">
              <el-select v-model="form.riskLevel"><el-option v-for="l in ['HIGH','MEDIUM','LOW']" :key="l" :label="l" :value="l" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标类型">
              <el-select v-model="form.targetType"><el-option v-for="t in ['file','registry','url','memory']" :key="t" :label="t" :value="t" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规则体">
          <el-input v-model="form.ruleBody" type="textarea" :rows="10"
            placeholder="rule MyRule { strings: $s1 = &quot;malicious&quot; condition: $s1 }"
            class="code-input" />
        </el-form-item>
        <el-form-item label="匹配样本">
          <el-input v-model="form.matchSample" placeholder="该规则用于匹配的典型样本" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">保存规则</el-button>
      </template>
    </el-dialog>

    <!-- 查看规则详情弹窗 -->
    <el-dialog v-model="showDetail" title="YARA 规则详情" width="640px">
      <div v-if="detailRule">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="名称">{{ detailRule.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="目标">{{ detailRule.targetType }}</el-descriptions-item>
          <el-descriptions-item label="风险">{{ detailRule.riskLevel }}</el-descriptions-item>
          <el-descriptions-item label="命中次数">{{ detailRule.matchCount }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailRule.description }}</el-descriptions-item>
          <el-descriptions-item label="样本" :span="2">{{ detailRule.matchSample }}</el-descriptions-item>
        </el-descriptions>
        <pre class="rule-preview">{{ detailRule.ruleBody }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getYaraRules, createYaraRule, deleteYaraRule, updateYaraRule, matchYara } from '../api'
import { Plus, Aim, Search } from '@element-plus/icons-vue'

const rules = ref([])
const loading = ref(false)
const filterKeyword = ref('')
const showCreate = ref(false)
const showDetail = ref(false)
const detailRule = ref(null)
const testContent = ref('')
const matchResults = ref(null)
const matching = ref(false)
const form = ref({ ruleName: '', description: '', riskLevel: 'HIGH', targetType: 'file', ruleBody: '', matchSample: '', enabled: true })

const filteredRules = computed(() => {
  if (!filterKeyword.value) return rules.value
  const kw = filterKeyword.value.toLowerCase()
  return rules.value.filter(r => r.ruleName.toLowerCase().includes(kw) || r.description?.toLowerCase().includes(kw))
})

const targetTag = (t) => ({ file: 'primary', registry: 'danger', url: 'warning', memory: 'success' }[t] || '')
const riskLevel = (r) => r

const loadRules = async () => { loading.value = true; const res = await getYaraRules(); rules.value = res.data || []; loading.value = false }

const viewRule = (r) => { detailRule.value = r; showDetail.value = true }
const toggleRule = async (r) => { await updateYaraRule(r.id, r) }
const handleDelete = async (id) => { await deleteYaraRule(id); loadRules() }
const handleCreate = async () => { await createYaraRule(form.value); showCreate.value = false; form.value = { ruleName: '', description: '', riskLevel: 'HIGH', targetType: 'file', ruleBody: '', matchSample: '', enabled: true }; loadRules() }

const runMatch = async () => {
  if (!testContent.value.trim()) return
  matching.value = true
  const res = await matchYara(testContent.value)
  matchResults.value = res.data || []
  matching.value = false
}

onMounted(loadRules)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.page-title { color: #e8e8f8; margin: 0; font-size: 20px; }
.panel {
  background: linear-gradient(135deg, #12122a 0%, #181830 100%);
  border: 1px solid rgba(255,255,255,0.05); border-radius: 10px; overflow: hidden;
}
.panel-header {
  padding: 14px 18px; border-bottom: 1px solid rgba(255,255,255,0.05);
  color: #ccc; font-size: 14px; font-weight: 600;
  display: flex; justify-content: space-between; align-items: center;
}
.panel-body { padding: 16px; }
.risk-HIGH { color: #ff5e5e; }
.risk-MEDIUM { color: #ffa502; }
.risk-LOW { color: #00e676; }
.match-item {
  padding: 12px; margin-bottom: 8px;
  background: rgba(255,94,94,0.06); border: 1px solid rgba(255,94,94,0.15); border-radius: 6px;
}
.match-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.match-patterns { color: #aaa; font-size: 12px; }
.match-code { color: #ffa502; background: rgba(255,165,2,0.1); padding: 1px 6px; border-radius: 3px; margin: 0 2px; font-size: 11px; }
.rule-preview {
  background: #0a0a16; color: #7bed9f; padding: 16px; border-radius: 6px; margin-top: 12px;
  font-family: 'Consolas','Courier New',monospace; font-size: 12px; line-height: 1.6;
  max-height: 300px; overflow-y: auto; white-space: pre-wrap;
}
.code-input :deep(textarea) {
  font-family: 'Consolas','Courier New',monospace; font-size: 12px;
  background: #0a0a16; color: #7bed9f;
}
</style>
