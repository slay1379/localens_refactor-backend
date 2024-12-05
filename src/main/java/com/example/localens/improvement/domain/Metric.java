package com.example.localens.improvement.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_metric_change_type")
@Getter
@Setter
public class Metric {

    @Id
    private String metricsUuid;

    private String metricsName;
}