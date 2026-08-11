# Steam Threat Analysis Platform

[![CI](https://github.com/two-s-bro/steam-threat-analysis-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/two-s-bro/steam-threat-analysis-platform/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Sample policy](https://img.shields.io/badge/malware_samples-not_distributed-success.svg)](docs/SAMPLE_HANDLING.md)

一个面向检测工程与事件响应的离线研究项目：将一次 Steam 客户端劫持事件中的攻击链、IOC、持久化、客户端篡改和历史心跳证据，整理为可查询的 Spring Boot + Vue 数据集与教学界面。

> 安全承诺：本仓库不包含恶意可执行文件、DLL、Python 字节码、加密载荷、压缩包、原始日志或可恢复源码；不解析 DNS，也不连接历史 C2。所有基础设施均已去武器化，受害者标识均已删除。

## 项目为什么存在

安全事件中的原始证据往往不适合公开：它可能包含受害者身份、仍可访问的基础设施以及可恢复载荷。本项目探索一条更可复用的公开研究路径：

- 保留可验证性：公布 SHA-256、文件大小、观察时间与证据分层；
- 保留防御价值：记录攻击阶段、稳定检测信号、清理与恢复检查；
- 降低传播风险：只发布脱敏衍生数据和无害夹具；
- 自动防回归：CI 阻止样本、个人标识、可点击恶意域名和硬编码凭据进入公开树。

## 可核验的研究范围

| 项目 | 公开内容 | 私有证据（不发布） |
|------|----------|--------------------|
| 运行记录 | 11 行人工筛选、脱敏夹具 | 2,717 行源日志 |
| 文件证据 | 8 个 SHA-256 与大小 | 973 个隔离文件，约 105 MB |
| IOC | 去武器化域名、文件名、注册表信号 | 原始日志与工作站路径 |
| 样本 | 无 | 禁用的二进制、字节码、载荷与归档 |

这些数字描述研究语料，不代表用户数、下载量或项目采用率。详细方法见 [研究来源与证据分层](docs/RESEARCH_PROVENANCE.md)。

## 功能

- 攻击阶段时间线与组件关系浏览；
- IOC 分类、查询和风险标注；
- 历史 C2 状态的纯离线展示；
- 内置脱敏样例的幂等导入；
- 由事件证据衍生的文本模式规则及匹配解释；
- 面向公开仓库的安全门禁与单元测试。

规则页是教育用途的文本模式匹配器，不是完整 YARA 引擎。任何“未命中”都不能证明输入安全。

## 架构与数据流

```text
脱敏 SQL / 内置文本夹具
          │
          ▼
Spring Boot 3.2 / Java 17 ── MySQL 8
          │ REST /api
          ▼
      Vue 3 / Vite

外部网络连接：无
私有证据目录访问：无
```

## 本地运行

要求：JDK 17、Maven 3.9+、Node.js 22+、MySQL 8。

1. 初始化本地数据库（脚本仅用于你的本地研究库）：

   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p steam_threat_db < database/seed_data.sql
   mysql -u root -p steam_threat_db < database/yara_seed.sql
   ```

2. 通过环境变量提供数据库账号，不要提交本地口令：

   ```bash
   export DB_USERNAME=steam_threat
   export DB_PASSWORD='your-local-password'
   cd backend
   mvn spring-boot:run
   ```

   PowerShell 可使用 `$env:DB_USERNAME` 与 `$env:DB_PASSWORD`。后端默认只监听 `127.0.0.1:8088`。

3. 启动前端：

   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

访问 `http://localhost:5173`。如需改变本地来源，设置 `CORS_ALLOWED_ORIGIN`；生产部署应由反向代理和组织安全策略另行约束。

## 验证

```bash
python scripts/verify_public_safety.py

cd backend
mvn -B -ntp test

cd ../frontend
npm ci
npm run build
```

Pull Request 会执行相同门禁。工作流使用只读权限、并发取消和固定 commit SHA 的官方 actions。

## 证据与文档

- [研究来源与证据分层](docs/RESEARCH_PROVENANCE.md)
- [恶意样本处理政策](docs/SAMPLE_HANDLING.md)
- [攻击链路全景图](docs/01_攻击链路全景图.md)
- [防御性分析学习路径](docs/12_防御性分析学习路径.md)
- [清理与恢复检查](docs/how-to-remove.md)
- [哈希清单](evidence/sample-hashes.json)

## 参与维护

项目处于早期公开研究阶段。欢迎贡献脱敏测试、解析器改进、检测规则误报说明、文档校正与安全门禁增强；请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 和 [SECURITY.md](SECURITY.md)。

- 维护者与职责：[MAINTAINERS.md](MAINTAINERS.md)
- 计划中的工作：[ROADMAP.md](ROADMAP.md)
- 版本变化：[CHANGELOG.md](CHANGELOG.md)
- 支持范围：[SUPPORT.md](SUPPORT.md)

## 使用边界

本项目仅用于防御、安全教育和授权事件响应。不要用其绕过安全控制、恢复载荷、复刻钓鱼内容或连接恶意基础设施。Steam 和 Valve 是其各自所有者的商标；本项目与 Valve 无隶属或背书关系。

## License

[MIT](LICENSE)

---

**English summary:** An offline, sample-free defensive research platform derived from a Steam client hijacking incident. The public repository contains only hashes, defanged indicators, redacted fixtures, detection context, and incident-response documentation. It never ships or executes malware and makes no external C2 connection.
