package com.example.localens.influx;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfluxDataRequest {
    private String measurement;
    private String field;
    private String timeRange;

    public InfluxDataRequest() {}
}
