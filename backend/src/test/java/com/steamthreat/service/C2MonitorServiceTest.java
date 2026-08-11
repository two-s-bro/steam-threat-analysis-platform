package com.steamthreat.service;

import com.steamthreat.entity.Timeline;
import com.steamthreat.repository.TimelineRepository;
import com.steamthreat.security.ThreatIntelSanitizer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class C2MonitorServiceTest {

    @Test
    void emptyDatabaseRemainsUnknownWithoutFabricatedHistory() {
        TimelineRepository repository = mock(TimelineRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        C2MonitorService service = new C2MonitorService(repository);

        service.init();

        assertEquals(2, service.getTargets().size());
        assertEquals(0, service.getTotalHeartbeats());
        assertNull(service.getFirstHeartbeat());
        assertNull(service.getLastHeartbeat());
        assertTrue(service.getRecentEvents().isEmpty());
        service.getTargets().forEach(target -> {
            assertEquals(ThreatIntelSanitizer.DEFANGED_C2_HOST, target.getHost());
            assertEquals(C2MonitorService.STATUS_NOT_CHECKED, target.getStatus());
            assertTrue(target.getFullUrl().startsWith("hxxps://"));
            assertTrue(target.getHistoryNote().contains("未检查"));
        });
    }

    @Test
    void summarizesOnlyObservedHistoricalHeartbeatRecords() {
        TimelineRepository repository = mock(TimelineRepository.class);
        LocalDateTime first = LocalDateTime.of(2026, 6, 15, 23, 25, 44);
        LocalDateTime last = first.plusMinutes(7);
        when(repository.findAll()).thenReturn(List.of(
                heartbeat(last),
                Timeline.builder().timestamp(first.plusMinutes(1)).phase("INJECT").action("fixture").build(),
                heartbeat(first)
        ));
        C2MonitorService service = new C2MonitorService(repository);

        service.init();

        assertEquals(2, service.getTotalHeartbeats());
        assertEquals(first, service.getFirstHeartbeat());
        assertEquals(last, service.getLastHeartbeat());
        assertEquals(420, service.getInfectionDurationSeconds());
        assertEquals(2, service.getRecentEvents().size());
        service.getRecentEvents().forEach(event ->
                assertEquals(C2MonitorService.STATUS_HISTORICAL_OBSERVATION, event.getStatus()));
        service.getTargets().forEach(target ->
                assertEquals(C2MonitorService.STATUS_NOT_CHECKED, target.getStatus()));
    }

    private Timeline heartbeat(LocalDateTime timestamp) {
        return Timeline.builder()
                .timestamp(timestamp)
                .phase("HEARTBEAT")
                .action("synthetic historical fixture")
                .detail("synthetic historical fixture")
                .build();
    }
}
