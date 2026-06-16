package com.steamthreat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 攻击时间线
 * 从病毒运行日志还原的完整攻击时间线
 */
@Entity
@Table(name = "timeline")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 事件时间戳 */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** 攻击阶段: DROPPER / PERSIST / INJECT / PHISH / HEARTBEAT */
    @Column(nullable = false, length = 30)
    private String phase;

    /** 动作描述: 强杀Steam进程 */
    @Column(nullable = false, length = 300)
    private String action;

    /** 详细日志原文 */
    @Column(columnDefinition = "TEXT")
    private String detail;

    /** 关联组件ID */
    private Long componentId;

    /** 关联IOC ID */
    private Long iocId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
