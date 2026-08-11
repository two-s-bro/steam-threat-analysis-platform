# 组件 A — SteelDungeon.exe 静态证据摘要

> 本页只记录从隔离证据中得到的静态元数据和防御性结论。仓库不分发该文件，也不提供启动、解包、解密或恢复载荷的步骤。

## 已核验元数据

| 属性 | 值 |
|------|-----|
| 文件名 | `SteelDungeon.exe.SAFE_DISABLED`（仅存在于私有证据目录） |
| SHA-256 | `97796dac3ceb63587495da58b73dee1135b79b9f608e3019b4987d3bcc0cdbb7` |
| 大小 | 22,016 bytes |
| 相关载荷名 | `payload.bin`、`library.zip`、`python314.dll` |
| 观察到的打包特征 | CPython / PyInstaller 运行时布局 |

## 防御性判断

静态目录布局和同批日志支持以下假设，但不把尚未动态验证的内容表述为事实：

- 该文件可能承担首阶段加载器角色；
- 后续行为涉及持久化、进程控制、客户端文件篡改和外部通信；
- `payload.bin` 与压缩归档可能承载后续配置或组件；
- `vdf`、`requests`、`win32crypt` 等依赖名称可作为调查线索，但单独出现不能证明恶意行为。

## 可用于检测与调查的信号

- SHA-256 与文件大小组合；
- 异常目录名与同批组件的共同落地；
- 用户级 Run 键指向临时或随机目录；
- Steam 客户端资源文件和 `steam.cfg` 的非预期变更；
- 与仓库中已去武器化 IOC 对应的代理、DNS 或 EDR 历史记录。

## 安全边界

- 不在工作站或 CI 中执行、编译、解包或反编译样本；
- 不提交 `.exe`、`.dll`、`.pyc`、归档、载荷或原始日志；
- 不尝试恢复密钥、解密载荷或重建钓鱼界面；
- 需要进一步分析时，应由具备授权和隔离设施的专业团队按组织流程进行。

完整哈希清单见 [`../evidence/sample-hashes.json`](../evidence/sample-hashes.json)，处理规范见 [`SAMPLE_HANDLING.md`](SAMPLE_HANDLING.md)。
