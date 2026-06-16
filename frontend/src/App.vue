<template>
  <div id="app-container">
    <!-- 粒子背景 -->
    <div class="particles-bg"></div>

    <el-container>
      <el-aside width="230px" class="app-aside">
        <div class="logo" @click="$router.push('/dashboard')">
          <div class="logo-icon-box">🔬</div>
          <div>
            <div class="logo-text">Steam 威胁分析</div>
            <div class="logo-sub">Threat Analysis Platform</div>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="#8890a8"
          active-text-color="#00d4ff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/iocs">
            <el-icon><WarningFilled /></el-icon>
            <span>IOC 情报库</span>
          </el-menu-item>
          <el-menu-item index="/timeline">
            <el-icon><Timer /></el-icon>
            <span>攻击时间线</span>
          </el-menu-item>
          <el-menu-item index="/components">
            <el-icon><Share /></el-icon>
            <span>组件关系图</span>
          </el-menu-item>
          <el-menu-item index="/yara">
            <el-icon><Aim /></el-icon>
            <span>YARA 规则引擎</span>
          </el-menu-item>
          <el-menu-item index="/logs">
            <el-icon><Notebook /></el-icon>
            <span>日志查看器</span>
          </el-menu-item>
        </el-menu>

        <div class="aside-footer">
          <div class="status-dot" :class="backendOnline ? 'online' : 'offline'"></div>
          <span class="status-text">{{ backendOnline ? '后端连接正常' : '后端离线' }}</span>
        </div>
      </el-aside>

      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import {
  Monitor, WarningFilled, Timer, Share, Aim, Notebook
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const backendOnline = ref(false)

const checkBackend = async () => {
  try {
    await axios.get('/api/dashboard', { timeout: 3000 })
    backendOnline.value = true
  } catch {
    backendOnline.value = false
  }
}

onMounted(() => {
  checkBackend()
  setInterval(checkBackend, 15000)
})
</script>

<style scoped>
.app-aside {
  background: linear-gradient(180deg, #0c0c1d 0%, #12122a 50%, #0c0c1d 100%);
  min-height: 100vh;
  border-right: 1px solid rgba(255,255,255,0.04);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}
.logo {
  padding: 20px 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  cursor: pointer;
}
.logo-icon-box {
  width: 38px; height: 38px;
  background: linear-gradient(135deg, #00d4ff, #0066ff);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.logo-text { color: #e8e8f8; font-size: 15px; font-weight: 700; line-height: 1.2; }
.logo-sub  { color: #606078; font-size: 10px; }
.app-main {
  background: #0a0a16;
  min-height: 100vh;
  padding: 28px;
  position: relative;
  z-index: 1;
}
.aside-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.05);
  display: flex; align-items: center; gap: 8px;
}
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.online { background: #00ff88; box-shadow: 0 0 8px #00ff8877; }
.status-dot.offline { background: #ff4444; }
.status-text { color: #606078; font-size: 11px; }

/* 粒子背景 */
.particles-bg {
  position: fixed; inset: 0; z-index: 0; pointer-events: none;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(0,212,255,0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(0,102,255,0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, rgba(138,43,226,0.04) 0%, transparent 50%);
}

/* 页面过渡动画 */
.fade-slide-enter-active { transition: all 0.25s ease-out; }
.fade-slide-leave-active { transition: all 0.15s ease-in; }
.fade-slide-enter-from { opacity: 0; transform: translateY(10px); }
.fade-slide-leave-to   { opacity: 0; transform: translateY(-10px); }
</style>
