package com.steamthreat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 病毒组件关系表
 * 记录病毒所有组件及其父子调用关系
 */
@Entity
@Table(name = "component_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 组件名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 组件角色: dropper / controller / injector / payload / c2 */
    @Column(nullable = false, length = 50)
    private String role;

    /** 技术栈 */
    @Column(length = 100)
    private String techStack;

    /** 文件大小 (字节) */
    private Long fileSize;

    /** 父组件ID (谁调用了它) */
    private Long parentId;

    /** 所处攻击阶段 0-3 */
    @Column(nullable = false)
    private Integer attackPhase;

    /** 功能描述 */
    @Column(length = 500)
    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
