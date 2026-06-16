-- ============================================
-- YARA 规则种子数据
-- 基于 Steam 劫持病毒特征编写
-- ============================================
USE steam_threat_db;

-- 建表 (如果 JPA ddl-auto=none 则需手动)
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

-- 清空旧数据
TRUNCATE yara_rule;

INSERT INTO yara_rule (rule_name, description, risk_level, target_type, rule_body, match_sample, enabled) VALUES

('SteamHijack_RegPersistence_SteamHelper',
 '检测伪装成 SteamHelper 的注册表持久化',
 'HIGH', 'registry',
 'rule SteamHijack_RegPersistence_SteamHelper {
    strings:
        $key1 = "SteamHelper" nocase
        $key2 = "lizercllaxe.exe"
        $key3 = "\\\\Microsoft\\\\Windows\\\\CurrentVersion\\\\Run"
    condition:
        ($key1 and $key2) or ($key1 and $key3)
}',
 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\SteamHelper', TRUE),

('SteamHijack_RegPersistence_SteamService',
 '检测伪装成 SteamService 的注册表持久化',
 'HIGH', 'registry',
 'rule SteamHijack_RegPersistence_SteamService {
    strings:
        $key1 = "SteamService" nocase
        $key2 = "snapshot.exe"
        $key3 = "v6vslsk8sfjeiqau"
    condition:
        ($key1 and $key2) or any of them
}',
 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\SteamService', TRUE),

('SteamHijack_RegPersistence_WindowsUpdateService',
 '检测伪装成 WindowsUpdateService 的注册表持久化',
 'HIGH', 'registry',
 'rule SteamHijack_RegPersistence_WindowsUpdateService {
    strings:
        $key1 = "WindowsUpdateService" nocase
        $key2 = "gameSatorHost.exe"
        $key3 = "launcher_"
    condition:
        ($key1 and $key2) or ($key1 and $key3)
}',
 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\WindowsUpdateService', TRUE),

('SteamHijack_Dropper_SteelDungeon',
 '检测 SteelDungeon dropper 及其组件',
 'HIGH', 'file',
 'rule SteamHijack_Dropper_SteelDungeon {
    strings:
        $exe1 = "SteelDungeon.exe"
        $exe2 = "NexusTechNotify.exe"
        $dll1 = "locale_patch.dll"
        $py1  = "python314.dll"
        $dir  = "01anu7963sndw1ua"
    condition:
        2 of them
}',
 'C:\\Users\\*\\AppData\\Local\\Programs\\01anu7963sndw1ua\\SteelDungeon.exe', TRUE),

('SteamHijack_C2_Domain',
 '检测 C2 域名 nexustechsolution.top',
 'HIGH', 'url',
 'rule SteamHijack_C2_Domain {
    strings:
        $c2  = "nexustechsolution.top"
        $api = "/steamhelper"
        $api2 = "/steamhelper.html"
    condition:
        $c2 and ($api or $api2)
}',
 'https://nexustechsolution.top/steamhelper?d=76561198892973993', TRUE),

('SteamHijack_Chunk_JS_Injection',
 '检测被注入恶意代码的 Steam chunk JS',
 'HIGH', 'file',
 'rule SteamHijack_Chunk_JS_Injection {
    strings:
        $inj1 = "nexustechsolution.top"
        $inj2 = "SupportMessages"
        $inj3 = "HelpAppPage"
        $inj4 = "HelpFrontPage"
        $inj5 = "76561198892973993"
    condition:
        ($inj1 and $inj2) or ($inj1 and ($inj3 or $inj4))
}',
 'steamui\\chunk~2dcc5aaf7.js (被篡改版本)', TRUE),

('SteamHijack_SteamCfg_Disabled',
 '检测被篡改的 steam.cfg (禁用自动更新)',
 'MEDIUM', 'file',
 'rule SteamHijack_SteamCfg_Disabled {
    strings:
        $cfg1 = "BootStrapperInhibitAll=enable"
        $cfg2 = "BootStrapperForceSelfUpdate=disable"
    condition:
        $cfg1 and $cfg2
}',
 'Steam\\steam.cfg (篡改后)', TRUE),

('SteamHijack_Toast_Phishing',
 '检测钓鱼 Toast 弹窗文件',
 'MEDIUM', 'file',
 'rule SteamHijack_Toast_Phishing {
    strings:
        $t1 = "toast_window.html"
        $t2 = "toast-authentic.css"
        $t3 = "SteamToastWin"
        $t4 = "HT6YWQBY4XMF55"
        $t5 = "您客服案件的新回复"
    condition:
        ($t1 and $t2) or $t3 or ($t4 and $t5)
}',
 'ServiceApp\\toast_window.html', TRUE),

('SteamHijack_PyInstaller_Payload',
 '检测 PyInstaller 打包的恶意 Python 载荷',
 'MEDIUM', 'file',
 'rule SteamHijack_PyInstaller_Payload {
    strings:
        $p1 = "python314.dll"
        $p2 = "library.zip"
        $p3 = "win32crypt.pyd"
        $p4 = "win32api.pyd"
        $p5 = "payload.bin"
    condition:
        3 of them
}',
 '01anu7963sndw1ua\\ (Python 3.14 PyInstaller 载荷)', TRUE),

('SteamHijack_VDF_SteamID64_Leak',
 '检测泄露的 SteamID64 标识符',
 'MEDIUM', 'file',
 'rule SteamHijack_VDF_SteamID64_Leak {
    strings:
        $sid = "76561198892973993"
        $hash1 = "1301664629a"
        $hash2 = "45f8663926dd7e3b076963037d63a0be85244240"
    condition:
        $sid and ($hash1 or $hash2)
}',
 '日志/配置文件中硬编码的 SteamID64', TRUE);
