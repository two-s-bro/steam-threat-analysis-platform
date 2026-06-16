-- ============================================
-- Steam 威胁分析平台 - 种子数据
-- 数据来源: 对真实 Steam 劫持病毒的完整逆向分析
-- 受害者 SteamID64: 76561198892973993
-- ============================================

USE steam_threat_db;

-- ============================================
-- 1. IOC 威胁指标 (22条)
-- ============================================
INSERT INTO ioc (ioc_type, ioc_value, risk_level, source_file, description, attack_phase) VALUES

-- 域名
('domain', 'nexustechsolution.top', 'HIGH', 'SteamPatchToast_patch.lo', 'C2 钓鱼页面主机，使用 Cloudflare CDN 隐藏真实IP', 2),

-- 文件路径
('file_path', '%LocalAppData%\\Programs\\01anu7963sndw1ua\\SteelDungeon.exe', 'HIGH', 'README_研究说明.txt', 'Python 3.14 PyInstaller dropper 主程序', 1),
('file_path', '%LocalAppData%\\ServiceApp\\NexusTechNotify.exe', 'HIGH', '.state.json', 'C/C++ MFC 主控程序，负责Steam劫持和守护', 2),
('file_path', '%LocalAppData%\\ServiceApp\\locale_patch.dll', 'HIGH', 'README_研究说明.txt', 'CEF 进程注入 DLL，加载钓鱼弹窗', 3),
('file_path', '%LocalAppData%\\ServiceApp\\toast_window.html', 'MEDIUM', 'README_研究说明.txt', '高仿真 Steam 客服通知钓鱼弹窗', 3),
('file_path', '%LocalAppData%\\ServiceApp\\toast-authentic.css', 'LOW', 'README_研究说明.txt', '1:1 还原 Steam Toast CSS (169行)', 3),
('file_path', 'C:\\Windows\\System32\\merged_steam.py', 'HIGH', '注册表 Run 键', '恶意 Python 脚本，伪装为系统文件', 2),
('file_path', 'steamui\\chunk~2dcc5aaf7.js', 'MEDIUM', 'SteamPatchToast_patch.lo', '被注入 110B 恶意代码的 Steam UI 核心 JS', 2),
('file_path', 'Steam\\steam.cfg', 'MEDIUM', 'SteamPatchToast_patch.lo', '被篡改的 Steam 配置文件 (禁用自动更新)', 2),

-- 注册表键
('registry_key', 'HKCU\\...\\Run\\SteamHelper', 'HIGH', '注册表扫描', 'Dropper 持久化 - 伪装 Steam 助手', 1),
('registry_key', 'HKCU\\...\\Run\\SteamService', 'HIGH', '注册表扫描', '主控持久化 - 伪装 Steam 服务', 2),
('registry_key', 'HKCU\\...\\Run\\WindowsUpdateService', 'HIGH', '注册表扫描', '注入器持久化 - 伪装 Windows 更新', 3),

-- SteamID
('steamid', '76561198892973993', 'MEDIUM', 'SteamPatchToast_patch.lo', '受害者 SteamID64，已回传至 C2', 2),

-- 案件编号
('case_number', 'HT6YWQBY4XMF55', 'LOW', 'toast_window.html', '伪造的 Steam 客服案件编号', 3),

-- 恶意文件路径
('file_path', '%Temp%\\launcher_40ed5d587663474f9cb7355b40c1041a\\lizercllaxe.exe', 'HIGH', '注册表 Run 键', 'SteamHelper 指向的 dropper 副本', 1),
('file_path', '%AppData%\\v6vslsk8sfjeiqau\\snapshot.exe', 'HIGH', '注册表 Run 键', 'SteamService 指向的主控副本', 2),
('file_path', '%Temp%\\launcher_7049431c078f4302a634bafe3e5057e5\\gameSatorHost.exe', 'HIGH', '注册表 Run 键', 'WindowsUpdateService 指向的注入器', 3),

-- C2 URL
('domain', 'nexustechsolution.top/steamhelper', 'HIGH', 'SteamPatchToast_patch.lo', '钓鱼页面: 含 SteamID64 + 头像哈希参数', 3),
('domain', 'nexustechsolution.top/steamhelper.html', 'HIGH', 'SteamPatchToast_patch.lo', '备用钓鱼页: 含账户哈希+SteamID64+头像哈希', 3),

-- 其他
('file_path', '%LocalAppData%\\Temp\\steam', 'LOW', '文件系统扫描', '病毒临时文件目录', 1),
('file_path', '%LocalAppData%\\ServiceApp\\desktop_toast_default.wav', 'LOW', 'README_研究说明.txt', 'Steam 原生通知音效 (增强仿真度)', 3),
('file_path', '%LocalAppData%\\ServiceApp\\.state.json', 'LOW', 'README_研究说明.txt', '感染状态标记文件', 2);


-- ============================================
-- 2. 攻击时间线 (30条关键事件)
-- ============================================
INSERT INTO timeline (timestamp, phase, action, detail, component_id) VALUES

-- Phase 1: Dropper
('2026-06-15 23:25:44', 'DROPPER', 'C2 下载成功，SteelDungeon 落地', '76561198892973993----匹配成功----下载成功----执行成功', 1),
('2026-06-15 23:25:53', 'DROPPER', 'SteelDungeon 启动，枚举 Steam 路径', 'steam path: E:\\steam', 1),
('2026-06-15 23:25:53', 'DROPPER', '读取 Steam VDF 配置获取受害者信息', 'profile steamid64: 76561198892973993, account: 1301664629a, avatar: 45f8663926dd7e3b076963037d63a0be85244240', 1),

-- Phase 2: 注入
('2026-06-15 23:25:53', 'INJECT', '检测需要打补丁', 'patch check: need=1 already=0', 2),
('2026-06-15 23:25:53', 'INJECT', '强杀 Steam 进程', 'proc sync: force kill (no shutdown dialog)', 2),
('2026-06-15 23:25:54', 'INJECT', 'Steam 进程已终止', 'proc sync: done', 2),
('2026-06-15 23:25:54', 'PHISH', '构建钓鱼 URL (含受害者 SteamID)', 'patch url help: https://nexustechsolution.top/steamhelper?d=76561198892973993&a=45f8663926dd7e3b076963037d63a0be85244240', 2),
('2026-06-15 23:25:54', 'PHISH', '构建备用钓鱼 URL (含账户哈希)', 'patch url support: https://nexustechsolution.top/steamhelper.html?u=1301664629a&d=76561198892973993&a=45f8663926dd7e3b076963037d63a0be85244240', 2),
('2026-06-15 23:25:55', 'INJECT', 'chunk JS 注入完成', 'patch: ok done total=1 attempt=0', 2),
('2026-06-15 23:25:55', 'INJECT', '写入 steam.cfg 禁用自动更新', 'steam.cfg: write ok (BootStrapperInhibitAll=enable, BootStrapperForceSelfUpdate=disable)', 2),
('2026-06-15 23:25:55', 'INJECT', '静默重启 Steam', 'proc sync: steam.exe -silent (detached)', 2),
('2026-06-15 23:25:55', 'PHISH', '钓鱼弹窗部署完成', 'toast_window.html OK', 3),

-- 守护循环 (抽样)
('2026-06-15 23:25:59', 'HEARTBEAT', '守护线程: 第1次检查', 'watch: check → result=0 (补丁在位)', 2),
('2026-06-15 23:26:45', 'HEARTBEAT', '守护线程: 第2次检查', 'watch: check → result=0', 2),
('2026-06-15 23:27:31', 'HEARTBEAT', '守护线程: 第3次检查', 'watch: check → result=0', 2),
('2026-06-15 23:29:29', 'HEARTBEAT', 'C2 心跳上报 (第2次)', '76561198892973993----匹配成功----已经下载过----进程已经存在', 2),
('2026-06-15 23:33:04', 'HEARTBEAT', 'C2 心跳上报 (第3次)', '76561198892973993----匹配成功----已经下载过----进程已经存在', 2),

-- 重启后
('2026-06-16 08:05:08', 'INJECT', '机器重启后病毒再次启动', '=== Application start === (第2次启动)', 2),
('2026-06-16 08:05:08', 'INJECT', '验证补丁在位', 'patch check: need=0 already=1 → patch skip', 2),
('2026-06-16 08:05:08', 'INJECT', '验证 steam.cfg 在位', 'cfg check: need=0 had_steam=1', 2),

-- 持续运行至深夜
('2026-06-16 21:29:28', 'INJECT', '第3次启动 (可能手动重启)', '=== Application start ===', 2),
('2026-06-16 21:29:31', 'INJECT', '补丁验证通过', 'patch: ok, toast_window.html OK', 2),
('2026-06-16 22:53:44', 'HEARTBEAT', '最后一次 C2 心跳', '76561198892973993----匹配成功----已经下载过----进程已经存在 (日志终止)', 2);


-- ============================================
-- 3. 病毒组件 (14个)
-- ============================================
INSERT INTO component_info (id, name, role, tech_stack, file_size, parent_id, attack_phase, description) VALUES

-- Phase 1: Dropper
(1, 'SteelDungeon.exe', 'dropper', 'Python 3.14 + PyInstaller', 22016, NULL, 1,
 '主载荷入口。自解压到 %LocalAppData% 并下载后续组件，写入注册表持久化。'),
(2, 'python314.dll', 'payload', 'CPython 3.14 原生DLL', 6753112, 1, 1,
 'Python 3.14 运行时，使打包后的 Python 代码能在目标机器上执行。'),
(3, 'payload.bin', 'payload', '二进制加密数据', 38096, 1, 1,
 '加密的 Dropper 载荷配置，需逆向 SteelDungeon 获取解密密钥。'),
(4, 'ungeond.rar', 'payload', '加密 RAR 压缩包', 15883534, 1, 1,
 '15.8MB 加密压缩包，疑似包含 ServiceApp 完整组件。'),
(5, 'library.zip', 'payload', 'Python 标准库', NULL, 1, 1,
 'PyInstaller 打包的 Python 标准库，包含 JWT/VDF/requests 等依赖。'),

-- Phase 2: 主控
(6, 'NexusTechNotify.exe', 'controller', 'C/C++ MFC (Win32 x86)', 236544, 1, 2,
 '主控程序。强杀 Steam、篡改 chunk JS、写 steam.cfg，每 42 秒守护补丁。'),
(7, '.state.json', 'payload', 'JSON 状态文件', 101, 6, 2,
 '感染状态标记: {downloaded:true, exe_list:[...]}'),

-- Phase 2: C2
(8, 'nexustechsolution.top', 'c2', 'HTTPS + Cloudflare', NULL, 6, 2,
 'C2 服务器域名。端点: /steamhelper, /steamhelper.html'),

-- Phase 3: 注入器
(9, 'locale_patch.dll', 'injector', 'C/C++ DLL (Win32 x86)', 206336, 6, 3,
 'CEF 进程注入 DLL。将 toast_window.html 注入 Steam 浏览器渲染管线。'),

-- Phase 3: 钓鱼弹窗
(10, 'toast_window.html', 'payload', 'HTML5 + vanilla JS', 6280, 9, 3,
 '高仿真 Steam 客服通知弹窗。SSOT: 实时时间戳 + 固定案件编号。'),
(11, 'toast-authentic.css', 'payload', 'CSS3 (169 lines)', 115636, 10, 3,
 '从 Steam 原版 chunk~2dcc5aaf7.css 提取，1:1 还原 Toast 样式。'),
(12, 'desktop_toast_default.wav', 'payload', 'WAV 音效', 111952, 10, 3,
 'Steam 原生通知音效，增强弹窗仿真度。'),

-- Stealth
(13, 'chunk~2dcc5aaf7.js (篡改后)', 'payload', 'JavaScript (~14MB, +110B注入)', NULL, 6, 2,
 'Steam UI Webpack chunk。在 ResolveURL() 中注入 110B，劫持 HelpAppPage/HelpFrontPage/SupportMessages。'),
(14, 'steam.cfg (篡改后)', 'payload', 'INI 配置', 68, 6, 2,
 '禁用 Steam 自动更新: BootStrapperInhibitAll=enable, BootStrapperForceSelfUpdate=disable');
