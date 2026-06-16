package com.steamthreat.repository;

import com.steamthreat.entity.Ioc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IocRepository extends JpaRepository<Ioc, Long> {

    /** 按类型查询 */
    List<Ioc> findByIocType(String iocType);

    /** 按风险等级查询 */
    List<Ioc> findByRiskLevel(String riskLevel);

    /** 按攻击阶段查询 */
    List<Ioc> findByAttackPhase(Integer attackPhase);

    /** 模糊搜索 IOC 值 */
    List<Ioc> findByIocValueContaining(String keyword);

    /** 统计各类型数量 */
    @Query("SELECT i.iocType, COUNT(i) FROM Ioc i GROUP BY i.iocType")
    List<Object[]> countByType();

    /** 统计各风险等级数量 */
    @Query("SELECT i.riskLevel, COUNT(i) FROM Ioc i GROUP BY i.riskLevel")
    List<Object[]> countByRiskLevel();
}
