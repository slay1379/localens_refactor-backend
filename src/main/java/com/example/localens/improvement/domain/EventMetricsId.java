package com.example.localens.improvement.domain;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class EventMetricsId implements Serializable {
    private String eventUuid;
    private String metricsUuid;
}
