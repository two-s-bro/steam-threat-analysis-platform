package com.steamthreat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Steam 威胁分析平台
 * 基于对真实 Steam 劫持病毒的反向分析构建
 */
@SpringBootApplication
public class SteamThreatApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamThreatApplication.class, args);
        System.out.println("""

                ╔══════════════════════════════════════════════╗
                ║   🔬 Steam 威胁分析平台 v1.0 已启动           ║
                ║   IOC 库 · 攻击链 · 时间线 · 组件关系         ║
                ║   http://localhost:8088                     ║
                ╚══════════════════════════════════════════════╝
                """);
    }
}
