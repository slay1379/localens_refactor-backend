package com.example.localens.improvement.domain;

import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class EventMetricsId implements Serializable {
    private UUID eventUuid;
    private UUID metricsUuid;
}
