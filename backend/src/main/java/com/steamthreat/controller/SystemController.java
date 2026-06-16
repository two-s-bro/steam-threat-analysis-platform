package com.steamthreat.controller;

import com.steamthreat.dto.ApiResponse;
import com.steamthreat.service.C2MonitorService;
import com.steamthreat.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final C2MonitorService c2Monitor;
    private final DataImportService dataImport;

    /** C2 历史状态 (离线分析，不连接真实C2) */
    @GetMapping("/c2-status")
    public ApiResponse<Map<String, Object>> getC2Status() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("targets", c2Monitor.getTargets().stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("host", t.getHost());
            m.put("label", t.getLabel());
            m.put("port", t.getPort());
            m.put("fullUrl", t.getFullUrl());
            m.put("riskLevel", t.getRiskLevel());
            m.put("online", t.isOnline());
            m.put("latency", t.getLatency());
            m.put("lastCheck", t.getLastCheck() != null ? t.getLastCheck().toString() : null);
            m.put("historyNote", t.getHistoryNote());
            return m;
        }).toList());

        data.put("recentEvents", c2Monitor.getRecentEvents().stream().limit(20).map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", e.getTime() != null ? e.getTime().toString() : null);
            m.put("target", e.getTarget());
            m.put("online", e.isOnline());
            m.put("latency", e.getLatency());
            m.put("note", e.getNote());
            return m;
        }).toList());

        // 历史统计数据
        Map<String, Object> history = new LinkedHashMap<>();
        history.put("firstHeartbeat", c2Monitor.getFirstHeartbeat() != null
                ? c2Monitor.getFirstHeartbeat().toString() : null);
        history.put("lastHeartbeat", c2Monitor.getLastHeartbeat() != null
                ? c2Monitor.getLastHeartbeat().toString() : null);
        history.put("totalHeartbeats", c2Monitor.getTotalHeartbeats());
        history.put("durationHours", c2Monitor.getInfectionDurationSeconds() / 3600);
        data.put("history", history);

        return ApiResponse.ok(data);
    }

    /** 从本地日志刷新 C2 历史状态 (不连接外部) */
    @PostMapping("/c2-refresh")
    public ApiResponse<Map<String, Object>> refreshFromLogs() {
        c2Monitor.refreshFromLogs();
        return getC2Status();
    }

    /** 导入真实日志 */
    @PostMapping("/import-logs")
    public ApiResponse<Map<String, Object>> importLogs() {
        try {
            return ApiResponse.ok("真实日志导入完成", dataImport.importAllRealLogs());
        } catch (Exception e) {
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
    }
}
