package com.example.localens.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InfluxDBService {

    private final InfluxDBClient influxDBClient;

    @Autowired
    public InfluxDBService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public List<FluxTable> getData(InfluxDataRequest influxDataRequest) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") |> range(start: %s) |> filter(fn: (r) => r.measurement == \"%s\" and r._field == \"%s\")",
                influxDataRequest.getMeasurement(),
                influxDataRequest.getTimeRange(),
                influxDataRequest.getMeasurement(),
                influxDataRequest.getField()
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery);
    }
}
