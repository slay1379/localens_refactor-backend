package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MetricRepository extends JpaRepository<Metric, String> {
    @Query("SELECT m.metricsUuid FROM Metric m WHERE m.metricsName = :metricsName")
    String findMetricsUuidByMetricsName(@Param("metricsName") String metricsName);

}
