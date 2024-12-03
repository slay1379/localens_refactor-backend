package com.example.localens.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        try {
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
        } catch (Exception e) {
            System.err.println("InfluxDB에서 필드 키를 가져오는 중 오류 발생: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<FluxRecord> getPlaceData(String placeName) {
        try {
            String fluxQuery = String.format(
                    "from(bucket: \"%s\") |> range(start: -30d) |> filter(fn: (r) => r.place == \"%s\")",
                    influxDBClientWrapper.getBucket(), placeName);

            QueryApi queryApi = influxDBClientWrapper.getInfluxDBClient().getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxDBClientWrapper.getOrg());

            List<FluxRecord> records = new ArrayList<>();
            for (FluxTable table : tables) {
                records.addAll(table.getRecords());
            }
            return records;
        } catch (Exception e) {
            System.err.println("InfluxDB에서 place 데이터를 가져오는 중 오류 발생: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Double> getLatestMetricsByDistrictUuid(String districtUuid) {
        try {
            String fluxQuery = String.format(
                    "from(bucket:\"%s\")\n" +
                            "|> range(start: 0)\n" +
                            "|> filter(fn: (r) => r[\"district_uuid\"] == \"%s\")\n" +
                            "|> last()",
                    influxDBClientWrapper.getBucket(), districtUuid);

            QueryApi queryApi = influxDBClientWrapper.getInfluxDBClient().getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxDBClientWrapper.getOrg());

            Map<String, Double> metrics = new HashMap<>();

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    String field = (String) record.getValueByKey("_field");
                    double value = ((Number) record.getValue()).doubleValue();
                    metrics.put(field, value);
                }
            }

            return metrics;
        } catch (Exception e) {
            System.err.println("InfluxDB에서 지표 데이터를 가져오는 중 오류 발생: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public List<Map<String, Double>> getMetricsByDistrictUuidAndTimeRange(String districtUuid, LocalDateTime start,
                                                                          LocalDateTime end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String startString = start.format(formatter);
            String endString = end.format(formatter);

            String fluxQuery = String.format(
                    "from(bucket:\"%s\")\n" +
                            "|> range(start: %s, stop: %s)\n" +
                            "|> filter(fn: (r) -> r[\"district_uuid\"] == \"%s\")",
                    influxDBClientWrapper.getBucket(), startString, endString, districtUuid);

            QueryApi queryApi = influxDBClientWrapper.getInfluxDBClient().getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxDBClientWrapper.getOrg());

            List<Map<String, Double>> metricsList = new ArrayList<>();

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    String field = (String) record.getValueByKey("_field");
                    double value = ((Number) record.getValue()).doubleValue();

                    Map<String, Double> metric = new HashMap<>();
                    metric.put(field, value);

                    metricsList.add(metric);
                }
            }

            return metricsList;
        } catch (Exception e) {
            System.err.println("InfluxDB에서 특정 기간 동안 상권 데이터를 가져오는 중 오류 발생: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
