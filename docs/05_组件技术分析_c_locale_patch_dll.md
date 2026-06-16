# 组件 c — locale_patch.dll (CEF 进程注入)

---

## 基本信息

| 属性 | 值 |
|------|-----|
| 文件名 | `locale_patch.dll` |
| 技术栈 | C/C++ DLL |
| 体积 | 201 KB |
| 平台 | Win32 x86 |
| 注入目标 | Steam CEF (Chromium Embedded Framework) 进程 |

---

## 推测功能

1. **注入到 Steam CEF 进程**
   - Steam 客户端使用 CEF 渲染 UI
   - DLL 通过 `SetWindowsHookEx` 或 `CreateRemoteThread` 注入

2. **加载钓鱼弹窗**
   - 在 CEF 浏览器上下文中加载 `toast_window.html`
   - 使用 Steam 原生 Toast 动画系统显示弹窗

3. **触发时机**
   - 注入后等待合适时机显示弹窗（用户活跃状态）
   - 弹窗设计为看起来像 Steam 客服通知

---

## 与 JS 注入的配合

locale_patch.dll 负责 **UI 诱导层**:
- 在 CEF 渲染管线的特定位置插入 toast_window.html
- 弹窗样式使用 toast-authentic.css (1:1 还原 Steam Toast)

chunk JS 注入负责 **URL 路由层**:
- 用户点击弹窗后，Steam 调用 ResolveURL("HelpAppPage")
- 被劫持的 URL 将用户导向 C2 钓鱼页面

---

## 待逆向内容

- IDA Pro / Ghidra 分析注入技术 (Hook 类型、注入方法)
- CEF 渲染管线劫持点分析
- 是否有反调试/反沙箱代码
