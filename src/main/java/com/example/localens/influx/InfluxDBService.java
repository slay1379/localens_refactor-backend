package com.example.localens.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InfluxDBService {

    private final InfluxDBClientWrapper influxDBClientWrapper;

    @Autowired
    public InfluxDBService(InfluxDBClientWrapper influxDBClientWrapper) {
        this.influxDBClientWrapper = influxDBClientWrapper;
    }

    public List<String> getFieldKeys(String measurement) {
        String fluxQuery = String.format(
                "import \"influxdata/influxdb/schema\"\n" +
                        "schema.fieldKeys(bucket: \"%s\", predicate: (r) => r._measurement == \"%s\")",
                influxDBClientWrapper.getBucket(), measurement);

        QueryApi queryApi = influxDBClientWrapper.getInfluxDBClient().getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxDBClientWrapper.getOrg());

        List<String> fieldKeys = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String fieldKey = (String) record.getValue();
                fieldKeys.add(fieldKey);
            }
        }

        return fieldKeys;
    }
}
