package com.steamthreat.repository;

import com.steamthreat.entity.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, Long> {

    /** 按角色查询 */
    List<ComponentEntity> findByRole(String role);

    /** 按攻击阶段查询 */
    List<ComponentEntity> findByAttackPhase(Integer attackPhase);

    /** 查子组件 */
    List<ComponentEntity> findByParentId(Long parentId);

    /** 查顶层组件 (无父组件的) */
    List<ComponentEntity> findByParentIdIsNull();
}
