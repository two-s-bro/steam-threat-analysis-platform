-- ============================================
-- Steam 威胁分析平台 - 数据库建表脚本
-- 数据库: steam_threat_db
-- ============================================

CREATE DATABASE IF NOT EXISTS steam_threat_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE steam_threat_db;

-- IOC 威胁情报表
DROP TABLE IF EXISTS ioc;
CREATE TABLE ioc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ioc_type VARCHAR(30) NOT NULL COMMENT 'IOC类型: domain/file_path/registry_key/steamid/case_number',
    ioc_value VARCHAR(500) NOT NULL COMMENT 'IOC值',
    risk_level VARCHAR(10) NOT NULL COMMENT '风险等级: HIGH/MEDIUM/LOW',
    source_file VARCHAR(200) COMMENT '来源文件',
    description VARCHAR(1000) COMMENT '描述',
    attack_phase INT COMMENT '攻击阶段 0-3',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 攻击时间线表
DROP TABLE IF EXISTS timeline;
CREATE TABLE timeline (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL COMMENT '事件时间',
    phase VARCHAR(30) NOT NULL COMMENT 'DROPPER/PERSIST/INJECT/PHISH/HEARTBEAT',
    action VARCHAR(300) NOT NULL COMMENT '动作描述',
    detail TEXT COMMENT '日志原文',
    component_id BIGINT COMMENT '关联组件ID',
    ioc_id BIGINT COMMENT '关联IOC ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 病毒组件表
DROP TABLE IF EXISTS component_info;
CREATE TABLE component_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '组件名称',
    role VARCHAR(50) NOT NULL COMMENT 'dropper/controller/injector/payload/c2',
    tech_stack VARCHAR(100) COMMENT '技术栈',
    file_size BIGINT COMMENT '文件大小(字节)',
    parent_id BIGINT COMMENT '父组件ID',
    attack_phase INT NOT NULL COMMENT '攻击阶段 0-3',
    description VARCHAR(500) COMMENT '功能描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
