package com.steamthreat.service;

import com.steamthreat.entity.Timeline;
import com.steamthreat.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineRepository timelineRepository;

    public List<Timeline> findAll() {
        return timelineRepository.findAllByOrderByTimestampAsc();
    }

    public Optional<Timeline> findById(Long id) {
        return timelineRepository.findById(id);
    }

    public Timeline save(Timeline timeline) {
        return timelineRepository.save(timeline);
    }

    public List<Timeline> findByPhase(String phase) {
        return timelineRepository.findByPhaseOrderByTimestampAsc(phase);
    }

    public List<Timeline> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return timelineRepository.findByTimestampBetweenOrderByTimestampAsc(start, end);
    }

    public Map<String, Long> statsByPhase() {
        List<Timeline> all = timelineRepository.findAll();
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Timeline t : all) {
            stats.merge(t.getPhase(), 1L, Long::sum);
        }
        return stats;
    }
}
