package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.domain.EventMetricsId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMetricsRepository extends JpaRepository<EventMetrics, EventMetricsId> {
    List<EventMetrics> findByMetricsUuidIn(List<String> metricsUuids);

    List<String> findEventUuidByMetricsUuidIn(List<String> metricsUuids);
}
