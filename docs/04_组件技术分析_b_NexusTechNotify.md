# 组件 b — NexusTechNotify.exe (主控 / Steam 劫持引擎)

---

## 基本信息

| 属性 | 值 |
|------|-----|
| 文件名 | `NexusTechNotify.exe` |
| 技术栈 | C/C++ (MFC GUI 框架) |
| 体积 | 231 KB |
| 平台 | Win32 x86 |
| 运行方式 | 注册表 Run 键自启动 |

---

## 核心行为 (从日志还原)

### 1. 枚举 Steam 安装路径
```
steam path: E:\steam
```

### 2. 解析 Steam VDF 配置
```
读取 loginusers.vdf:
  ├─ SteamID64:  76561198892973993
  └─ 账户名哈希:  1301664629a

读取 config/config.vdf:
  └─ 头像哈希:    45f8663926dd7e3b076963037d63a0be85244240
```

### 3. 构建 C2 钓鱼 URL (带受害者唯一标识)
```
help URL:
  https://nexustechsolution.top/steamhelper?d=76561198892973993&a=45f8663926dd7e3b076963037d63a0be85244240

support URL:
  https://nexustechsolution.top/steamhelper.html?u=1301664629a&d=76561198892973993&a=45f8663926dd7e3b076963037d63a0be85244240
```

### 4. 强杀 Steam 进程
```
proc sync: force kill (no shutdown dialog)
proc sync: done
```

### 5. 篡改 Steam UI 核心文件
```
patch check: need=1 already=0
patch: needed, applying
目标文件: E:\steam\steamui\chunk~2dcc5aaf7.js
注入位置: ResolveURL() 函数内部
注入大小: ~110 字节
劫持目标: HelpAppPage / HelpFrontPage / SupportMessages
```

### 6. 写入 steam.cfg 禁用更新
```
steam.cfg: write ok
内容:
  BootStrapperInhibitAll=enable
  BootStrapperForceSelfUpdate=disable
```

### 7. 静默重启 Steam
```
proc sync: steam.exe -silent (detached)
```

### 8. 守护线程
```
每 42 秒循环检查:
  watch: check
  patch check: need=0 already=1
  cfg check: need=0 had_steam=1
  patch skip: already patched
  watch: result=0

如果发现补丁被修复 (need=1 already=0):
  → 重新执行步骤 4-7
```

---

## 守护线程时间线

日志显示从 2026-06-15 23:25:59 开始守护，约每 42-43 秒检查一次，
持续运行到 2026-06-16 22:53:44，共约 23.5 小时。

---

## 下载日志格式

```
2026-06-15 23:25:44 76561198892973993----匹配成功----下载成功----执行成功
2026-06-15 23:29:29 76561198892973993----匹配成功----已经下载过----进程已经存在
```

格式: `时间戳 SteamID64----匹配状态----下载状态----执行状态`

每约 3.5 分钟向 C2 上报一次心跳。

---

## 待逆向内容

- IDA Pro / Ghidra 静态分析可执行文件
- C2 API 端点完整列表
- 是否有其他隐藏功能 (键盘记录/截图/远控)
