package com.steamthreat.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘聚合数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    /** IOC 总数 */
    private long totalIocs;

    /** 高危 IOC 数量 */
    private long highRiskCount;

    /** 时间线事件总数 */
    private long totalTimelineEvents;

    /** 组件总数 */
    private long totalComponents;

    /** 各类型 IOC 统计 */
    private Map<String, Long> iocTypeStats;

    /** 各风险等级统计 */
    private Map<String, Long> riskLevelStats;

    /** 各阶段事件统计 */
    private Map<String, Long> phaseStats;

    /** 最近的事件列表 */
    private List<?> recentEvents;
}
