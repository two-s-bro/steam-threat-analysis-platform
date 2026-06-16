import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 15000 })

api.interceptors.response.use(
  res => res.data,
  err => { console.error('API 请求失败:', err); return Promise.reject(err) }
)

// ============ Dashboard ============
export const getDashboard = () => api.get('/dashboard')
export const searchAll = (keyword) => api.get('/dashboard/search', { params: { keyword } })

// ============ IOC ============
export const getIocs = () => api.get('/iocs')
export const getIocById = (id) => api.get(`/iocs/${id}`)
export const createIoc = (data) => api.post('/iocs', data)
export const updateIoc = (id, data) => api.put(`/iocs/${id}`, data)
export const deleteIoc = (id) => api.delete(`/iocs/${id}`)
export const searchIocs = (keyword) => api.get('/iocs/search', { params: { keyword } })

// ============ Timeline ============
export const getTimeline = () => api.get('/timeline')
export const getTimelineByPhase = (phase) => api.get(`/timeline/phase/${phase}`)

// ============ Components ============
export const getComponents = () => api.get('/components')
export const getComponentTree = () => api.get('/components/tree')

// ============ System ============
export const getC2Status = () => api.get('/system/c2-status')
export const refreshC2FromLogs = () => api.post('/system/c2-refresh')
export const importLogs = () => api.post('/system/import-logs')

// ============ YARA ============
export const getYaraRules = () => api.get('/yara')
export const getYaraRuleById = (id) => api.get(`/yara/${id}`)
export const createYaraRule = (data) => api.post('/yara', data)
export const updateYaraRule = (id, data) => api.put(`/yara/${id}`, data)
export const deleteYaraRule = (id) => api.delete(`/yara/${id}`)
export const matchYara = (content) => api.post('/yara/match', { content })

export default api
