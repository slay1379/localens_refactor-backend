package com.example.localens.improvement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_metrics")
@IdClass(EventMetricsId.class)
@Getter
@Setter
public class EventMetrics {

    @Id
    @Column(name = "event_uuid")
    private String eventUuid;

    @Id
    @Column(name = "metrics_uuid")
    private String metricsUuid;
}
