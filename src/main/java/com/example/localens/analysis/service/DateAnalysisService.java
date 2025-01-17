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
        String startTime = date.split("T")[0] + "T00:00:00Z";
        String endTime = date.split("T")[0] + "T23:59:59Z";

        Map<String, Double> rawMap = new LinkedHashMap<>();

        // 1. 유동인구 수 조회 (result_bucket)
        String populationQuery = String.format("""
            from(bucket: "result_bucket")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> last()
            """, startTime, endTime, place);
        rawMap.put("population", executeQuery(populationQuery));

        // 2. 체류/방문 비율 조회 (result_stay_visit_bucket)
        String stayVisitQuery = String.format("""
            from(bucket: "result_stay_visit_bucket")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> last()
            """, startTime, endTime, place);
        rawMap.put("stayVisit", executeQuery(stayVisitQuery));

        // 3. 혼잡도 변화율 조회 (date_congestion)
        String congestionQuery = String.format("""
            from(bucket: "date_congestion")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
                |> last()
            """, startTime, endTime, place);
        rawMap.put("congestion", executeQuery(congestionQuery));

        // 4. 체류시간 대비 방문자 수 조회 (stay_per_visitor_bucket)
        String stayPerVisitorQuery = String.format("""
            from(bucket: "stay_per_visitor_bucket")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
                |> last()
            """, startTime, endTime, place);
        rawMap.put("stayPerVisitor", executeQuery(stayPerVisitorQuery));

        // 5. 체류시간 변화율 조회 (date_stay_duration)
        String stayDurationQuery = String.format("""
            from(bucket: "date_stay_duration")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
                |> last()
            """, startTime, endTime, place);
        rawMap.put("stayTimeChange", executeQuery(stayDurationQuery));

        log.info("Query results for place: {}, date: {}, results: {}", place, date, rawMap);
        return rawMap;
    }

    private double executeQuery(String query) {
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
                return 0.0;
            }
            Object value = tables.get(0).getRecords().get(0).getValueByKey("_value");
            return value != null ? Double.parseDouble(value.toString()) : 0.0;
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            return 0.0;
        }
    }
}
