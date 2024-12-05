package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Metric;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepository extends JpaRepository<Metric, UUID> {
    @Query("SELECT m.metricsUuid FROM Metric m WHERE m.metricsName = :metricsName")
    UUID findMetricsUuidByMetricsName(@Param("metricsName") String metricsName);

}
