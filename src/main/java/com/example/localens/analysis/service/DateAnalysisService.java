package com.example.localens.analysis.service;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DateAnalysisService {
    private final InfluxDBClientWrapper influxDBClientWrapper;
    private final MetricStatsService metricStatsService;

    public Map<String, Integer> analyzeDate(String place, String date) {
        Map<String, Double> rawValues = queryInfluxForDate(place, date);

        Map<String, Integer> normalizedMap = new LinkedHashMap<>();
        for (Entry<String, Double> entry : rawValues.entrySet()) {
            String field = entry.getKey();
            double rawValue = entry.getValue();
            double normalized = metricStatsService.normalizeValue(place, field, rawValue);
            normalizedMap.put(field, (int) Math.round(normalized * 100));
        }
        return normalizedMap;
    }

    private Map<String, Double> queryInfluxForDate(String place, String date) {
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\") "
                        + "|> keep(columns: [\"_time\", \"_field\", \"_value\"])",
                place
        );
        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> rawMap = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (var record : table.getRecords()) {
                Object fieldObj = record.getValueByKey("_field");
                if (fieldObj == null) {
                    continue;
                }
                String fieldName = fieldObj.toString();

                Object valueObj = record.getValueByKey("_value");
                if (valueObj != null) {
                    double numericValue = Double.parseDouble(valueObj.toString());
                    rawMap.put(fieldName, numericValue);
                }
            }
        }
        return rawMap;
    }
}
