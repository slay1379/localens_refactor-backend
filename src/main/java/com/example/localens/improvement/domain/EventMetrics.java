package com.example.localens.improvement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_metrics")
@IdClass(EventMetricsId.class)
@Getter
@Setter
public class EventMetrics {

    @Id
    @Column(name = "event_uuid", columnDefinition = "BINARY(16)")
    private UUID eventUuid;

    @Id
    @Column(name = "metrics_uuid", columnDefinition = "BINARY(16)")
    private UUID metricsUuid;
}
