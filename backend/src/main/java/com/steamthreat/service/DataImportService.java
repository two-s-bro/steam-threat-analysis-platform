package com.steamthreat.service;

import com.steamthreat.entity.Timeline;
import com.steamthreat.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 真实日志导入服务
 * 从 virus 项目文件夹读取实际日志文件并解析入库
 */
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final TimelineRepository timelineRepository;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_BASE = "D:/Users/周/Desktop/诈骗项目600";

    /**
     * 清空并重新导入全部真实日志
     */
    @Transactional
    public Map<String, Object> importAllRealLogs() {
        Map<String, Object> result = new LinkedHashMap<>();
        int total = 0;

        // 1. 补丁日志
        String patchPath = LOG_BASE + "/ServiceApp/SteamPatchToast_patch.lo";
        int patchCount = importPatchLog(patchPath, 2L);
        result.put("patchLog", patchCount + " 条");
        total += patchCount;

        // 2. 启动日志
        String startupPath = LOG_BASE + "/ServiceApp/SteamPatchToast_startup.lo";
        int startupCount = importStartupLog(startupPath);
        result.put("startupLog", startupCount + " 条");
        total += startupCount;

        // 3. C2 下载日志
        String dlPath = LOG_BASE + "/ServiceApp/_downloadlog/76561198892973993.txt";
        int dlCount = importDownloadLog(dlPath);
        result.put("downloadLog", dlCount + " 条");
        total += dlCount;

        result.put("total", total + " 条真实日志导入完成");
        return result;
    }

    private int importPatchLog(String path, Long componentId) {
        int count = 0;
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            List<Timeline> batch = new ArrayList<>();

            for (String line : lines) {
                if (line.length() < 20 || !line.startsWith("202")) continue;
                try {
                    LocalDateTime time = LocalDateTime.parse(line.substring(0, 19), DT);
                    String content = line.substring(20).trim();
                    String phase = detectPhase(content);
                    String action = content.length() > 280 ? content.substring(0, 280) : content;

                    batch.add(Timeline.builder()
                            .timestamp(time).phase(phase).action(action)
                            .detail(line).componentId(componentId).build());
                    count++;
                } catch (Exception ignored) {}
            }
            timelineRepository.saveAll(batch);
        } catch (IOException e) {
            throw new RuntimeException("读取补丁日志失败: " + path, e);
        }
        return count;
    }

    private int importStartupLog(String path) {
        int count = 0;
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            List<Timeline> batch = new ArrayList<>();

            for (String line : lines) {
                if (!line.startsWith("[")) continue;
                try {
                    String inner = line.substring(1, line.indexOf("]"));
                    // 格式: [2026-06-15 23:25:53] ...
                    int spaceIdx = inner.indexOf(" ");
                    String timeStr = inner; // full datetime
                    LocalDateTime time = LocalDateTime.parse(timeStr, DT);
                    String content = line.substring(line.indexOf("]") + 2).trim();

                    batch.add(Timeline.builder()
                            .timestamp(time).phase("INJECT")
                            .action(content.length() > 280 ? content.substring(0, 280) : content)
                            .detail(line).componentId(2L).build());
                    count++;
                } catch (Exception ignored) {}
            }
            timelineRepository.saveAll(batch);
        } catch (IOException e) {
            throw new RuntimeException("读取启动日志失败: " + path, e);
        }
        return count;
    }

    private int importDownloadLog(String path) {
        int count = 0;
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            List<Timeline> batch = new ArrayList<>();

            for (String line : lines) {
                if (!line.startsWith("202")) continue;
                try {
                    LocalDateTime time = LocalDateTime.parse(line.substring(0, 19), DT);
                    int steamIdEnd = line.indexOf("----", 20);
                    String steamId = steamIdEnd > 0 ? line.substring(20, steamIdEnd).trim() : line.substring(20).trim();
                    String status = steamIdEnd > 0 ? line.substring(steamIdEnd).replace("----", " | ") : "";

                    batch.add(Timeline.builder()
                            .timestamp(time).phase("HEARTBEAT")
                            .action("C2心跳: " + steamId + status)
                            .detail(line).componentId(8L).build());
                    count++;
                } catch (Exception ignored) {}
            }
            timelineRepository.saveAll(batch);
        } catch (IOException e) {
            throw new RuntimeException("读取下载日志失败: " + path, e);
        }
        return count;
    }

    private String detectPhase(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("download") || lower.contains("dropper") || lower.contains("steel")) return "DROPPER";
        if (lower.contains("kill") || lower.contains("steam.exe") && lower.contains("silent")) return "INJECT";
        if (lower.contains("patch") || lower.contains("cfg")) return "INJECT";
        if (lower.contains("url help") || lower.contains("url support") || lower.contains("nexustechsolution")) return "PHISH";
        if (lower.contains("watch") || lower.contains("heartbeat") || lower.contains("匹配")) return "HEARTBEAT";
        if (lower.contains("profile") || lower.contains("steam path")) return "DROPPER";
        return "INJECT";
    }
}
