package com.steamthreat.controller;

import com.steamthreat.dto.ApiResponse;
import com.steamthreat.dto.DashboardDTO;
import com.steamthreat.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IocService iocService;
    private final TimelineService timelineService;
    private final ComponentService componentService;
    private final C2MonitorService c2Monitor;

    @GetMapping
    public ApiResponse<Map<String, Object>> getDashboard() {
        DashboardDTO dto = DashboardDTO.builder()
                .totalIocs(iocService.findAll().size())
                .highRiskCount(iocService.findAll().stream()
                        .filter(i -> "HIGH".equals(i.getRiskLevel())).count())
                .totalTimelineEvents(timelineService.findAll().size())
                .totalComponents(componentService.findAll().size())
                .iocTypeStats(iocService.statsByType())
                .riskLevelStats(iocService.statsByRiskLevel())
                .phaseStats(timelineService.statsByPhase())
                .recentEvents(timelineService.findAll().stream()
                        .limit(15).toList())
                .build();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dashboard", dto);

        // C2 实时状态
        List<Map<String, Object>> c2Targets = c2Monitor.getTargets().stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("host", t.getHost());
            m.put("label", t.getLabel());
            m.put("port", t.getPort());
            m.put("fullUrl", t.getFullUrl());
            m.put("riskLevel", t.getRiskLevel());
            m.put("online", t.isOnline());
            m.put("latency", t.getLatency());
            m.put("lastCheck", t.getLastCheck() != null ? t.getLastCheck().toString() : null);
            return m;
        }).toList();
        result.put("c2Targets", c2Targets);

        List<Map<String, Object>> c2Events = c2Monitor.getRecentEvents().stream().limit(12).map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", e.getTime() != null ? e.getTime().toString() : null);
            m.put("target", e.getTarget());
            m.put("online", e.isOnline());
            m.put("latency", e.getLatency());
            return m;
        }).toList();
        result.put("c2Events", c2Events);

        return ApiResponse.ok(result);
    }

    @GetMapping("/search")
    public ApiResponse<Object> search(@RequestParam String keyword) {
        var result = new HashMap<String, Object>();
        result.put("iocs", iocService.search(keyword));
        result.put("timeline", timelineService.findAll().stream()
                .filter(t -> t.getDetail() != null && t.getDetail().contains(keyword))
                .limit(20).toList());
        return ApiResponse.ok(result);
    }
}
