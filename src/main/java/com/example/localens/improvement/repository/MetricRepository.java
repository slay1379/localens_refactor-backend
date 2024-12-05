package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, String> {
    String findMetricsUuidByMetricsName(String metricsName);
}
