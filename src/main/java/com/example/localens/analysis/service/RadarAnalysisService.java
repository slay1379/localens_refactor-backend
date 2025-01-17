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
        // 1) 상권정보 조회
        CommercialDistrict district = districtRepository.findById(districtUuid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid districtUuid: " + districtUuid));

        String place = district.getDistrictName();

        // 2) InfluxDB에서 데이터 조회
        Map<String, Double> rawData = new LinkedHashMap<>();

        // 체류시간 대비 방문자 수 조회
        String stayPerVisitorQuery = String.format("""
            from(bucket: "stay_per_visitor_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
                |> last()
            """, CURRENT_RANGE, place);
        rawData.put("stayPerVisitor", executeQuery(stayPerVisitorQuery));

        // 유동인구 수 조회
        String populationQuery = String.format("""
            from(bucket: "result_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> last()
            """, CURRENT_RANGE, place);
        rawData.put("population", executeQuery(populationQuery));

        // 체류/방문 비율 조회
        String stayVisitQuery = String.format("""
            from(bucket: "result_stay_visit_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> last()
            """, CURRENT_RANGE, place);
        rawData.put("stayVisit", executeQuery(stayVisitQuery));

        // 혼잡도 변화율 조회 (date_compare_range 사용)
        String congestionQuery = String.format("""
            from(bucket: "date_congestion")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
                |> last()
            """, DATE_COMPARE_RANGE, place);
        rawData.put("congestion", executeQuery(congestionQuery));

        // 체류시간 변화율 조회 (date_compare_range 사용)
        String stayTimeChangeQuery = String.format("""
            from(bucket: "date_stay_duration")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
                |> last()
            """, DATE_COMPARE_RANGE, place);
        rawData.put("stayTimeChange", executeQuery(stayTimeChangeQuery));

        // 방문 집중도 조회 (date_compare_range 사용)
        String visitConcentrationQuery = String.format("""
            from(bucket: "date_stay_visit")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> last()
            """, DATE_COMPARE_RANGE, place);
        rawData.put("visitConcentration", executeQuery(visitConcentrationQuery));

        // 3) 정규화
        Map<String, Integer> normalizedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : rawData.entrySet()) {
            String finalField = entry.getKey();
            double rawValue = entry.getValue();
            double normalized = metricStatsService.normalizeValue(place, finalField, rawValue);
            int scaled = (int) Math.round(normalized * 100);
            normalizedMap.put(finalField, scaled);
        }

        // 4) DistrictDTO 생성
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

        // 5) 상위 2개 추출
        Map<String, Object> topTwoMap = findTopTwo(normalizedMap);

        // 6) RadarDataDTO 생성
        RadarDataDTO radarDataDTO = new RadarDataDTO();
        radarDataDTO.setDistrictInfo(districtDTO);
        radarDataDTO.setOverallData(normalizedMap);
        radarDataDTO.setTopTwo(topTwoMap);

        return radarDataDTO;
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
