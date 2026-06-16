package com.steamthreat.repository;

import com.steamthreat.entity.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimelineRepository extends JpaRepository<Timeline, Long> {

    /** 按阶段查询并按时间排序 */
    List<Timeline> findByPhaseOrderByTimestampAsc(String phase);

    /** 按时间范围查询 */
    List<Timeline> findByTimestampBetweenOrderByTimestampAsc(
            LocalDateTime start, LocalDateTime end);

    /** 按关联组件查询 */
    List<Timeline> findByComponentIdOrderByTimestampAsc(Long componentId);

    /** 全部按时间排序 */
    List<Timeline> findAllByOrderByTimestampAsc();
}
