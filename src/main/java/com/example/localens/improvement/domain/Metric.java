package com.example.localens.improvement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_metric_change_type")
@Getter
@Setter
public class Metric {

    @Id
    @Column(name = "metrics_uuid", columnDefinition = "BINARY(16)")
    private UUID metricsUuid;

    @Column(name = "metrics_name", length = 100, nullable = false)
    private String metricsName;
}