package com.steamthreat.controller;

import com.steamthreat.dto.ApiResponse;
import com.steamthreat.entity.Timeline;
import com.steamthreat.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public ApiResponse<List<Timeline>> list() {
        return ApiResponse.ok(timelineService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Timeline> get(@PathVariable Long id) {
        return timelineService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "事件不存在"));
    }

    @GetMapping("/phase/{phase}")
    public ApiResponse<List<Timeline>> byPhase(@PathVariable String phase) {
        return ApiResponse.ok(timelineService.findByPhase(phase));
    }

    @GetMapping("/range")
    public ApiResponse<List<Timeline>> byTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ApiResponse.ok(timelineService.findByTimeRange(start, end));
    }
}
