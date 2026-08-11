package com.steamthreat.service;

import com.steamthreat.entity.Timeline;
import com.steamthreat.repository.TimelineRepository;
import com.steamthreat.security.ThreatIntelSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Loads only the small, sanitized fixtures bundled with this public project. */
@Service
@RequiredArgsConstructor
public class DataImportService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final TimelineRepository timelineRepository;

    /**
     * Imports public fixtures idempotently. No filesystem path or private evidence directory is read.
     */
    @Transactional
    public synchronized Map<String, Object> importBundledSamples() {
        Set<String> existingDetails = new HashSet<>();
        timelineRepository.findAll().stream()
                .map(Timeline::getDetail)
                .filter(java.util.Objects::nonNull)
                .forEach(existingDetails::add);

        Map<String, Object> result = new LinkedHashMap<>();
        int patchCount = saveNew(parsePatch(readFixture("samples/patch.log"), 2L), existingDetails);
        int startupCount = saveNew(parseStartup(readFixture("samples/startup.log")), existingDetails);
        int downloadCount = saveNew(parseDownload(readFixture("samples/download.log")), existingDetails);

        result.put("patchSample", patchCount + " 条新增");
        result.put("startupSample", startupCount + " 条新增");
        result.put("heartbeatSample", downloadCount + " 条新增");
        result.put("total", patchCount + startupCount + downloadCount);
        result.put("source", "bundled-redacted-fixtures");
        return result;
    }

    private List<String> readFixture(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new IllegalStateException("无法读取内置脱敏样例: " + classpathLocation, e);
        }
    }

    private int saveNew(List<Timeline> candidates, Set<String> existingDetails) {
        List<Timeline> unseen = candidates.stream()
                .filter(event -> existingDetails.add(event.getDetail()))
                .toList();
        timelineRepository.saveAll(unseen);
        return unseen.size();
    }

    private List<Timeline> parsePatch(List<String> lines, Long componentId) {
        List<Timeline> events = new ArrayList<>();
        for (String rawLine : lines) {
            String line = ThreatIntelSanitizer.sanitize(rawLine);
            if (line.length() < 20 || !line.startsWith("202")) continue;
            try {
                LocalDateTime time = LocalDateTime.parse(line.substring(0, 19), DT);
                String content = line.substring(20).trim();
                events.add(event(time, detectPhase(content), content, line, componentId));
            } catch (RuntimeException ignored) {
                // Public fixtures may include explanatory lines; malformed records are skipped.
            }
        }
        return events;
    }

    private List<Timeline> parseStartup(List<String> lines) {
        List<Timeline> events = new ArrayList<>();
        for (String rawLine : lines) {
            String line = ThreatIntelSanitizer.sanitize(rawLine);
            if (!line.startsWith("[") || !line.contains("]")) continue;
            try {
                LocalDateTime time = LocalDateTime.parse(line.substring(1, line.indexOf(']')), DT);
                String content = line.substring(line.indexOf(']') + 1).trim();
                events.add(event(time, "INJECT", content, line, 2L));
            } catch (RuntimeException ignored) {
                // Skip malformed public fixture lines.
            }
        }
        return events;
    }

    private List<Timeline> parseDownload(List<String> lines) {
        List<Timeline> events = new ArrayList<>();
        for (String rawLine : lines) {
            String line = ThreatIntelSanitizer.sanitize(rawLine);
            if (line.length() < 20 || !line.startsWith("202")) continue;
            try {
                LocalDateTime time = LocalDateTime.parse(line.substring(0, 19), DT);
                String content = line.substring(20).trim().replace("----", " | ");
                events.add(event(time, "HEARTBEAT", "历史心跳: " + content, line, 8L));
            } catch (RuntimeException ignored) {
                // Skip malformed public fixture lines.
            }
        }
        return events;
    }

    private Timeline event(LocalDateTime time, String phase, String content, String detail, Long componentId) {
        String action = content.length() > 280 ? content.substring(0, 280) : content;
        return Timeline.builder()
                .timestamp(time)
                .phase(phase)
                .action(action)
                .detail(detail)
                .componentId(componentId)
                .build();
    }

    private String detectPhase(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("download") || lower.contains("dropper") || lower.contains("steel")) return "DROPPER";
        if (lower.contains("url help") || lower.contains("url support") || lower.contains("[.]")) return "PHISH";
        if (lower.contains("watch") || lower.contains("heartbeat") || lower.contains("匹配")) return "HEARTBEAT";
        return "INJECT";
    }
}
