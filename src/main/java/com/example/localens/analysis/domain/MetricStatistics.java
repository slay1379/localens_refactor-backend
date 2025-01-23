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

    @Column(name = "place", nullable = false)
    private String place;  // GLOBAL 또는 특정 지역 ID

    @Column(name = "field", nullable = false)
    private String field;

    @Column(name = "min_value", nullable = false)
    private double minValue;

    @Column(name = "max_value", nullable = false)
    private double maxValue;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}

