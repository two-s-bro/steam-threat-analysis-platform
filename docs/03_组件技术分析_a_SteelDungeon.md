# 组件 a — SteelDungeon.exe (Dropper / 下载器)

---

## 基本信息

| 属性 | 值 |
|------|-----|
| 文件名 | `SteelDungeon.exe` |
| 技术栈 | Python 3.14 + PyInstaller |
| exe 体积 | 22 KB |
| python314.dll | 6.5 MB |
| python3.dll | 72 KB |
| library.zip | ~3.8 MB (标准库) |
| payload.bin | 38 KB (加密载荷) |
| ungeond.rar | 15.8 MB (加密压缩包) |
| _svc_launch.bat | 155 B (启动脚本) |

---

## 启动脚本 (_svc_launch.bat)

```batch
@echo off
cd /d "C:\Users\周\AppData\Local\Programs\01anu7963sndw1ua"
start "" "C:\Users\周\AppData\Local\Programs\01anu7963sndw1ua\SteelDungeon.exe"
```

---

## Python 依赖库 (lib/ 目录)

### 加密/安全类
| 库 | 用途 |
|----|------|
| `Crypto/` (PyCryptodome) | AES/RSA/ECC 加密解密 |
| `cryptography/` | 现代加密库 (OpenSSL 绑定) |
| `cffi/` | C 扩展接口 (调用 .dll/.pyd) |
| `certifi/` | SSL 证书验证 |
| `jwt/` | JWT 令牌处理 (Steam Session) |

### 网络通信类
| 库 | 用途 |
|----|------|
| `requests/` | HTTP 请求 |
| `urllib3/` | URL 处理 |
| `h2/` | HTTP/2 协议支持 |
| `hpack/` | HTTP/2 头部压缩 |
| `hyperframe/` | HTTP/2 帧处理 |

### Steam 交互类
| 库 | 用途 |
|----|------|
| `vdf/` | Valve Data Format 解析 (读取 Steam 本地配置) |

### Windows API 类
| 库 | 用途 |
|----|------|
| `win32api.pyd` | Windows API 调用 |
| `win32crypt.pyd` | Windows 凭据管理 (凭据窃取) |
| `_win32sysloader.pyd` | 系统级 DLL 加载器 |
| `pywintypes314.dll` | Windows 类型支持 |

### SSL/TLS 类 (native .pyd/.dll)
| 文件 | 用途 |
|------|------|
| `_ssl.pyd` | SSL/TLS 加密通信 |
| `_socket.pyd` | Socket 网络通信 |
| `libcrypto-3.dll` | OpenSSL 加密库 |
| `libssl-3.dll` | OpenSSL SSL 库 |

---

## 推测功能

1. **自解压**: 从 PyInstaller 打包中提取 Python 运行时到安装目录
2. **持久化**: 写入注册表 Run 键
3. **下载**: 从 C2 拉取 NexusTechNotify.exe、locale_patch.dll 等组件
4. **解密**: 使用 Crypto 库解密 payload.bin 和 ungeond.rar
5. **VDF 解析**: 读取 Steam 本地配置文件获取受害者信息
6. **上报**: 向 C2 注册受害者
7. **启动**: 执行 NexusTechNotify.exe 进入 Phase 2

---

## 待逆向内容

- `payload.bin` — 需提取 SteelDungeon.exe 中的解密密钥
- `ungeond.rar` — 15.8MB 加密压缩包，疑似包含 ServiceApp 完整组件
- `library.zip` — PyInstaller 标准库包，可解压查看 Python 源码
