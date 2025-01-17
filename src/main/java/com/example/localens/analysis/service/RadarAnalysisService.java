package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.DistrictDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RadarAnalysisService {

    private final CommercialDistrictRepository districtRepository;
    private final MetricStatsService metricStatsService;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    // 시간 범위 상수 정의
    private static final String CURRENT_RANGE =
            "start: 2024-05-30T00:00:00Z, stop: 2024-08-31T23:59:59Z";
    private static final String DATE_COMPARE_RANGE =
            "start: 2023-08-30T00:00:00Z, stop: 2024-08-31T23:59:59Z";

    public RadarDataDTO getRadarData(Integer districtUuid) {
        CommercialDistrict district = districtRepository.findById(districtUuid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid districtUuid: " + districtUuid));

        String place = district.getDistrictName();
        log.info("Getting radar data for place: {}", place);

        // InfluxDB에서 데이터 조회
        Map<String, Double> rawData = new LinkedHashMap<>();

        // 체류시간 대비 방문자 수 조회
        String stayPerVisitorQuery = String.format("""
            from(bucket: "stay_per_visitor_bucket")
                |> range(start: 2024-05-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
                |> mean()
                |> yield(name: "mean")
            """, place);
        rawData.put("stayPerVisitor", executeQuery(stayPerVisitorQuery));

        // 유동인구 수 조회
        String populationQuery = String.format("""
            from(bucket: "result_bucket")
                |> range(start: 2024-05-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> mean()
                |> yield(name: "mean")
            """, place);
        rawData.put("population", executeQuery(populationQuery));

        // 체류/방문 비율 조회
        String stayVisitQuery = String.format("""
            from(bucket: "result_stay_visit_bucket")
                |> range(start: 2024-05-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> mean()
                |> yield(name: "mean")
            """, place);
        rawData.put("stayVisit", executeQuery(stayVisitQuery));

        // 혼잡도 변화율 조회
        String congestionQuery = String.format("""
            from(bucket: "date_congestion")
                |> range(start: 2023-08-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
                |> mean()
                |> yield(name: "mean")
            """, place);
        rawData.put("congestion", executeQuery(congestionQuery));

        // 체류시간 변화율 조회
        String stayTimeChangeQuery = String.format("""
            from(bucket: "date_stay_duration")
                |> range(start: 2023-08-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
                |> mean()
                |> yield(name: "mean")
            """, place);
        rawData.put("stayTimeChange", executeQuery(stayTimeChangeQuery));

        // 방문 집중도 조회
        String visitConcentrationQuery = String.format("""
            from(bucket: "date_stay_visit")
                |> range(start: 2023-08-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> mean()
                |> yield(name: "mean")
            """, place);
        rawData.put("visitConcentration", executeQuery(visitConcentrationQuery));

        log.info("Raw data for place {}: {}", place, rawData);

        // 정규화
        Map<String, Integer> normalizedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : rawData.entrySet()) {
            String finalField = entry.getKey();
            double rawValue = entry.getValue();
            double normalized = metricStatsService.normalizeValue(place, finalField, rawValue);
            int scaled = (int) Math.round(normalized * 100);
            normalizedMap.put(finalField, scaled);
        }

        // DistrictDTO 생성
        DistrictDTO districtDTO = new DistrictDTO();
        districtDTO.setDistrictName(district.getDistrictName());
        if (district.getLatitude() != null) {
            districtDTO.setLatitude(district.getLatitude().doubleValue());
        }
        if (district.getLongitude() != null) {
            districtDTO.setLongitude(district.getLongitude().doubleValue());
        }
        if (district.getCluster() != null) {
            districtDTO.setClusterName(district.getCluster().getClusterName());
        }

        Map<String, Object> topTwoMap = findTopTwo(normalizedMap);

        return new RadarDataDTO(districtDTO, normalizedMap, topTwoMap);
    }

    private double executeQuery(String query) {
        try {
            log.info("Executing InfluxDB query: {}", query);
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            log.info("Query result tables: {}", tables);

            if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
                log.warn("No data found for query");
                return 0.0;
            }

            Object value = tables.get(0).getRecords().get(0).getValueByKey("_value");
            if (value == null) {
                log.warn("Null value found in query result");
                return 0.0;
            }

            double result = Double.parseDouble(value.toString());
            log.info("Query result value: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            return 0.0;
        }
    }

    private Map<String, Object> findTopTwo(Map<String, Integer> overallData) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(overallData.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Map<String, String> keyToKoreanMap = new LinkedHashMap<>();
        keyToKoreanMap.put("population", "유동인구 수");
        keyToKoreanMap.put("stayVisit", "체류/방문 비율");
        keyToKoreanMap.put("congestion", "혼잡도 변화율");
        keyToKoreanMap.put("stayPerVisitor", "체류시간 대비 방문자 수");
        keyToKoreanMap.put("visitConcentration", "방문 집중도");
        keyToKoreanMap.put("stayTimeChange", "체류시간 변화율");

        Map<String, Object> topTwo = new LinkedHashMap<>();

        if (!sorted.isEmpty()) {
            var firstKey = sorted.get(0).getKey();
            var firstVal = sorted.get(0).getValue();
            Map<String, Object> firstMap = new LinkedHashMap<>();
            firstMap.put("value", firstVal);
            firstMap.put("name", keyToKoreanMap.getOrDefault(firstKey, firstKey));
            topTwo.put("first", firstMap);
        }
        if (sorted.size() > 1) {
            var secondKey = sorted.get(1).getKey();
            var secondVal = sorted.get(1).getValue();
            Map<String, Object> secondMap = new LinkedHashMap<>();
            secondMap.put("value", secondVal);
            secondMap.put("name", keyToKoreanMap.getOrDefault(secondKey, secondKey));
            topTwo.put("second", secondMap);
        }
        return topTwo;
    }
}
