package com.steamthreat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * YARA 规则表
 * 存储用于检测 Steam 劫持病毒的 YARA 规则
 */
@Entity
@Table(name = "yara_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YaraRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 规则名称 */
    @Column(nullable = false, unique = true, length = 100)
    private String ruleName;

    /** 规则描述 */
    @Column(length = 500)
    private String description;

    /** 风险等级 */
    @Column(nullable = false, length = 10)
    private String riskLevel;

    /** 目标类型: file / registry / memory / url */
    @Column(nullable = false, length = 20)
    private String targetType;

    /** YARA 规则体 (完整规则文本) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleBody;

    /** 匹配样本 (检测到什么文件/值时报) */
    @Column(length = 500)
    private String matchSample;

    /** 是否启用 */
    @Builder.Default
    private Boolean enabled = true;

    /** 匹配次数统计 */
    @Builder.Default
    private Long matchCount = 0L;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
