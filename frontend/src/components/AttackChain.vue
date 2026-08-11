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
      '用户从不可信来源取得伪装资源',
      '初始传播渠道未被独立确认',
      '建议关联下载历史与安全告警'
    ]
  },
  {
    name: 'Dropper 落地',
    desc: 'Python 3.14',
    color: 'phase-1',
    details: [
      '多个异常组件在用户目录中同批出现',
      '用户级 Run 键指向异常目录',
      '仓库仅发布哈希，不提供任何组件',
      '载荷内容未经公开动态验证'
    ]
  },
  {
    name: 'Steam UI 劫持',
    desc: 'C/C++ MFC',
    color: 'phase-2',
    details: [
      '账户相关配置被读取（公开值已脱敏）',
      '历史记录包含去武器化异常目的地',
      'Steam 进程与 UI 资源发生异常变化',
      '更新相关配置被修改',
      '周期性检查行为出现在历史日志中'
    ]
  },
  {
    name: '钓鱼窃密',
    desc: 'CEF注入+Toast',
    color: 'phase-3',
    details: [
      '本地资源呈现仿冒支持通知',
      '帮助/支持路由出现异常目的地',
      '当前基础设施状态未检查',
      '凭据风险取决于用户交互与远端证据'
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
