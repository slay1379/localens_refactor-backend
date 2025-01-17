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
        // InfluxDB timestamp format으로 변환
        String startTime = date.split("T")[0] + "T00:00:00Z";
        String endTime = date.split("T")[0] + "T23:59:59Z";

        Map<String, Double> rawMap = new LinkedHashMap<>();

        // 1. 유동인구 수 조회
        String populationQuery = String.format("""
            from(bucket: "result_bucket")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> mean()
            """, startTime, endTime, place);
        rawMap.put("population", executeQuery("population", populationQuery));

        // 2. 체류/방문 비율 조회
        String stayVisitQuery = String.format("""
            from(bucket: "result_stay_visit_bucket")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> mean()
            """, startTime, endTime, place);
        rawMap.put("stayVisit", executeQuery("stayVisit", stayVisitQuery));

        // 3. 혼잡도 변화율 조회
        String congestionQuery = String.format("""
            from(bucket: "date_congestion")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
                |> mean()
            """, startTime, endTime, place);
        rawMap.put("congestion", executeQuery("congestion", congestionQuery));

        // 4. 체류시간 대비 방문자 수 조회
        String stayPerVisitorQuery = String.format("""
            from(bucket: "stay_per_visitor_bucket")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
                |> mean()
            """, startTime, endTime, place);
        rawMap.put("stayPerVisitor", executeQuery("stayPerVisitor", stayPerVisitorQuery));

        // 5. 체류시간 변화율 조회
        String stayTimeChangeQuery = String.format("""
            from(bucket: "date_stay_duration")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
                |> mean()
            """, startTime, endTime, place);
        rawMap.put("stayTimeChange", executeQuery("stayTimeChange", stayTimeChangeQuery));

        log.info("Query results for place: {}, date: {}, results: {}", place, date, rawMap);
        return rawMap;
    }

    private double executeQuery(String metric, String query) {
        try {
            log.info("Executing {} query: {}", metric, query);
            List<FluxTable> tables = influxDBClientWrapper.query(query);

            if (tables == null || tables.isEmpty()) {
                log.warn("{} query returned no tables", metric);
                return 0.0;
            }

            List<FluxRecord> records = tables.get(0).getRecords();
            if (records == null || records.isEmpty()) {
                log.warn("{} query returned no records", metric);
                return 0.0;
            }

            FluxRecord record = records.get(0);
            Object value = record.getValueByKey("_value");
            if (value == null) {
                log.warn("{} query returned null value", metric);
                return 0.0;
            }

            double result = Double.parseDouble(value.toString());
            log.info("{} query result: {}", metric, result);
            return result;

        } catch (Exception e) {
            log.error("Error executing {} query: {}", metric, e.getMessage());
            return 0.0;
        }
    }
}