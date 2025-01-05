package com.example.localens.analysis.repository;

import com.example.localens.analysis.domain.MetricStatistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricStatisticsRepository extends JpaRepository<MetricStatistics, Long> {
    Optional<MetricStatistics> findByPlaceAndField(String place, String field);
}
