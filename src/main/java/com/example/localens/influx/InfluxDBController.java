package com.example.localens.influx;

import com.influxdb.query.FluxTable;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/influx")
public class InfluxDBController {

    private final InfluxDBService influxDBService;

    public InfluxDBController(InfluxDBService influxDBService) {
        this.influxDBService = influxDBService;
    }

    @PostMapping("/data")
    public ResponseEntity<List<FluxTable>> getData(@RequestBody InfluxDataRequest influxDataRequest) {
        List<FluxTable> data = influxDBService.getData(influxDataRequest);
        return ResponseEntity.ok(data);
    }
}
