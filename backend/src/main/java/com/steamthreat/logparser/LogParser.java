package com.steamthreat.logparser;

import com.steamthreat.entity.Timeline;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * 病毒日志解析器
 * 解析 SteamPatchToast_patch.lo 和 _downloadlog/*.txt
 */
public class LogParser {

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析补丁日志
     * 格式: 2026-06-15 23:25:53 steam path: E:\steam
     */
    public static List<Timeline> parsePatchLog(String filePath) throws IOException {
        List<Timeline> events = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (String line : lines) {
            if (line.length() < 20 || !line.startsWith("202")) continue;

            try {
                String timeStr = line.substring(0, 19);
                LocalDateTime time = LocalDateTime.parse(timeStr, DT_FORMAT);
                String content = line.substring(20).trim();

                String phase = detectPhase(content);
                String action = content.length() > 200 ? content.substring(0, 200) : content;

                events.add(Timeline.builder()
                        .timestamp(time)
                        .phase(phase)
                        .action(action)
                        .detail(line)
                        .build());
            } catch (Exception ignored) {}
        }
        return events;
    }

    /**
     * 解析 C2 下载日志
     * 格式: 2026-06-15 23:25:44 76561198892973993----匹配成功----下载成功----执行成功
     */
    public static List<Timeline> parseDownloadLog(String filePath) throws IOException {
        List<Timeline> events = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (String line : lines) {
            if (!line.startsWith("202")) continue;

            try {
                String timeStr = line.substring(0, 19);
                LocalDateTime time = LocalDateTime.parse(timeStr, DT_FORMAT);

                String[] parts = line.substring(20).split("----");
                String steamId = parts.length > 0 ? parts[0].trim() : "";

                events.add(Timeline.builder()
                        .timestamp(time)
                        .phase("HEARTBEAT")
                        .action("C2心跳: " + steamId)
                        .detail(line)
                        .build());
            } catch (Exception ignored) {}
        }
        return events;
    }

    private static String detectPhase(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("download") || lower.contains("dropper")) return "DROPPER";
        if (lower.contains("registry") || lower.contains("run key") || lower.contains("startup")) return "PERSIST";
        if (lower.contains("patch") || lower.contains("inject") || lower.contains("kill") || lower.contains("chunk")) return "INJECT";
        if (lower.contains("phish") || lower.contains("toast") || lower.contains("helpapp") || lower.contains("url")) return "PHISH";
        if (lower.contains("watch") || lower.contains("heartbeat") || lower.contains("cfg")) return "HEARTBEAT";
        return "INJECT";
    }
}
