<template>
  <div class="attack-chain">
    <div class="chain-container">
      <div v-for="(phase, idx) in phases" :key="idx" class="phase-wrapper">
        <!-- 阶段节点 -->
        <div class="phase-node" :class="phase.color">
          <div class="phase-number">Phase {{ idx }}</div>
          <div class="phase-name">{{ phase.name }}</div>
          <div class="phase-desc">{{ phase.desc }}</div>
        </div>
        <!-- 连接箭头 -->
        <div v-if="idx < phases.length - 1" class="arrow-wrapper">
          <div class="arrow-line"></div>
          <div class="arrow-head">▼</div>
        </div>
      </div>
    </div>

    <!-- 详情卡片 -->
    <div class="phase-details">
      <el-collapse>
        <el-collapse-item v-for="(phase, idx) in phases" :key="idx"
          :title="`Phase ${idx}: ${phase.name}`">
          <ul class="detail-list">
            <li v-for="item in phase.details" :key="item">{{ item }}</li>
          </ul>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
const phases = [
  {
    name: '欺骗传播',
    desc: '诱导下载',
    color: 'phase-0',
    details: [
      '伪装为免费资源/修改器/外挂',
      '通过网盘、论坛、群聊传播',
      '诱导用户下载 SteelDungeon.exe'
    ]
  },
  {
    name: 'Dropper 落地',
    desc: 'Python 3.14',
    color: 'phase-1',
    details: [
      '自解压到 %LocalAppData%\\Programs\\01anu7963sndw1ua\\',
      '写入注册表 Run 键 "SteamHelper" 持久化',
      '从 C2 下载 NexusTechNotify.exe + locale_patch.dll',
      '解码 payload.bin 和 ungeond.rar'
    ]
  },
  {
    name: 'Steam UI 劫持',
    desc: 'C/C++ MFC',
    color: 'phase-2',
    details: [
      '读取 Steam VDF → 获取 SteamID64 + 头像哈希',
      '向 C2 (nexustechsolution.top) 注册受害者',
      '强杀 Steam 进程 (不弹关机对话框)',
      '篡改 chunk~2dcc5aaf7.js → 注入 110B 恶意代码',
      '写 steam.cfg 禁用自动更新',
      '静默重启: steam.exe -silent',
      '守护线程: 每 42 秒检查补丁完整性'
    ]
  },
  {
    name: '钓鱼窃密',
    desc: 'CEF注入+Toast',
    color: 'phase-3',
    details: [
      'locale_patch.dll 向 CEF 注入 toast_window.html',
      '弹窗显示: "Steam客服 - 您客服案件的新回复"',
      '用户点击 → ResolveURL("HelpAppPage") 被劫持',
      '跳转: nexustechsolution.top/steamhelper?d={SteamID64}&a={hash}'
    ]
  }
]
</script>

<style scoped>
.chain-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}
.phase-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.phase-node {
  width: 320px;
  padding: 14px 18px;
  border-radius: 8px;
  text-align: center;
  border-left: 4px solid;
}
.phase-0 { background: #1a1a2e; border-color: #ffa502; }
.phase-1 { background: #1a1a2e; border-color: #ff6b6b; }
.phase-2 { background: #1a1a2e; border-color: #ff4757; }
.phase-3 { background: #1a1a2e; border-color: #ee5a24; }
.phase-number { font-size: 11px; color: #888; }
.phase-name { font-size: 16px; font-weight: 700; color: #e0e0f0; margin: 4px 0; }
.phase-desc { font-size: 12px; color: #888; }
.arrow-wrapper { text-align: center; padding: 2px 0; }
.arrow-head { color: #555; font-size: 14px; }
.detail-list { color: #bbb; font-size: 13px; line-height: 2; padding-left: 16px; }
.phase-details { margin-top: 16px; }
</style>
