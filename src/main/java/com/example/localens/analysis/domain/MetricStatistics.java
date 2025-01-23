package com.example.localens.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "global_metric_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field", nullable = false)
    private String field; // 필드 이름 (예: stay_visit_ratio, total_population 등)

    @Column(name = "min_value", nullable = false)
    private double minValue; // 모든 지역에서의 최소값

    @Column(name = "max_value", nullable = false)
    private double maxValue; // 모든 지역에서의 최대값

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated; // 마지막 업데이트 시간
}

