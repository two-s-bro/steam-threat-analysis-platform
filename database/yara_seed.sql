-- Defensive pattern rules derived from redacted incident evidence.
-- These records feed the project's educational text matcher; they are not a substitute for a YARA scanner.
USE steam_threat_db;

CREATE TABLE IF NOT EXISTS yara_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    risk_level VARCHAR(10) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    rule_body TEXT NOT NULL,
    match_sample VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    match_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT IGNORE INTO yara_rule
(rule_name, description, risk_level, target_type, rule_body, match_sample, enabled) VALUES

('SteamHijack_RegPersistence_SteamHelper',
 '检测伪装成 SteamHelper 的用户级 Run 键持久化',
 'HIGH', 'registry',
 'rule SteamHijack_RegPersistence_SteamHelper {
    strings:
        $name = "SteamHelper" nocase
        $file = "lizercllaxe.exe"
        $run = "\\\\Microsoft\\\\Windows\\\\CurrentVersion\\\\Run"
    condition:
        $name and ($file or $run)
}',
 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\SteamHelper', TRUE),

('SteamHijack_RegPersistence_ServiceNames',
 '检测同批证据中使用的伪装服务名与文件名组合',
 'HIGH', 'registry',
 'rule SteamHijack_RegPersistence_ServiceNames {
    strings:
        $name1 = "SteamService" nocase
        $name2 = "WindowsUpdateService" nocase
        $file1 = "snapshot.exe"
        $file2 = "gameSatorHost.exe"
    condition:
        ($name1 and $file1) or ($name2 and $file2)
}',
 'Run 键名称与落地文件名组合', TRUE),

('SteamHijack_Component_Cluster',
 '检测同一目录或清单中出现的多个事件组件名',
 'HIGH', 'file',
 'rule SteamHijack_Component_Cluster {
    strings:
        $exe1 = "SteelDungeon.exe"
        $exe2 = "NexusTechNotify.exe"
        $dll1 = "locale_patch.dll"
        $payload = "payload.bin"
    condition:
        2 of them
}',
 '脱敏文件清单或 EDR 遥测', TRUE),

('SteamHijack_Defanged_C2',
 '匹配研究数据中的去武器化历史 C2 与端点',
 'HIGH', 'url',
 'rule SteamHijack_Defanged_C2 {
    strings:
        $host = "nexustechsolution[.]top"
        $api1 = "/steamhelper"
        $api2 = "/steamhelper.html"
    condition:
        $host and ($api1 or $api2)
}',
 'hxxps://nexustechsolution[.]top/steamhelper', TRUE),

('SteamHijack_Chunk_JS_Injection',
 '检测帮助页面路由与去武器化 IOC 同时出现的异常客户端脚本',
 'HIGH', 'file',
 'rule SteamHijack_Chunk_JS_Injection {
    strings:
        $host = "nexustechsolution[.]top"
        $route1 = "SupportMessages"
        $route2 = "HelpAppPage"
        $route3 = "HelpFrontPage"
    condition:
        $host and 1 of ($route*)
}',
 'Steam 客户端 JS 的脱敏文本或差异报告', TRUE),

('SteamHijack_SteamCfg_Disabled',
 '检测同时禁用 Steam 引导更新与强制自更新的配置',
 'MEDIUM', 'file',
 'rule SteamHijack_SteamCfg_Disabled {
    strings:
        $cfg1 = "BootStrapperInhibitAll=enable"
        $cfg2 = "BootStrapperForceSelfUpdate=disable"
    condition:
        all of them
}',
 'Steam\\steam.cfg 的文本内容', TRUE),

('SteamHijack_Toast_Phishing',
 '检测同批钓鱼通知资源名称与伪造案件标识组合',
 'MEDIUM', 'file',
 'rule SteamHijack_Toast_Phishing {
    strings:
        $html = "toast_window.html"
        $css = "toast-authentic.css"
        $window = "SteamToastWin"
        $ticket = "HT6YWQBY4XMF55"
    condition:
        2 of them
}',
 '文件清单、HTML 文本或窗口遥测', TRUE),

('SteamHijack_Packaged_Python_Cluster',
 '检测同一清单中出现的多个打包 Python 载荷组件',
 'MEDIUM', 'file',
 'rule SteamHijack_Packaged_Python_Cluster {
    strings:
        $runtime = "python314.dll"
        $library = "library.zip"
        $crypto = "win32crypt.pyd"
        $api = "win32api.pyd"
        $payload = "payload.bin"
    condition:
        3 of them
}',
 '静态文件清单；单个 Python 组件不足以判恶', TRUE),

('SteamHijack_Profile_Exfiltration_Context',
 '检测 Steam 配置读取与外传端点上下文组合，不依赖任何受害者 ID',
 'MEDIUM', 'file',
 'rule SteamHijack_Profile_Exfiltration_Context {
    strings:
        $vdf = "loginusers.vdf" nocase
        $profile = "steamid64" nocase
        $endpoint = "/steamhelper"
    condition:
        $vdf and $profile and $endpoint
}',
 '脱敏日志或脚本差异中的组合上下文', TRUE);
