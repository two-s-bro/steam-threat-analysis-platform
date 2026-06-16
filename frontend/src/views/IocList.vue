<template>
  <div class="ioc-list">
    <div class="page-header">
      <h2 class="page-title">🔗 IOC 威胁情报库</h2>
      <el-button type="primary" @click="showAdd = true">
        <el-icon><Plus /></el-icon> 录入 IOC
      </el-button>
    </div>

    <!-- 搜索 + 筛选 -->
    <el-row :gutter="12" class="filter-row">
      <el-col :span="8">
        <el-input v-model="keyword" placeholder="搜索 IOC 值..." clearable
          @input="doSearch" prefix-icon="Search" />
      </el-col>
      <el-col :span="4">
        <el-select v-model="filterType" placeholder="类型筛选" clearable @change="doFilter">
          <el-option label="域名" value="domain" />
          <el-option label="文件路径" value="file_path" />
          <el-option label="注册表键" value="registry_key" />
          <el-option label="SteamID" value="steamid" />
          <el-option label="案件编号" value="case_number" />
        </el-select>
      </el-col>
    </el-row>

    <!-- IOC 表格 -->
    <el-table :data="iocs" style="width: 100%; margin-top: 12px;" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="iocType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.iocType)">{{ row.iocType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="iocValue" label="IOC 值" min-width="260" show-overflow-tooltip />
      <el-table-column prop="riskLevel" label="风险" width="80">
        <template #default="{ row }">
          <span :class="'risk-' + row.riskLevel">{{ row.riskLevel }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sourceFile" label="来源文件" width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/iocs/${row.id}`)">详情</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 IOC 弹窗 -->
    <el-dialog v-model="showAdd" title="录入新 IOC" width="500px">
      <el-form :model="newIoc" label-width="100px">
        <el-form-item label="IOC 类型">
          <el-select v-model="newIoc.iocType">
            <el-option label="域名" value="domain" />
            <el-option label="文件路径" value="file_path" />
            <el-option label="注册表键" value="registry_key" />
            <el-option label="SteamID" value="steamid" />
            <el-option label="案件编号" value="case_number" />
          </el-select>
        </el-form-item>
        <el-form-item label="IOC 值">
          <el-input v-model="newIoc.iocValue" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-radio-group v-model="newIoc.riskLevel">
            <el-radio value="HIGH">高危</el-radio>
            <el-radio value="MEDIUM">中危</el-radio>
            <el-radio value="LOW">低危</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="来源文件">
          <el-input v-model="newIoc.sourceFile" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newIoc.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="攻击阶段">
          <el-input-number v-model="newIoc.attackPhase" :min="0" :max="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认录入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getIocs, createIoc, deleteIoc, searchIocs } from '../api'

const iocs = ref([])
const loading = ref(false)
const showAdd = ref(false)
const keyword = ref('')
const filterType = ref('')
const newIoc = ref({ iocType: 'domain', riskLevel: 'HIGH', attackPhase: 0 })

const typeTag = (t) => ({ domain: '', file_path: 'warning', registry_key: 'danger', steamid: 'info', case_number: 'success' })[t] || ''

const loadIocs = async () => {
  loading.value = true
  const res = await getIocs()
  iocs.value = res.data || []
  loading.value = false
}

const doSearch = async () => {
  if (!keyword.value) return loadIocs()
  const res = await searchIocs(keyword.value)
  iocs.value = res.data || []
}

const doFilter = () => {
  loadIocs().then(() => {
    if (filterType.value) iocs.value = iocs.value.filter(i => i.iocType === filterType.value)
  })
}

const handleAdd = async () => {
  await createIoc(newIoc.value)
  showAdd.value = false
  newIoc.value = { iocType: 'domain', riskLevel: 'HIGH', attackPhase: 0 }
  loadIocs()
}

const handleDelete = async (id) => {
  await deleteIoc(id)
  loadIocs()
}

onMounted(loadIocs)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { color: #e0e0f0; margin: 0; }
.filter-row { margin-top: 12px; }
.risk-HIGH { color: #ff6b6b; font-weight: 700; }
.risk-MEDIUM { color: #ffa502; font-weight: 700; }
.risk-LOW { color: #7bed9f; }
</style>
