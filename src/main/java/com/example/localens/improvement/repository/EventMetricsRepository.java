package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.domain.EventMetricsId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMetricsRepository extends JpaRepository<EventMetrics, EventMetricsId> {
    List<EventMetrics> findByMetricsUuidIn(List<UUID> metricsUuids);

    @Query("SELECT em.eventUuid FROM EventMetrics em WHERE em.metricsUuid IN :metricsUuids")
    List<UUID> findEventUuidByMetricsUuidIn(@Param("metricsUuids") List<UUID> metricsUuids);
}

