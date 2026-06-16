package com.steamthreat.repository;

import com.steamthreat.entity.YaraRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface YaraRuleRepository extends JpaRepository<YaraRule, Long> {

    List<YaraRule> findByEnabledTrue();
    List<YaraRule> findByTargetType(String targetType);
    List<YaraRule> findByRiskLevel(String riskLevel);

    /** 规则名称模糊搜索 */
    List<YaraRule> findByRuleNameContainingIgnoreCase(String keyword);
}
