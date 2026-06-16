# 🛡️ Steam 劫持病毒 — 完整清除与安全加固指南

> **如果你怀疑自己的 Steam 被劫持了，按这份指南一步一步操作。**
>
> 本指南基于对真实 Steam 凭证窃取木马的完整逆向分析编写。
> 每一步都有截图级文字说明，零基础也能跟做。

---

## ⚠️ 先读这里

| 问题 | 答案 |
|------|------|
| 我中了这个病毒吗？ | 往下看「第一步：判断是否中招」 |
| 我的 Steam 库存会被盗吗？ | 如果你开着 Steam Guard 手机令牌，大概率不会。如果没开，**立即往下操作** |
| 重装 Steam 就行了吗？ | **不够**。病毒在注册表有持久化，重装 Steam 不会清除 |
| 整个过程要多久？ | 15-30 分钟 |

---

## 目录

1. [第一步：判断是否中招](#第一步判断是否中招)
2. [第二步：停止病毒进程](#第二步停止病毒进程)
3. [第三步：删除注册表自启动](#第三步删除注册表自启动)
4. [第四步：删除病毒文件](#第四步删除病毒文件)
5. [第五步：检查 Steam 文件完整性](#第五步检查-steam-文件完整性)
6. [第六步：修改 Steam 密码](#第六步修改-steam-密码)
7. [第七步：开启 Steam Guard 手机令牌](#第七步开启-steam-guard-手机令牌)
8. [第八步：注销可疑设备](#第八步注销可疑设备)
9. [第九步：检查 API 密钥和交易记录](#第九步检查-api-密钥和交易记录)
10. [第十步：最终验证](#第十步最终验证)
11. [附：一键检测脚本](#附一键检测脚本powershell)
12. [附：防护建议](#附防护建议)

---

## 第一步：判断是否中招

### 1.1 检查注册表 Run 键

按下键盘上的 **Win + R**，输入 `regedit`，回车。

在注册表编辑器地址栏粘贴以下路径：

```
计算机\HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Run
```

看右边列表里有没有以下**任何一个**键名：

| 可疑键名 | 指向的恶意文件 | 伪装成 |
|----------|---------------|--------|
| `SteamHelper` | `...\launcher_xxxx\...\lizercllaxe.exe` | Steam 助手 |
| `SteamService` | `...\v6vslsk8sfjeiqau\snapshot.exe` | Steam 服务 |
| `WindowsUpdateService` | `...\launcher_xxxx\...\gameSatorHost.exe` | Windows 更新 |

**如果三个都没有 → 你可能没中这个病毒，但继续往下检查。**

### 1.2 检查病毒目录

按 **Win + R**，输入 `%LocalAppData%\Programs`，回车。

在里面找有没有叫 `01anu7963sndw1ua` 的文件夹。

按 **Win + R**，输入 `%LocalAppData%`，回车。

在里面找有没有叫 `ServiceApp` 的文件夹。

### 1.3 检查 Steam 配置文件

打开你的 Steam 安装目录（默认 `C:\Program Files (x86)\Steam`），
找到 `steam.cfg` 文件。

用记事本打开。**正常情况下这个文件不存在或者内容没有下面这两行：**

```
BootStrapperInhibitAll=enable
BootStrapperForceSelfUpdate=disable
```

如果看到这两行 → **确认中招**。

### 1.4 火绒/Defender 日志

打开火绒安全软件 → 安全日志，搜索以下关键词：
- `SteelDungeon`
- `NexusTechNotify`
- `lizercllaxe`
- `snapshot.exe`

如果有相关拦截记录 → **确认中招**。

---

## 第二步：停止病毒进程

### 2.1 打开任务管理器

按 **Ctrl + Shift + Esc** 打开任务管理器。

### 2.2 找可疑进程

点"详细信息"标签页，在列表里找以下进程名：

| 进程名 | 右键 → 结束任务 |
|--------|:--:|
| `NexusTechNotify.exe` | ✅ 结束 |
| `SteelDungeon.exe` | ✅ 结束 |
| `lizercllaxe.exe` | ✅ 结束 |
| `snapshot.exe` | ✅ 结束 |
| `gameSatorHost.exe` | ✅ 结束 |

**没找到不代表没有**，继续下一步。

### 2.3 用命令行强杀（保险）

按 **Win + R**，输入 `cmd`，按 **Ctrl + Shift + Enter**（以管理员身份运行）。

逐条粘贴执行：

```batch
taskkill /f /im NexusTechNotify.exe 2>nul
taskkill /f /im SteelDungeon.exe 2>nul
taskkill /f /im lizercllaxe.exe 2>nul
taskkill /f /im snapshot.exe 2>nul
taskkill /f /im gameSatorHost.exe 2>nul
echo 完成
```

显示"成功"或"没有找到"，两种都正常。

---

## 第三步：删除注册表自启动

### 3.1 打开注册表编辑器

按 **Win + R**，输入 `regedit`，回车。

### 3.2 导航到 Run 键

在地址栏粘贴：

```
HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Run
```

### 3.3 删除恶意键

在右边列表里，**右键单击** → **删除** 以下键：

- [ ] `SteamHelper`
- [ ] `SteamService`
- [ ] `WindowsUpdateService`

**每删一个，系统会问"确定要删除吗" → 点"是"。**

> ⚠️ 注意：不要删除其他的键！正常的自启动项包括 OneDrive、LGHUB、Thunder 等。

### 3.4 验证删除成功

删完后，右边列表里不再有这三个键名。

或者用命令行验证（以管理员身份运行 cmd）：

```batch
reg query "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" | findstr /i "SteamHelper SteamService WindowsUpdateService"
```

如果**没有任何输出** → 删除成功 ✅

---

## 第四步：删除病毒文件

### 4.1 删除 Dropper 目录

按 **Win + R**，粘贴以下路径，回车：

```
%LocalAppData%\Programs\01anu7963sndw1ua
```

如果这个文件夹存在 → **右键整个文件夹 → 删除** → 清空回收站。

### 4.2 删除主控目录

按 **Win + R**，粘贴，回车：

```
%LocalAppData%\ServiceApp
```

如果存在 → 右键删除 → 清空回收站。

### 4.3 删除临时文件中的残留

按 **Win + R**，粘贴，回车：

```
%LocalAppData%\Temp
```

按 **Ctrl + F**，搜索以下关键词，搜到的文件夹全部删掉：

```
launcher_
v6vslsk8sfjeiqau
steam
merged_steam
```

### 4.4 删除 AppData\Roaming 残留

按 **Win + R**，粘贴，回车：

```
%AppData%
```

找有没有叫 `v6vslsk8sfjeiqau` 或 `01anu7963sndw1ua` 的文件夹，有就删。

### 4.5 用命令行一键清理

以**管理员身份**打开 cmd，一次性粘贴执行：

```batch
rmdir /s /q "%LocalAppData%\Programs\01anu7963sndw1ua" 2>nul
rmdir /s /q "%LocalAppData%\ServiceApp" 2>nul
rmdir /s /q "%AppData%\v6vslsk8sfjeiqau" 2>nul
rmdir /s /q "%AppData%\01anu7963sndw1ua" 2>nul
del /f /q "%LocalAppData%\Temp\steam\*" 2>nul
rmdir /s /q "%LocalAppData%\Temp\steam" 2>nul
for /d %i in ("%LocalAppData%\Temp\launcher_*") do rmdir /s /q "%i" 2>nul
del /f /q "C:\Windows\System32\merged_steam.py" 2>nul
echo 清理完成
```

---

## 第五步：检查 Steam 文件完整性

### 5.1 检查 steam.cfg

打开你的 Steam 安装目录（默认 `C:\Program Files (x86)\Steam`）。

找到 `steam.cfg` 文件：
- 如果这个文件**不存在** → ✅ 正常
- 如果存在 → 用记事本打开，看有没有：
  ```
  BootStrapperInhibitAll=enable
  BootStrapperForceSelfUpdate=disable
  ```
- 如果有 → **直接删除整个 steam.cfg 文件**，或者把这两行删掉并保存。

### 5.2 检查 steamui 目录

进入 Steam 安装目录下的 `steamui` 文件夹。

找到文件 `chunk~2dcc5aaf7.js`（约 14 MB）。

在文件上 **右键 → 属性 → 详细信息**。如果显示最近被修改过，而你最近没更新 Steam，可能被篡改过。

### 5.3 最简单的方法 — 重装 Steam

如果你不放心手动检查：

1. 备份 `Steam\steamapps` 文件夹（这是你下载的游戏，不用重新下）
2. 控制面板 → 卸载程序 → 卸载 Steam
3. 删除整个 Steam 安装目录（保留 steamapps 备份）
4. https://store.steampowered.com/about/ 重新下载安装
5. 安装完后把 steamapps 复制回去

> **注意：重装 Steam 不会清除病毒的注册表持久化！必须先做第三步！**

---

## 第六步：修改 Steam 密码

> ⚠️ 这一节很重要。病毒可能已经获取了你的密码。

### 6.1 修改密码

1. 打开 Steam 客户端，点右上角你的用户名 → **账户明细**
2. 在"账户安全"下，点 **更改密码**
3. 输入当前密码 → 输入新密码 → 确认

**新密码要求：**
- 至少 12 位
- 包含大小写字母 + 数字 + 符号
- **不要**跟其他网站共用
- 推荐用密码管理器生成随机密码（Bitwarden / 1Password）

### 6.2 如果 Steam 账号已被改密码/改邮箱

如果你发现自己登不上 Steam 了：

1. 打开 https://help.steampowered.com/
2. 点 **"我的 Steam 账户"** → **"账户被盗"**
3. 选择 **"更改我的密码"** → 用注册邮箱找回
4. 如果邮箱也被改了 → 联系 Steam 客服，提供购买记录/激活码证明你是号主

---

## 第七步：开启 Steam Guard 手机令牌

**这是最重要的一步。手机令牌是目前最有效的防劫持手段。**

### 7.1 下载 Steam App

在手机应用商店搜索 **Steam**，下载官方 App（不是第三方）。

### 7.2 开启令牌

1. 打开手机 Steam App，登录你的账号
2. 点左上角菜单 → **Steam Guard**
3. 点 **添加验证器**
4. 输入手机号 → 收到验证码 → 输入
5. 保存**救援代码**（Recovery Code）

> ⚠️ **把救援代码抄下来，放安全的地方。** 手机丢了没有这个码，账号就找不回来了。

### 7.3 验证令牌已生效

Steam 客户端 → 右上角用户名 → **账户明细**。

在"账户安全"下应该显示 **"受 Steam Guard 保护"**。

---

## 第八步：注销可疑设备

### 8.1 注销所有设备授权

1. Steam 客户端 → 右上角用户名 → **账户明细**
2. 找到 **"管理 Steam 令牌"**
3. 点 **"注销所有其他设备的授权"**
4. 确认

这会强制所有设备（包括你自己的其他电脑）重新登录。你的手机上令牌不受影响。

### 8.2 检查最近登录记录

Steam 客户端 → 右上角用户名 → **账户明细** → **"查看我的 Steam 账号数据"** → **"最近的登录历史"**。

检查有没有你不认识的 IP 或地区。

---

## 第九步：检查 API 密钥和交易记录

有些 Steam 劫持病毒会在拿到凭证后，通过 Steam API 自动转移库存。

### 9.1 检查 API 密钥

打开 https://steamcommunity.com/dev/apikey

- 如果页面显示"您尚未注册 Steam Web API 密钥" → ✅ 安全
- 如果有密钥 → 点 **"撤销"**

### 9.2 检查交易记录

Steam 客户端 → 右上角用户名 → **库存** → 右上角 **"更多"** → **"查看库存历史记录"**。

检查有没有你不认识的交易。如果你没开手机令牌，盗号者可以在几秒内转走你的所有可交易物品。

### 9.3 检查市场交易记录

Steam 客户端 → 右上角用户名 → **账户明细** → **"查看市场交易记录"**。

检查有没有异常的市场买卖（用极低价格卖掉你的贵重物品）。

---

## 第十步：最终验证

### 10.1 重启电脑

重启后，再次检查注册表 Run 键（按第一步的路径），确认三个恶意键没有复活。

### 10.2 用检测脚本验证

把本文末尾的 PowerShell 脚本保存为 `check.ps1`，右键 → **使用 PowerShell 运行**。

### 10.3 验证清单

| 检查项 | 预期结果 | 你的结果 |
|--------|---------|:------:|
| 注册表 Run 键 | 无 SteamHelper/SteamService/WindowsUpdateService | ⬜ |
| `%LocalAppData%\ServiceApp` | 不存在 | ⬜ |
| `%LocalAppData%\Programs\01anu7963sndw1ua` | 不存在 | ⬜ |
| `steam.cfg` | 不存在或不含 BootStrapperInhibitAll | ⬜ |
| Steam Guard 手机令牌 | 已开启 | ⬜ |
| 密码 | 已改为新密码 | ⬜ |
| API 密钥 | 无 | ⬜ |

**全部打勾 → ✅ 病毒已彻底清除。**

---

## 附：一键检测脚本（PowerShell）

将以下代码保存为 `check-steam-virus.ps1`，**右键 → 使用 PowerShell 运行**：

```powershell
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Steam 劫持病毒检测脚本 v1.0" -ForegroundColor Cyan
Write-Host " 基于对真实病毒的完整逆向分析" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$found = $false

# 1. 检查注册表 Run 键
Write-Host "[1/6] 检查注册表持久化..." -ForegroundColor Yellow
$runPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Run"
$maliciousKeys = @("SteamHelper", "SteamService", "WindowsUpdateService")

foreach ($key in $maliciousKeys) {
    try {
        $val = Get-ItemProperty -Path $runPath -Name $key -ErrorAction SilentlyContinue
        if ($val) {
            Write-Host "  ❌ 发现恶意注册表键: $key" -ForegroundColor Red
            Write-Host "     值: $($val.$key)" -ForegroundColor DarkRed
            $found = $true
        } else {
            Write-Host "  ✅ $key - 未发现" -ForegroundColor Green
        }
    } catch {
        Write-Host "  ✅ $key - 未发现" -ForegroundColor Green
    }
}

# 2. 检查病毒目录
Write-Host ""
Write-Host "[2/6] 检查病毒文件目录..." -ForegroundColor Yellow
$paths = @(
    "$env:LOCALAPPDATA\Programs\01anu7963sndw1ua",
    "$env:LOCALAPPDATA\ServiceApp",
    "$env:APPDATA\v6vslsk8sfjeiqau",
    "$env:APPDATA\01anu7963sndw1ua",
    "$env:LOCALAPPDATA\Temp\steam"
)

foreach ($path in $paths) {
    if (Test-Path $path) {
        Write-Host "  ❌ 发现残留目录: $path" -ForegroundColor Red
        $found = $true
    } else {
        Write-Host "  ✅ $(Split-Path $path -Leaf) - 不存在" -ForegroundColor Green
    }
}

# 3. 检查 steam.cfg
Write-Host ""
Write-Host "[3/6] 检查 Steam 配置篡改..." -ForegroundColor Yellow
$steamPaths = @(
    "C:\Program Files (x86)\Steam\steam.cfg",
    "C:\Steam\steam.cfg",
    "D:\Steam\steam.cfg",
    "E:\Steam\steam.cfg"
)

foreach ($cfgPath in $steamPaths) {
    if (Test-Path $cfgPath) {
        $cfg = Get-Content $cfgPath -Raw -ErrorAction SilentlyContinue
        if ($cfg -match "BootStrapperInhibitAll=enable" -or $cfg -match "BootStrapperForceSelfUpdate=disable") {
            Write-Host "  ❌ 发现被篡改的 steam.cfg: $cfgPath" -ForegroundColor Red
            $found = $true
        } else {
            Write-Host "  ⚠️  steam.cfg 存在但内容正常" -ForegroundColor DarkYellow
        }
    }
}
if (-not $found) {
    Write-Host "  ✅ 未发现被篡改的 steam.cfg" -ForegroundColor Green
}

# 4. 检查恶意进程
Write-Host ""
Write-Host "[4/6] 检查运行中的恶意进程..." -ForegroundColor Yellow
$badProcs = @("NexusTechNotify", "SteelDungeon", "lizercllaxe", "snapshot", "gameSatorHost")
$running = Get-Process | Where-Object { $_.ProcessName -in $badProcs }

if ($running) {
    foreach ($p in $running) {
        Write-Host "  ❌ 发现恶意进程: $($p.ProcessName) (PID: $($p.Id))" -ForegroundColor Red
        $found = $true
    }
} else {
    Write-Host "  ✅ 未发现恶意进程" -ForegroundColor Green
}

# 5. 检查 Temp 中的 launcher_ 残留
Write-Host ""
Write-Host "[5/6] 检查临时文件残留..." -ForegroundColor Yellow
$tempLaunchers = Get-ChildItem -Path "$env:LOCALAPPDATA\Temp" -Directory -Filter "launcher_*" -ErrorAction SilentlyContinue
if ($tempLaunchers) {
    foreach ($d in $tempLaunchers) {
        Write-Host "  ❌ 发现临时残留: $($d.FullName)" -ForegroundColor Red
        $found = $true
    }
} else {
    Write-Host "  ✅ 未发现临时文件残留" -ForegroundColor Green
}

# 6. 检查 merged_steam.py
Write-Host ""
Write-Host "[6/6] 检查 System32 伪装文件..." -ForegroundColor Yellow
$pyPath = "C:\Windows\System32\merged_steam.py"
if (Test-Path $pyPath) {
    Write-Host "  ❌ 发现恶意 Python 脚本: $pyPath" -ForegroundColor Red
    $found = $true
} else {
    Write-Host "  ✅ 未发现伪装 Python 脚本" -ForegroundColor Green
}

# 结果
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($found) {
    Write-Host " ⚠️  检测到病毒残留！请按照清除指南操作。" -ForegroundColor Red -BackgroundColor Black
    Write-Host ""
    Write-Host " 清除指南: https://github.com/two-s-bro/steam-threat-analysis-platform" -ForegroundColor Yellow
    Write-Host ""
    Write-Host " 快速清除（管理员cmd执行）:" -ForegroundColor Yellow
    Write-Host " reg delete `"HKCU\Software\Microsoft\Windows\CurrentVersion\Run`" /v SteamHelper /f"
    Write-Host " reg delete `"HKCU\Software\Microsoft\Windows\CurrentVersion\Run`" /v SteamService /f"
    Write-Host " reg delete `"HKCU\Software\Microsoft\Windows\CurrentVersion\Run`" /v WindowsUpdateService /f"
} else {
    Write-Host " ✅ 未检测到病毒残留！你的系统是干净的。" -ForegroundColor Green
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
```

---

## 附：防护建议

### 日常习惯

| 建议 | 重要程度 |
|------|:--------:|
| **开启 Steam Guard 手机令牌** | ⭐⭐⭐⭐⭐ |
| **不要下载来路不明的"破解版"游戏、修改器、外挂** | ⭐⭐⭐⭐⭐ |
| **不要在非官方网页输入 Steam 密码** | ⭐⭐⭐⭐⭐ |
| 定期检查 Steam 账户的授权设备列表 | ⭐⭐⭐ |
| 定期检查注册表 Run 键 | ⭐⭐⭐ |
| Steam 密码不要和其他网站共用 | ⭐⭐⭐⭐ |
| 装一个火绒或 Defender，保持更新 | ⭐⭐⭐⭐ |
| 收到 Steam 内"客服案件回复"通知时，去 help.steampowered.com 核实，**不要直接点弹窗** | ⭐⭐⭐⭐⭐ |

### 中了这个病毒但不确定是否干净？

GitHub 仓库: https://github.com/two-s-bro/steam-threat-analysis-platform

里面有：
- 完整 IOC 清单（22 条威胁指标）
- YARA 检测规则（10 条，可以导入火绒/安全工具）
- 12 份详细分析报告

---

> **编写者按：** 这份指南基于对真实 Steam 凭证窃取木马从 dropper → 持久化 → UI 劫持 → 钓鱼的全链路逆向分析。每一个检查点都来自实际运行的恶意代码行为日志。希望这份指南能帮到你。
