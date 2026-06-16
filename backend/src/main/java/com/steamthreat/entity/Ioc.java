package com.steamthreat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * IOC 威胁情报表
 * 存储从病毒分析中提取的所有威胁指标
 */
@Entity
@Table(name = "ioc")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ioc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** IOC 类型: domain / file_path / registry_key / steamid / case_number */
    @Column(nullable = false, length = 30)
    private String iocType;

    /** IOC 值: nexustechsolution.top */
    @Column(nullable = false, length = 500)
    private String iocValue;

    /** 风险等级: HIGH / MEDIUM / LOW */
    @Column(nullable = false, length = 10)
    private String riskLevel;

    /** 来源文件: SteamPatchToast_patch.lo */
    @Column(length = 200)
    private String sourceFile;

    /** 描述 */
    @Column(length = 1000)
    private String description;

    /** 关联的攻击阶段 0-3 */
    private Integer attackPhase;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
