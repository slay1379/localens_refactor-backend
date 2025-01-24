package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.DistrictDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.dto.RadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarTimeSeriesDataDTO;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
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

    private static final String CURRENT_RANGE =
            "start: 2024-05-30T00:00:00Z, stop: 2025-02-01T23:59:59Z";
    private static final String DATE_COMPARE_RANGE =
            "start: 2023-08-30T00:00:00Z, stop: 2025-02-01T23:59:59Z";

    private static final Map<String, String> keyToKoreanMap = Map.of(
            "population", "유동인구 수",
            "stayVisit", "체류/방문 비율",
            "congestion", "혼잡도 변화율",
            "stayPerVisitor", "체류시간 대비 방문자 수",
            "visitConcentration", "방문 집중도",
            "stayTimeChange", "체류시간 변화율"
    );

    public RadarDataDTO<AnalysisRadarDistrictInfoDTO> getRadarData(Integer districtUuid) {
        CommercialDistrict district = districtRepository.findById(districtUuid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid districtUuid: " + districtUuid));

        String place = district.getDistrictName();
        log.info("Getting radar data for place: {}", place);

        Map<String, Double> rawData = new LinkedHashMap<>();
        rawData.put("stayPerVisitor", executeQuery(createQuery("stay_per_visitor_bucket", place, "stay_to_visitor", CURRENT_RANGE)));
        rawData.put("population", executeQuery(createQuery("result_bucket", place, "total_population", CURRENT_RANGE)));
        rawData.put("stayVisit", executeQuery(createQuery("result_stay_visit_bucket", place, "stay_visit_ratio", CURRENT_RANGE)));
        rawData.put("congestion", executeQuery(createQuery("date_congestion", place, "congestion_change_rate", DATE_COMPARE_RANGE)));
        rawData.put("stayTimeChange", executeQuery(createQuery("date_stay_duration", place, "stay_duration_change_rate", DATE_COMPARE_RANGE)));
        rawData.put("visitConcentration", executeQuery(createQuery("date_stay_visit", place, "stay_visit_ratio", DATE_COMPARE_RANGE)));

        log.info("Raw data for place {}: {}", place, rawData);

        return buildRadarDataDTO(district, place, rawData);
    }

    public RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> getRadarDataByDate(
            Integer districtUuid, LocalDateTime targetDate) {
        CommercialDistrict district = districtRepository.findById(districtUuid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid districtUuid"));

        String place = district.getDistrictName();
        String dateRange = createDateRange(targetDate);
        log.info("Getting radar data for place: {} at date: {}", place, CURRENT_RANGE);

        Map<String, Double> rawData = new LinkedHashMap<>();
        rawData.put("stayPerVisitor", executeQuery(createQuery("stay_per_visitor_bucket", place, "stay_to_visitor", CURRENT_RANGE)));
        rawData.put("population", executeQuery(createQuery("result_bucket", place, "total_population", CURRENT_RANGE)));
        rawData.put("stayVisit", executeQuery(createQuery("result_stay_visit_bucket", place, "stay_visit_ratio", CURRENT_RANGE)));
        rawData.put("congestion", executeQuery(createQuery("date_congestion", place, "congestion_change_rate", DATE_COMPARE_RANGE)));
        rawData.put("stayTimeChange", executeQuery(createQuery("date_stay_duration", place, "stay_duration_change_rate", DATE_COMPARE_RANGE)));
        rawData.put("visitConcentration", executeQuery(createQuery("date_stay_visit", place, "stay_visit_ratio", DATE_COMPARE_RANGE)));

        log.info("Raw data for place {}: {}", place, rawData);

        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> result = new RadarTimeSeriesDataDTO<>();
        result.setDistrictInfo(createDistrictInfo(district));
        result.setOverallData(normalizeData(place, rawData));
        result.setTopTwo(findTopTwo(normalizeData(place, rawData)));
        result.setTimeSeriesData(Collections.singletonList(targetDate));

        return result;
    }

    private String createQuery(String bucket, String place, String field, String timeRange) {
        return String.format("""
                from(bucket: "%s")
                    |> range(%s)
                    |> filter(fn: (r) => r["place"] == "%s")
                    |> filter(fn: (r) => r["_field"] == "%s")
                    |> mean()
                    |> yield(name: "mean")
                """, bucket, timeRange, place, field);
    }

    // 날짜 범위 생성 메서드
    private String createDateRange(LocalDateTime date) {
        return String.format("start: %s, stop: %s",
                date.toLocalDate().atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                date.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }

    private RadarDataDTO<AnalysisRadarDistrictInfoDTO> buildRadarDataDTO(
            CommercialDistrict district,
            String place,
            Map<String, Double> rawData) {
        Map<String, Integer> normalizedMap = normalizeData(place, rawData);

        RadarDataDTO<AnalysisRadarDistrictInfoDTO> radarDataDTO = new RadarDataDTO<>();
        radarDataDTO.setDistrictInfo(createDistrictInfo(district));
        radarDataDTO.setOverallData(normalizedMap);
        radarDataDTO.setTopTwo(findTopTwo(normalizedMap));

        return radarDataDTO;
    }

    private double executeQuery(String query) {
        try {
            log.debug("Executing InfluxDB query: {}", query);
            List<FluxTable> tables = influxDBClientWrapper.query(query);

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
            log.debug("Query result value: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            return 0.0;
        }
    }

    private static final Map<String, String> FIELD_MAPPINGS = Map.of(
            "stayPerVisitor", "stay_to_visitor",
            "population", "total_population",
            "stayVisit", "stay_visit_ratio",
            "congestion", "congestion_change_rate",
            "stayTimeChange", "stay_duration_change_rate",
            "visitConcentration", "visit_concentration"
    );

    private Map<String, Integer> normalizeData(String place, Map<String, Double> rawData) {
        return rawData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (int) Math.round(metricStatsService.normalizeValue(
                                FIELD_MAPPINGS.get(e.getKey()),
                                e.getValue()
                        )),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    private AnalysisRadarDistrictInfoDTO createDistrictInfo(CommercialDistrict district) {
        AnalysisRadarDistrictInfoDTO districtInfo = new AnalysisRadarDistrictInfoDTO();
        districtInfo.setDistrictName(district.getDistrictName());
        if (district.getCluster() != null) {
            districtInfo.setClusterName(district.getCluster().getClusterName());
        }
        districtInfo.setLatitude(district.getLatitude());
        districtInfo.setLongitude(district.getLongitude());
        return districtInfo;
    }

    private Map<String, Object> findTopTwo(Map<String, Integer> overallData) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(overallData.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Map<String, Object> topTwo = new LinkedHashMap<>();

        if (!sorted.isEmpty()) {
            addTopEntry("first", sorted.get(0), topTwo);
        }
        if (sorted.size() > 1) {
            addTopEntry("second", sorted.get(1), topTwo);
        }
        return topTwo;
    }

    private void addTopEntry(String position, Map.Entry<String, Integer> entry, Map<String, Object> topTwo) {
        Map<String, Object> entryMap = new LinkedHashMap<>();
        entryMap.put("value", entry.getValue());
        entryMap.put("name", keyToKoreanMap.getOrDefault(entry.getKey(), entry.getKey()));
        topTwo.put(position, entryMap);
    }
}
