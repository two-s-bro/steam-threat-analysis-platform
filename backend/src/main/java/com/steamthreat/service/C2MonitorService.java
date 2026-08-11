package com.steamthreat.service;

import com.steamthreat.entity.Timeline;
import com.steamthreat.repository.TimelineRepository;
import com.steamthreat.security.ThreatIntelSanitizer;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Summarizes historical, redacted timeline records without resolving or contacting any external host.
 * Current infrastructure state is deliberately represented as NOT_CHECKED.
 */
@Service
@RequiredArgsConstructor
public class C2MonitorService {

    public static final String STATUS_NOT_CHECKED = "NOT_CHECKED";
    public static final String STATUS_HISTORICAL_OBSERVATION = "HISTORICAL_OBSERVATION";

    private final TimelineRepository timelineRepository;

    @Getter
    private final List<C2Target> targets = new ArrayList<>();

    @Getter
    private final List<C2StatusEvent> recentEvents = new ArrayList<>();

    @Getter
    private LocalDateTime firstHeartbeat;

    @Getter
    private LocalDateTime lastHeartbeat;

    @Getter
    private int totalHeartbeats;

    @Getter
    private long infectionDurationSeconds;

    @PostConstruct
    public void init() {
        targets.clear();
        targets.add(new C2Target(ThreatIntelSanitizer.DEFANGED_C2_HOST, "历史 C2 钓鱼主机", 443,
                ThreatIntelSanitizer.DEFANGED_C2_BASE_URL + "/steamhelper", "HIGH"));
        targets.add(new C2Target(ThreatIntelSanitizer.DEFANGED_C2_HOST, "历史备用钓鱼页", 443,
                ThreatIntelSanitizer.DEFANGED_C2_BASE_URL + "/steamhelper.html", "HIGH"));
        refreshFromLogs();
    }

    /** Recomputes historical summary fields from already-redacted database records. */
    public void refreshFromLogs() {
        List<Timeline> heartbeats = timelineRepository.findAll().stream()
                .filter(t -> "HEARTBEAT".equals(t.getPhase()))
                .filter(t -> Objects.nonNull(t.getTimestamp()))
                .sorted(Comparator.comparing(Timeline::getTimestamp))
                .toList();

        recentEvents.clear();
        totalHeartbeats = heartbeats.size();
        firstHeartbeat = null;
        lastHeartbeat = null;
        infectionDurationSeconds = 0;

        String historyNote;
        if (heartbeats.isEmpty()) {
            historyNote = "尚无脱敏历史心跳记录；当前网络状态未检查";
        } else {
            firstHeartbeat = heartbeats.get(0).getTimestamp();
            lastHeartbeat = heartbeats.get(heartbeats.size() - 1).getTimestamp();
            infectionDurationSeconds = Duration.between(firstHeartbeat, lastHeartbeat).getSeconds();
            historyNote = "统计仅来自已入库的脱敏历史记录；当前网络状态未检查";

            recentEvents.add(new C2StatusEvent(firstHeartbeat,
                    ThreatIntelSanitizer.DEFANGED_C2_HOST,
                    STATUS_HISTORICAL_OBSERVATION,
                    "最早的已入库历史心跳"));
            if (!lastHeartbeat.equals(firstHeartbeat)) {
                recentEvents.add(new C2StatusEvent(lastHeartbeat,
                        ThreatIntelSanitizer.DEFANGED_C2_HOST,
                        STATUS_HISTORICAL_OBSERVATION,
                        "最后的已入库历史心跳"));
            }
        }

        targets.forEach(target -> target.setHistoryNote(historyNote));
    }

    @Getter
    public static class C2Target {
        private final String host;
        private final String label;
        private final int port;
        private final String fullUrl;
        private final String riskLevel;
        private final String status = STATUS_NOT_CHECKED;
        private String historyNote;

        public C2Target(String host, String label, int port, String fullUrl, String riskLevel) {
            this.host = host;
            this.label = label;
            this.port = port;
            this.fullUrl = fullUrl;
            this.riskLevel = riskLevel;
        }

        public void setHistoryNote(String historyNote) {
            this.historyNote = historyNote;
        }
    }

    @Getter
    public static class C2StatusEvent {
        private final LocalDateTime time;
        private final String target;
        private final String status;
        private final String note;

        public C2StatusEvent(LocalDateTime time, String target, String status, String note) {
            this.time = time;
            this.target = target;
            this.status = status;
            this.note = note;
        }
    }
}
