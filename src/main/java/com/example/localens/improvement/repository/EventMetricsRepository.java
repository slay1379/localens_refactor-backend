package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.domain.EventMetricsId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventMetricsRepository extends JpaRepository<EventMetrics, EventMetricsId> {
    List<EventMetrics> findByMetricsUuidIn(List<String> metricsUuids);

    @Query("SELECT em.eventUuid FROM EventMetrics em WHERE em.metricsUuid IN :metricsUuids")
    List<String> findEventUuidByMetricsUuidIn(@Param("metricsUuids") List<String> metricsUuids);

}
