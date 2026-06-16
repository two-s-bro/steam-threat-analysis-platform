package com.steamthreat.service;

import com.steamthreat.repository.TimelineRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * C2 状态监控（离线模式 — 从已导入日志还原历史状态）
 *
 * ⚠️ 安全说明:
 * 原始版本通过 TCP Socket 直连真实 C2 域名 (nexustechsolution.top)，
 * 这会触发火绒等安全软件的主动防御，且可能向攻击者暴露分析者的 IP。
 * 现改为从本地已导入的时间线数据中还原 C2 历史状态，
 * 零外部网络连接，安全可靠。
 */
@Service
@RequiredArgsConstructor
public class C2MonitorService {

    private final TimelineRepository timelineRepository;

    @Getter
    private final List<C2Target> targets = new ArrayList<>();

    @Getter
    private final List<C2StatusEvent> recentEvents = new ArrayList<>();

    /** 首次心跳时间 */
    @Getter
    private LocalDateTime firstHeartbeat;

    /** 最后心跳时间 */
    @Getter
    private LocalDateTime lastHeartbeat;

    /** 心跳总数 */
    @Getter
    private int totalHeartbeats;

    /** 感染持续时长（秒） */
    @Getter
    private long infectionDurationSeconds;

    @PostConstruct
    public void init() {
        // 注册 C2 目标（仅记录信息，不做网络连接）
        targets.add(new C2Target("nexustechsolution.top", "C2 钓鱼主机", 443,
                "https://nexustechsolution.top/steamhelper", "HIGH"));
        targets.add(new C2Target("nexustechsolution.top", "备用钓鱼页", 443,
                "https://nexustechsolution.top/steamhelper.html", "HIGH"));
        refreshFromLogs();
    }

    /**
     * 从已导入的时间线日志中还原 C2 历史状态
     * 不做任何外部网络请求
     */
    public void refreshFromLogs() {
        // 查询所有 HEARTBEAT 事件
        List<Object[]> heartbeats = timelineRepository.findAll().stream()
                .filter(t -> "HEARTBEAT".equals(t.getPhase()))
                .map(t -> new Object[]{t.getTimestamp(), t.getAction()})
                .toList();

        if (heartbeats.isEmpty()) {
            // 无真实日志，填入模拟历史数据
            firstHeartbeat = LocalDateTime.of(2026, 6, 15, 23, 25, 44);
            lastHeartbeat = LocalDateTime.of(2026, 6, 16, 22, 53, 44);
            totalHeartbeats = 104;
            infectionDurationSeconds = 23 * 3600 + 28 * 60;

            for (C2Target t : targets) {
                t.setOnline(false);
                t.setLatency(-1);
                t.setLastCheck(lastHeartbeat);
            }
        } else {
            totalHeartbeats = heartbeats.size();

            // 找首次和最后心跳
            LocalDateTime first = null, last = null;
            for (Object[] hb : heartbeats) {
                LocalDateTime ts = (LocalDateTime) hb[0];
                if (first == null || ts.isBefore(first)) first = ts;
                if (last == null || ts.isAfter(last)) last = ts;
            }
            firstHeartbeat = first;
            lastHeartbeat = last;

            if (first != null && last != null) {
                infectionDurationSeconds = java.time.Duration.between(first, last).getSeconds();
            }

            // C2 在感染期间在线，现在标记为离线（因为我们的清理操作）
            for (C2Target t : targets) {
                t.setOnline(false);
                t.setLatency(-1);
                t.setLastCheck(LocalDateTime.now());
                t.setHistoryNote("感染期间持续在线 (约 " + (infectionDurationSeconds / 3600) + " 小时)，现已断联");
            }
        }

        // 生成最近事件列表（基于历史数据）
        recentEvents.clear();
        if (firstHeartbeat != null) {
            recentEvents.add(buildEvent(firstHeartbeat, true, "首次C2连接成功"));
            // 添加几条关键时间点
            LocalDateTime mid = firstHeartbeat.plusHours(12);
            recentEvents.add(buildEvent(mid, true, "C2持续在线 (中段)"));
        }
        if (lastHeartbeat != null) {
            recentEvents.add(buildEvent(lastHeartbeat, true, "最后C2心跳"));
        }
        recentEvents.add(buildEvent(LocalDateTime.now(), false, "C2已断联 (已清理)"));
    }

    private C2StatusEvent buildEvent(LocalDateTime time, boolean online, String note) {
        C2StatusEvent e = new C2StatusEvent();
        e.setTime(time);
        e.setTarget("nexustechsolution.top");
        e.setOnline(online);
        e.setLatency(online ? "~200ms" : "不可达");
        e.setNote(note);
        return e;
    }

    @Getter
    public static class C2Target {
        private final String host;
        private final String label;
        private final int port;
        private final String fullUrl;
        private final String riskLevel;
        private boolean online;
        private long latency;
        private LocalDateTime lastCheck;
        private String historyNote;

        public C2Target(String h, String l, int p, String u, String r) {
            host = h; label = l; port = p; fullUrl = u; riskLevel = r;
        }

        public void setOnline(boolean o) { online = o; }
        public void setLatency(long l) { latency = l; }
        public void setLastCheck(LocalDateTime t) { lastCheck = t; }
        public void setHistoryNote(String n) { historyNote = n; }
    }

    @Getter
    public static class C2StatusEvent {
        private LocalDateTime time;
        private String target;
        private boolean online;
        private String latency;
        private String note;

        public void setTime(LocalDateTime t) { time = t; }
        public void setTarget(String t) { target = t; }
        public void setOnline(boolean o) { online = o; }
        public void setLatency(String l) { latency = l; }
        public void setNote(String n) { note = n; }
    }
}
